package com.heima.model.wemedia.dtos;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class WmNewsDto {
    
    private Integer id;
     /**
     * 标题
     */
    private String title;
     /**
     * 频道id
     */
    private Integer channelId;
     /**
     * 标签
     */
    private String labels;
     /**
     * 发布时间
     */
    private Date publishTime;
     /**
     * 文章内容
     */
    private String content;
     /**
     * 文章封面类型  1 无图 2 单图
     */
    private Short type;
     /**
     * 提交时间
     */
    private Date submitedTime; 
     /**
     * 状态 提交为1
     */
    private byte status;
     
     /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 上下架 0 下架  1 上架
     */
    private Short enable;
    private List<ContPic> pics;
    @Data
    public static class ContPic{
        private String picUri;
        private String picUrl;
    }
}