package com.heima.apis.article.fallback;

import com.heima.apis.article.ILevelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ILevelClientFallback implements ILevelClient {

    @Override
    public Map<String, Object> getUserLevelInfo(Long userId) {
        log.warn("等级服务不可用，userId={}", userId);
        return null;
    }
}