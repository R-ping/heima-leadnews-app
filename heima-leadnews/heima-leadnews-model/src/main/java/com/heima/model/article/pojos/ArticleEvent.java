package com.heima.model.article.pojos;

import java.util.Date;
import lombok.Data;

@Data
public class ArticleEvent {

    private Long id;

    /**
     * 文章id
     */
    private Long articleId;

//    private Integer wmNewsId;
    // 1 事务失败，2 事务成功
//    private Byte transactionalStatus;

    private Byte retryCount; // 重试次数，默认0，理想情况minio、es都可重试1次
    private Byte maxRetryCount = 2; // 最大重试次数
    private Date retryTime; // 重试时间
    private Byte sendStatus; // 生产者发送状态，0默认初始化，1未成功，2已成功

    private Byte minioStatus; // minio状态，0初始化，1为还未成功，2已成功

    private Byte esStatus; // es状态，0初始化，1为还未成功，2已成功

    private String parameter;

    private Date createTime;

    private Date updateTime;
}
