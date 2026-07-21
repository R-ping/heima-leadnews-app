package com.heima.user.controller.v1;

import cn.hutool.core.util.RandomUtil;
import com.heima.common.redis.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wechat/gzh/using")
@Slf4j
public class WechatGZHLogin {

    private static final String TOKEN = "huhudong";
    @Autowired
    private CacheService cacheService;


    @GetMapping
    public String auth(@RequestParam("signature") String signature, @RequestParam("timestamp") String timestamp,
        @RequestParam("nonce") String nonce, @RequestParam("echostr") String echostr) {
        log.info("========== 收到微信公众号GET验证请求 ==========");
        log.info("signature: {}, timestamp: {}, nonce: {}, echostr: {}", signature, timestamp, nonce, echostr);
        boolean auth = preAuth(signature, timestamp, nonce, echostr);
        if (!auth) {
            log.warn("微信公众号验证失败，拒绝访问");
            return ""; // 验证失败时返回空字符串
        }
        return echostr;
    }

    // 当普通微信用户向公众账号发消息时，微信服务器将POST消息的XML数据包到开发者填写的URL上。
    @PostMapping
    public String using(HttpServletRequest request) {
        String xmlData = readXmlFromRequest(request);
        log.info("========== 收到微信公众号POST请求 ==========");
        log.info("请求内容: {}", xmlData);
        if (xmlData == null || xmlData.trim().isEmpty()) {
            log.warn("收到空的微信消息");
            return "success";// 代表接收到消息，但不回复
        }
        WechatMessageDto message = parseXmlToMessage(xmlData);
        if (message != null && "text".equals(message.getMsgType())) {
            String content = message.getContent().trim();
            if ("登录".equals(content) || "登陆".equals(content)) {
                try {
                    // 6位随机数字
                    String token = RandomUtil.randomNumbers(6);
                    String redisKey = "wechat:token:" + message.getFromUserName();
                    cacheService.setEx(redisKey, token, 5, TimeUnit.MINUTES);
                    log.info("用户 {} 在公众号，生成token: {}", message.getFromUserName(), token);
                    String backXml = buildTextMessage(message.getFromUserName(), message.getToUserName(),
                        "您的token为: " + token + "\n有效期: 5分钟");
                    // GET https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=xxx&lang=zh_CN
                    // 生成token返回给用户，与此同时，请求获取用户信息
                    // response
                    // {
                    //    "subscribe": 1,
                    //    "openid": "xxxxx",
                    //    "nickname": "",
                    //    "sex": 0,
                    //    "language": "zh_CN",
                    //    "city": "",
                    //    "province": "",
                    //    "country": "",
                    //    "headimgurl": "",
                    //    "subscribe_time": 1780754379,
                    //    "remark": "",
                    //    "groupid": 0,
                    //    "tagid_list": [],
                    //    "subscribe_scene": "ADD_SCENE_QR_CODE",
                    //    "qr_scene": 0,
                    //    "qr_scene_str": ""
                    //}
                    log.info("返回给用户: {}", backXml);
                    return backXml;
                } catch (Exception e) {
                    log.error("处理微信消息失败", e);
                    return "success";
                }
            }
        }
        return "success";

    }

    private WechatMessageDto parseXmlToMessage(String xmlData) {
        try {
            WechatMessageDto dto = new WechatMessageDto();
            dto.setToUserName(extractXmlValue(xmlData, "ToUserName"));
            dto.setFromUserName(extractXmlValue(xmlData, "FromUserName"));
            dto.setCreateTime(extractXmlValue(xmlData, "CreateTime"));
            dto.setMsgType(extractXmlValue(xmlData, "MsgType"));
            dto.setContent(extractXmlValue(xmlData, "Content"));
            dto.setMsgId(extractXmlValue(xmlData, "MsgId"));
            return dto;
        } catch (Exception e) {
            log.error("解析XML消息失败", e);
            return null;
        }
    }

    private String extractXmlValue(String xml, String tagName) {
        int startTag = xml.indexOf("<" + tagName + ">");
        int endTag = xml.indexOf("</" + tagName + ">");

        if (startTag != -1 && endTag != -1) {
            // 提取标签内容，包含CDATA标记
            String value = xml.substring(startTag + tagName.length() + 2, endTag).trim();
            // 去除CDATA标签: <![CDATA[...]]>
            if (value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
                value = value.substring(9, value.length() - 3);
            }
            return value;
        }
        return null;
    }

    // 注意：回复消息时ToUserName和FromUserName需要互换
    private String buildTextMessage(String toUser, String fromUser, String content) {
        long createTime = System.currentTimeMillis() / 1000;

        return String.format(
            "<xml>" +
                "<ToUserName><![CDATA[%s]]></ToUserName>" +
                "<FromUserName><![CDATA[%s]]></FromUserName>" +
                "<CreateTime>%d</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[%s]]></Content>" +
                "</xml>",
            toUser, fromUser, createTime, content
        );
    }


    private static boolean preAuth(String signature, String timestamp, String nonce, String echostr) {
        // 微信公众号服务器配置验证：先进行参数校验
        if (signature == null || timestamp == null || nonce == null || echostr == null) {
            log.warn("微信公众号验证失败：参数不能为空");
            return false;
        }
        log.info("开始公众号登录验证");
        // 按照微信官方文档：将token、timestamp、nonce三个参数进行字典序排序后拼接成一个字符串
        String[] params = {TOKEN, timestamp, nonce};
        Arrays.sort(params);
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
            sb.append(param);
        }
        // 对拼接后的字符串进行sha1加密
        String hash = DigestUtils.sha1Hex(sb.toString());
        // 使用恒定时间比较，防止时序攻击
        if (!MessageDigest.isEqual(hash.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            log.warn("微信公众号验证失败：签名不匹配");
            return false;
        }
        log.info("微信公众号验证成功");
        return true;
    }

    /**
     * 从HttpServletRequest中读取XML数据 类似JavaScript中的raw-body，直接读取原始请求体
     */
    private String readXmlFromRequest(HttpServletRequest request) {
        StringBuilder xmlData = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                xmlData.append(line);
            }
        } catch (IOException e) {
            log.error("读取微信XML消息失败", e);
            return null;
        }
        return xmlData.toString();
    }


    @Data
    static class WechatMessageDto {

        private String ToUserName;
        private String FromUserName;
        private String CreateTime;
        private String MsgType;
        private String Content;
        private String MsgId;
    }
}
