package com.heima.article.controller.v1;

import com.heima.article.service.AlipayService;
import com.heima.article.service.OrderService;
import com.heima.model.article.pojos.ApCourseOrder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course/pay")
public class PayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    /** 发起支付 - 返回支付页面 */
    @GetMapping(value = "/page", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String payPage(@RequestParam String orderNo) {
        ApCourseOrder order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return "<html><body><h2>订单不存在</h2></body></html>";
        }

        if (order.getStatus() != ApCourseOrder.Status.PENDING.getCode()) {
            return "<html><body><h2>订单状态异常</h2></body></html>";
        }

        String subject = "课程购买 - " + order.getCourseId();
        return alipayService.generatePayPage(orderNo, subject, order.getPaidAmount().toString());
    }

    /** 支付异步通知 */
    @PostMapping("/notify")
    @ResponseBody
    public String payNotify(HttpServletRequest request) {
        String tradeNo = request.getParameter("trade_no");
        String orderNo = request.getParameter("out_trade_no");
        String totalAmount = request.getParameter("total_amount");
        String status = request.getParameter("trade_status");

        boolean success = alipayService.handleNotify(tradeNo, orderNo, totalAmount, status);
        return success ? "success" : "fail";
    }
}