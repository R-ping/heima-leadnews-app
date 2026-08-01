package com.heima.article.service;

import java.util.Map;

public interface AlipayService {

    /** 生成支付页面HTML */
    String generatePayPage(String orderNo, String subject, String amount);

    /** 处理支付异步通知 */
    boolean handleNotify(String tradeNo, String orderNo, String totalAmount, String status);

    /** 验证签名 */
    boolean verifySign(Map<String, String> params);
}