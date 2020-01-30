package org.lz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.internal.Nullable;
import org.elasticsearch.script.AbstractSearchScript;
import org.util.RelativeUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BatchRelativeScript  extends AbstractSearchScript {


    public static Logger log = LogManager.getLogger(BatchRelativeScript.class);
    List fields;
    String queryWords;
    List articleIds;

    public BatchRelativeScript(@Nullable Map<String, Object> params){
        fields = RelativeUtil.getListFromParams(params, "fields");
        queryWords = RelativeUtil.getStringFromParams(params, "queryWords");
        articleIds=RelativeUtil.getListFromParams(params, "articleIds");
    }

    @Override
    public Object run() {

        //字段名，文章id，分数
        Map<String,Map<String,Float>> relativeMap=new ConcurrentHashMap<>();
        CountDownLatch countDownLatch=new CountDownLatch(fields.size());
        for (Object field:fields){
            RelativeUtil.executor.execute(()->{
                Map<String,Float> score=RelativeUtil.batchRelativeScore(doc(), indexLookup().getIndexSearcher(), field.toString(), queryWords,articleIds,countDownLatch);
                relativeMap.put(field.toString(),score);
            });
        }
        try {
            countDownLatch.await(150, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("",e);
        }
        return relativeMap;
    }
}
