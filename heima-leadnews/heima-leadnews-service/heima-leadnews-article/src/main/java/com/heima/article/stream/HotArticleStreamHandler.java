package com.heima.article.stream;

import com.alibaba.fastjson.JSON;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.mess.UpdateArticleMess;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HotArticleStreamHandler {


    @Bean
    public KStream<String, String> handlerArticle(StreamsBuilder streamsBuilder){
        // 接收消息
        KStream<String, String> stream = streamsBuilder.stream("hot.article.score.topic");
        // 聚合流式处理
        stream.map((key, value) -> {
            UpdateArticleMess mess = JSON.parseObject(value, UpdateArticleMess.class);
            return new KeyValue<>(mess.getArticleId().toString(),mess.getType()+":"+mess.getAdd());
        })
            // 分组
            .groupBy((key, value) -> key)
            // 时间窗口
            .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
            // 自行的完成聚合的计算
            .aggregate(new Initializer<String>() {
                // 初始聚合，返回值是消息的value
                @Override
                public String apply() {
                    return "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0";
                }
                // 真正的聚合操作
            }, new Aggregator<String, String, String>() {
                @Override
                public String apply(String key, String value, String initValue) {
                    String[] initArr = initValue.split(",");
                    int col=0,com=0,lik=0,vie=0;
                    for (String s : initArr) {
                        String[] split = s.split(":");
                        switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])){
                            case COLLECTION:
                                col=Integer.parseInt(split[1]);
                                break;
                            case COMMENT:
                                com=Integer.parseInt(split[1]);
                                break;
                            case LIKES:
                                lik=Integer.parseInt(split[1]);
                                break;
                            case VIEWS:
                                vie=Integer.parseInt(split[1]);
                                break;
                        }
                    }
                    // 累加操作
                    String[] valArr = value.split(":");
                    switch (UpdateArticleMess.UpdateArticleType.valueOf(valArr[0])){
                        case COLLECTION:
                            col+=Integer.parseInt(valArr[1]);
                            break;
                        case COMMENT:
                            com+=Integer.parseInt(valArr[1]);
                            break;
                        case LIKES:
                            lik+=Integer.parseInt(valArr[1]);
                            break;
                        case VIEWS:
                            vie+=Integer.parseInt(valArr[1]);
                            break;
                    }
                    String format = String.format("COLLECTION:%d,COMMENT:%d,LIKES:%d,VIEWS:%d", col, com, lik, vie);
                    log.info("文章：{},当前时间窗口内聚合结果：{}",key,format);
                    return format;
                }
            }, Materialized.as("hot-article-stream-100"))
            // 抑制窗口内的中间结果，只在窗口关闭时发送
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
            .toStream()
            .map((key,value)->{
                return new KeyValue<>(key.key(), formValStr(key.key(),value));
            })
            //发送消息
            .to("hot.article.incr.handle.topic");

        return stream;
    }

    private String formValStr(String articleId,String value) {
        long id = Long.parseLong(articleId);
        String[] valArr = value.split(",");
        ArticleVisitStreamMess visitMess = new ArticleVisitStreamMess();
        for (String val : valArr) {
            String[] split = val.split(":");
            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])){
                case COLLECTION:
                    visitMess.setCollect(Integer.parseInt(split[1]));
                    break;
                case COMMENT:
                    visitMess.setComment(Integer.parseInt(split[1]));
                    break;
                case LIKES:
                    visitMess.setLike(Integer.parseInt(split[1]));
                    break;
                case VIEWS:
                    visitMess.setView(Integer.parseInt(split[1]));
                    break;
            }
        }
        return JSON.toJSONString(visitMess);
    }


}
