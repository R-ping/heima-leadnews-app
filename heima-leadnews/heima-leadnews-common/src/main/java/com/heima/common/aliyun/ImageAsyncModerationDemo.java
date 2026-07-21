package com.heima.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.ImageAsyncModerationRequest;
import com.aliyun.green20220302.models.ImageAsyncModerationResponse;
import com.aliyun.green20220302.models.ImageAsyncModerationResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImageAsyncModerationDemo {


    public static Client createClient(String accessKeyId, String accessKeySecret, String endpoint) throws Exception {
        Config config = new Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        config.setEndpoint(endpoint);
        return new Client(config);
    }

    public static ImageAsyncModerationResponse invokeFunction(String accessKeyId, String accessKeySecret, String
            endpoint) throws Exception {
        Client client = createClient(accessKeyId, accessKeySecret, endpoint);
        RuntimeOptions runtime = new RuntimeOptions();
        Map<String, String> serviceParameters = new HashMap<>();
        serviceParameters.put("imageUrl", "http://47.104.68.187:9000/leadnews/Snipaste_2025-06-10_09-50-26.jpg");
        serviceParameters.put("dataId", UUID.randomUUID().toString());
        ImageAsyncModerationRequest imageAsyncModerationRequest = new ImageAsyncModerationRequest();
        imageAsyncModerationRequest.setService("baselineCheck");
        imageAsyncModerationRequest.setServiceParameters(JSON.toJSONString(serviceParameters));
        ImageAsyncModerationResponse response = null;
        try {
            response = client.imageAsyncModerationWithOptions(imageAsyncModerationRequest, runtime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
//        String accessKeyId = "建议从环境变量中获取RAM用户AccessKey ID";
//        String accessKeySecret = "建议从环境变量中获取RAM用户AccessKey Secret";
        ImageAsyncModerationResponse response = invokeFunction(accessKeyId, accessKeySecret, "green-cip.cn-shanghai.aliyuncs.com");
        if (response != null) {
            if (response.getStatusCode() == 200) {
                ImageAsyncModerationResponseBody body = response.getBody();
                System.out.println("requestId=" + body.getRequestId());
                System.out.println("code=" + body.getCode());
                System.out.println("msg=" + body.getMsg());
                if (body.getCode() == 200) {
                    ImageAsyncModerationResponseBody.ImageAsyncModerationResponseBodyData data = body.getData();
                    System.out.println("dataId=" + data.getDataId());
                    System.out.println("requestId = [" + data.getReqId() + "]");
                } else {
                    System.out.println("image asyncmoderation not success. code:" + body.getCode());
                }
            } else {
                System.out.println("response not success. status:" + response.getStatusCode());
            }
        }
        System.out.println("cost time:" + (System.currentTimeMillis() - start));

    }

}