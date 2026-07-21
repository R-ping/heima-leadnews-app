package com.heima.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.*;
import com.aliyun.teaopenapi.models.Config;

public class DescribeImageModerationResult {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Config config = new Config();
        /**
         * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
         * 常见获取环境变量方式：
         * 方式一：
         *     获取RAM用户AccessKey ID：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         * 方式二：
         *     获取RAM用户AccessKey ID：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         */
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        //接入区域和地址请根据实际情况修改
        config.setRegionId("cn-shanghai");
        config.setEndpoint("green-cip.cn-shanghai.aliyuncs.com");
        //连接时超时时间，单位毫秒（ms）。
        config.setReadTimeout(6000);
        //读取时超时时间，单位毫秒（ms）。
        config.setConnectTimeout(3000);

        Client client = new Client(config);
        DescribeImageModerationResultRequest describeImageModerationResultRequest = new DescribeImageModerationResultRequest();
        // 提交任务时返回的reqId
        describeImageModerationResultRequest.setReqId("50FDD548-0DB6-557A-B602-AEBEBBD64AED");

        try {
            DescribeImageModerationResultResponse response = client.describeImageModerationResult(describeImageModerationResultRequest);
            if (response.getStatusCode() == 200) {
                DescribeImageModerationResultResponseBody result = response.getBody();
                System.out.println("requestId=" + result.getRequestId());
                System.out.println("code=" + result.getCode());
                System.out.println("msg=" + result.getMsg());
                if (200 == result.getCode()) {
                    DescribeImageModerationResultResponseBody.DescribeImageModerationResultResponseBodyData data = result.getData();
                    System.out.println("data = " + JSON.toJSONString(data,true));
                } else {
                    System.out.println("image async moderation result not success. code:" + result.getCode());
                }
            } else {
                System.out.println("response not success. status:" + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("cost time:" + (System.currentTimeMillis() - start));
    }
}