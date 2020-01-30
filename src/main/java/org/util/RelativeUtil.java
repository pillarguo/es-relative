package org.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.search.*;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.search.lookup.LeafDocLookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelativeUtil {

    public static Logger log = LogManager.getLogger(RelativeUtil.class);

    public static ExecutorService executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(),
            0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.DiscardPolicy());

    public static Map<String, Float> batchRelativeScore(LeafDocLookup leafDocLookup, IndexSearcher indexSearcher,
                                                        String fieldName, String queryWords, List articleIds, CountDownLatch countDownLatch) {
        Map<String, Float> idScoreMap = new HashMap<>();//第一个String是文章id，第2个是分数
        try {
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            Analyzer queryAnsjAnalyzer = leafDocLookup.mapperService().getIndexAnalyzers().get("query_ansj");
            Analyzer keyWordAnalyzer = leafDocLookup.mapperService().getIndexAnalyzers().get("keyword");
            QueryBuilder builder1 = new QueryBuilder(queryAnsjAnalyzer);
            Query relativeQuery1 = builder1.createBooleanQuery(fieldName, queryWords);
            QueryBuilder builder2 = new QueryBuilder(keyWordAnalyzer);
            Query relativeQuery2 = builder2.createBooleanQuery(fieldName, queryWords);
            Query exactQuery = IntPoint.newSetQuery("article_id", articleIds);
            booleanQueryBuilder.add(exactQuery, BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(relativeQuery1, BooleanClause.Occur.SHOULD);
            booleanQueryBuilder.add(relativeQuery2, BooleanClause.Occur.SHOULD);
            BooleanQuery booleanQuery = booleanQueryBuilder.build();
            TopDocs topDocs = indexSearcher.search(booleanQuery, articleIds.size());
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                // 取出文档编号
                int docID = scoreDoc.doc;
                // 根据编号去找文档
                Document doc=indexSearcher.getIndexReader().document(docID);
                String _uid = doc.get("_uid");//_uid大致是type和_id的拼接，类似这样：article#faxian_17670268
                String regex = ".*_(\\d*)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(_uid);
                while (matcher.find()){
                    String article_id = matcher.group(1);
                    float actualScore = scoreDoc.score;
                    idScoreMap.put(article_id, actualScore);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("", e);
        }finally {
            countDownLatch.countDown();
        }
        return idScoreMap;
    }


    /**
     * 根据参数名获取List参数值
     *
     * @param params    参数Map
     * @param paramName 参数名
     * @return list类型的参数值
     */
    public static List getListFromParams(Map<String, Object> params, String paramName) {
        if (params == null) {
            return null;
        }
        Object listValue = params.get(paramName);
        if (listValue != null && listValue instanceof List) {
            List paramValue = (List) listValue;
            return paramValue;
        }
        return null;
    }




    /**
     * 根据参数名获取String参数值
     *
     * @param params    参数Map
     * @param paramName 参数名
     * @return string类型的参数值
     */
    public static String getStringFromParams(Map<String, Object> params, String paramName) {
        if (params == null) {
            return null;
        }
        Object stringValue = params.get(paramName);
        if (stringValue != null && stringValue instanceof String) {
            String paramValue = (String) stringValue;
            return paramValue;
        } else if (stringValue instanceof Integer || stringValue instanceof Double || stringValue instanceof Float) {
            String paramValue = String.valueOf(stringValue);
            return paramValue;
        }
        return null;
    }
}
