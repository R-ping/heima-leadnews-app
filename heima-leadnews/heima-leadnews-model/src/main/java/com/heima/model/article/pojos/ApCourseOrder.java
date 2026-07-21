package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ap_course_order")
public class ApCourseOrder implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Integer userId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private Byte status;

    @TableField("pay_method")
    private String payMethod;

    @TableField("paid_at")
    private Date paidAt;

    @TableField("created_time")
    private Date createdTime;
}