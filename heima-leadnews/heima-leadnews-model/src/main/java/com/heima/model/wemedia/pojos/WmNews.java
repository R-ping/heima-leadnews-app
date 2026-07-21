package com.heima.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.Getter;
import org.apache.ibatis.type.Alias;

/**
 * <p>
 * 自媒体图文内容信息表
 * </p>
 *
 * @author itheima
 */
@Data
@TableName("wm_news")
public class WmNews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 自媒体用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 作者名称
     */
    @TableField("author_name")
    private String authorName;

    /**
     * 作者头像
     */
    @TableField("author_image")
    private String authorImage;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 图文内容
     */
    @TableField("content")
    private String content;

    /**
     *
        1 无图文章
        2 单图文章
     */
    @TableField("type")
    private Byte type;

    /**
     * 图文频道ID
     */
    @TableField("channel_id")
    private Integer channelId;

    @TableField("labels")
    private String labels;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 提交时间
     */
    @TableField("submited_time")
    private Date submitedTime;

    /**
     * 当前状态
         1 审核中
         2 审核失败
         9 已发布
     */
    @TableField("status")
    private Byte status;

    /**
     * 定时发布时间，不定时则为空
     */
    @TableField("publish_time")
    private Date publishTime;

    /**
     * 拒绝理由
     */
    @TableField("reason")
    private String reason;

    /**
     * 发布库文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 封面图片
     */
    @TableField("cover_image")
    private String coverImage;

    @TableField("enable")
    private Byte enable;
    private String contPics;


     //状态枚举类
    @Getter
    @Alias("WmNewsStatus")
    public enum Status{
        NORMAL((byte)0),SUBMIT((byte)1),FAIL((byte)2),
         SUCCESS((byte)8),PUBLISHED((byte)9);
        final byte code;
        Status(byte code){
            this.code = code;
        }
     }


}