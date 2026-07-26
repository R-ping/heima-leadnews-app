package com.heima.common.bailian;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class DashScopeClient {

    @Value("${bailian.dashscope.api-key}")
    private String apiKey;

    @Value("${bailian.dashscope.api-host:https://dashscope.aliyuncs.com}")
    private String apiHost;

    @Value("${bailian.dashscope.model:qwen-plus}")
    private String model;

    @Value("${bailian.dashscope.embedding-model:text-embedding-v2}")
    private String embeddingModel;

    @Value("${bailian.dashscope.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${bailian.dashscope.timeout.read:30000}")
    private int readTimeout;

    @Value("${bailian.dashscope.retry.max-attempts:3}")
    private int maxRetries;

    @Value("${bailian.dashscope.retry.backoff-delay:1000}")
    private long backoffDelay;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            Constants.apiKey = apiKey;
            log.info("DashScope API Key configured successfully");
        } else {
            log.warn("DASH_SCOPE_API_KEY environment variable is not set. AI analysis will be disabled.");
        }
    }

    /**
     * 调用大模型进行文本生成
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return 模型响应文本
     */
    public String callGeneration(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DashScope API Key not configured, skipping AI call");
            return null;
        }

        long startTime = System.currentTimeMillis();
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Generation gen = new Generation();
                
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(systemPrompt)
                        .build();
                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(userMessage)
                        .build();

                GenerationParam param = GenerationParam.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .temperature(0.3f)
                        .build();

                GenerationResult result = gen.call(param);
                String response = result.getOutput().getChoices().get(0).getMessage().getContent();
                
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("DashScope API call succeeded, attempt={}, elapsed={}ms", attempt, elapsed);
                return response;

            } catch (ApiException e) {
                lastException = e;
                String errorMsg = e.getMessage();
                log.warn("DashScope API error, attempt={}/{}, message={}", attempt, maxRetries, errorMsg);
                // 对于网络错误和服务端错误，继续重试
                if (errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("limit"))) {
                    log.warn("DashScope API rate limited, will retry");
                }
            } catch (NoApiKeyException | InputRequiredException e) {
                log.error("DashScope API configuration error: {}", e.getMessage());
                break;
            }

            if (attempt < maxRetries) {
                long delay = backoffDelay * (long) Math.pow(2, attempt - 1);
                try {
                    log.info("Retrying after {}ms...", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.error("DashScope API call failed after {} attempts, elapsed={}ms", maxRetries, elapsed, lastException);
        return null;
    }

    /**
     * 调用文本向量模型生成向量嵌入
     * @param text 文本内容
     * @return 向量嵌入（double数组）
     */
    public double[] callEmbedding(String text) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DashScope API Key not configured, skipping embedding call");
            return null;
        }

        long startTime = System.currentTimeMillis();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                TextEmbedding embedding = new TextEmbedding();
                
                TextEmbeddingParam param = TextEmbeddingParam.builder()
                        .apiKey(apiKey)
                        .model(embeddingModel)
                        .texts(Collections.singletonList(text))
                        .build();

                TextEmbeddingResult result = embedding.call(param);
                List<Double> embeddingList = result.getOutput().getEmbeddings().get(0).getEmbedding();
                double[] vector = embeddingList.stream().mapToDouble(Double::doubleValue).toArray();
                
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("DashScope embedding call succeeded, attempt={}, dimension={}, elapsed={}ms", 
                        attempt, vector.length, elapsed);
                return vector;

            } catch (Exception e) {
                log.warn("DashScope embedding API error, attempt={}/{}, message={}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    long delay = backoffDelay * (long) Math.pow(2, attempt - 1);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.error("DashScope embedding call failed after {} attempts, elapsed={}ms", maxRetries, elapsed);
        return null;
    }
}