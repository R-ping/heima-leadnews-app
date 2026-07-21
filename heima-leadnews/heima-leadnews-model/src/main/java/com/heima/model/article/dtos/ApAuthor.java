package com.heima.model.article.dtos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * APP文章作者信息表
 * @TableName ap_author
 */
@TableName(value ="ap_author")
@Data
public class ApAuthor {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 作者名称（用户昵称）
     */
    private String name;

    /**
     * 
            1 签约合作商
            2 平台自媒体人
            
     */
    private Integer type;

    /**
     * 社交账号ID
     */
    private Integer userId;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 自媒体账号
     */
    private Integer wmUserId;
}