package com.heima.model.notification.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("system_notifications")
public class SystemNotification implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long notificationId;     // 关联notifications表
    private String content;
    private String actionUrl;
    private LocalDateTime createdAt;
}