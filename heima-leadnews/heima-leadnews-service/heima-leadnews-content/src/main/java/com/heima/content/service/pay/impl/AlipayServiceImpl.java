package com.heima.content.service.pay.impl;

import com.heima.content.service.pay.AlipayService;
import com.heima.content.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AlipayServiceImpl implements AlipayService {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Autowired
    private OrderService orderService;

    @Override
    public String generatePayPage(String orderNo, String subject, String amount) {
        // 支付宝沙箱支付 - 使用简化版支付页面
        // 实际生产环境应使用支付宝SDK生成签名后的支付链接
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>支付宝沙箱支付</title>");
        html.append("<style>body{font-family:Arial,sans-serif;display:flex;justify-content:center;align-items:center;min-height:100vh;background:#f5f5f5;margin:0}");
        html.append(".pay-box{background:#fff;padding:40px;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.1);text-align:center;max-width:400px;width:100%}");
        html.append(".pay-title{font-size:20px;color:#333;margin-bottom:8px}");
        html.append(".pay-subtitle{font-size:14px;color:#999;margin-bottom:24px}");
        html.append(".pay-amount{font-size:36px;color:#ff6b00;font-weight:bold;margin-bottom:24px}");
        html.append(".pay-btn{display:inline-block;padding:14px 48px;background:#1677ff;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;text-decoration:none}");
        html.append(".pay-btn:hover{background:#4096ff}");
        html.append(".pay-note{font-size:12px;color:#999;margin-top:16px}</style></head><body>");
        html.append("<div class='pay-box'>");
        html.append("<div class='pay-title'>").append(subject).append("</div>");
        html.append("<div class='pay-subtitle'>订单号: ").append(orderNo).append("</div>");
        html.append("<div class='pay-amount'>¥").append(amount).append("</div>");
        html.append("<div class='pay-subtitle' style='color:#ff9800;margin-bottom:16px'>【沙箱环境】</div>");
        html.append("<a class='pay-btn' href='javascript:void(0)' onclick='confirmPay()'>确认支付</a>");
        html.append("<div class='pay-note'>点击确认后将模拟支付成功</div>");
        html.append("</div>");
        html.append("<script>");
        html.append("function confirmPay() {");
        html.append("  var tradeNo = 'ALIPAY_SANDBOX_' + Date.now();");
        html.append("  fetch('/content/api/v1/course/pay/notify', {");
        html.append("    method: 'POST',");
        html.append("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        html.append("    body: 'out_trade_no=").append(orderNo).append("&trade_no=' + tradeNo + '&total_amount=").append(amount).append("&trade_status=TRADE_SUCCESS'");
        html.append("  }).then(function(r) { return r.text(); }).then(function() {");
        html.append("    window.location.href = '").append(returnUrl).append("?orderNo=").append(orderNo).append("&status=success';");
        html.append("  }).catch(function() {");
        html.append("    alert('支付通知失败，请稍后重试');");
        html.append("  });");
        html.append("}");
        html.append("</script></body></html>");

        return html.toString();
    }

    @Override
    public boolean handleNotify(String tradeNo, String orderNo, String totalAmount, String status) {
        if (!"TRADE_SUCCESS".equals(status)) {
            log.warn("支付状态非成功: {}", status);
            return false;
        }

        orderService.handlePaySuccess(orderNo, tradeNo);
        return true;
    }

    @Override
    public boolean verifySign(Map<String, String> params) {
        // 沙箱环境简化验证，生产环境需使用支付宝SDK验证签名
        return true;
    }
}