# es-relative
如果需要获取搜索词和某文档某些字段的tf/idf分数，可以使用此插件
#注意
1、此插件适用es5.4.2版本，其他版本的es需要适量修改
2、此插件默认es安装了ansj分词器，如果是其他分词器，可以修改代码
#使用方法
1、安装插件
2、重启es
3、使用script_fields获取相关性分数字段，size的大小要等于分片数
{
	"from": 0,
	"size": 10,
	"rescore": {
		"query": {
			"rescore_query": {},
			"score_mode": "max",
			"query_weight": 1,
			"rescore_query_weight": 100000
		},
		"window_size": 1
	},
	"script_fields": {
		"relative": {
			"script": {
				"lang": "native",
				"inline": "BatchRelative",
				"params": {
					"fields": ["content"，"title"],
					"queryWords": "苹果手机",
					"articleIds": [1232，243，244，2434，45]
				}
			}
		}
	}

}
