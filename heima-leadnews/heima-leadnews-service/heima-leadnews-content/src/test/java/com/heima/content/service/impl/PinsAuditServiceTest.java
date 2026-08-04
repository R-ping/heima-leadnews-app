package com.heima.content.service.impl;

import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.content.mapper.ApPinsMapper;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.audit.AuditContext;
import com.heima.model.audit.AuditEntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class PinsAuditServiceTest {

    @Mock
    private ApPinsMapper apPinsMapper;

    @Mock
    private GreenImageScanPlus greenImageScan;

    @InjectMocks
    private PinsAuditService pinsAuditService;

    private ApPins createPins(Long id, Byte status) {
        ApPins pins = new ApPins();
        pins.setId(id);
        pins.setAuthorId(100L);
        pins.setContent("测试沸点内容");
        pins.setStatus(status);
        pins.setCreatedTime(new Date());
        return pins;
    }

    @BeforeEach
    void setUp() {
        // 注入BailianAiService mock
        ReflectionTestUtils.setField(pinsAuditService, "bailianAiService", mock(com.heima.content.service.BailianAiService.class));
    }

    @Test
    void handlePassed_ShouldUpdateStatusToPublished() {
        Long pinsId = 1L;
        ApPins pins = createPins(pinsId, ApPins.Status.SUBMIT.getCode());
        when(apPinsMapper.selectById(pinsId)).thenReturn(pins);

        AuditContext context = new AuditContext(AuditEntityType.PINS, pinsId, 100L);
        pinsAuditService.handlePassed(context);

        assertEquals(ApPins.Status.PUBLISHED.getCode(), pins.getStatus().byteValue());
        assertNotNull(pins.getReviewTime());
        verify(apPinsMapper).updateById(pins);
    }

    @Test
    void handlePassed_ShouldLogWarning_WhenPinsNotFound() {
        when(apPinsMapper.selectById(anyLong())).thenReturn(null);

        AuditContext context = new AuditContext(AuditEntityType.PINS, 999L, 100L);
        pinsAuditService.handlePassed(context);

        verify(apPinsMapper, never()).updateById(Mockito.<ApPins>any());
    }

    @Test
    void handleFailed_ShouldUpdateStatusToFail() {
        Long pinsId = 1L;
        ApPins pins = createPins(pinsId, ApPins.Status.SUBMIT.getCode());
        when(apPinsMapper.selectById(pinsId)).thenReturn(pins);

        AuditContext context = new AuditContext(AuditEntityType.PINS, pinsId, 100L);
        pinsAuditService.handleFailed(context, "包含违规内容");

        assertEquals(ApPins.Status.FAIL.getCode(), pins.getStatus().byteValue());
        assertEquals("包含违规内容", pins.getReason());
        verify(apPinsMapper).updateById(pins);
    }

    @Test
    void handleFailed_ShouldLogWarning_WhenPinsNotFound() {
        when(apPinsMapper.selectById(anyLong())).thenReturn(null);

        AuditContext context = new AuditContext(AuditEntityType.PINS, 999L, 100L);
        pinsAuditService.handleFailed(context, "违规");

        verify(apPinsMapper, never()).updateById(Mockito.<ApPins>any());
    }

    @Test
    void audit_ShouldPass_WhenContentAndImagesAreValid() throws Exception {
        Long pinsId = 1L;
        ApPins pins = createPins(pinsId, ApPins.Status.SUBMIT.getCode());
        when(apPinsMapper.selectById(pinsId)).thenReturn(pins);

        AuditContext context = new AuditContext(AuditEntityType.PINS, pinsId, 100L)
            .withContent("正常沸点内容")
            .withImageUrls(java.util.Arrays.asList("http://example.com/pic.jpg"));

        com.heima.content.service.BailianAiService aiService =
            (com.heima.content.service.BailianAiService) ReflectionTestUtils.getField(pinsAuditService, "bailianAiService");
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("is_violation", false);
        when(aiService.checkViolation(anyLong(), any(), anyString())).thenReturn(aiResult);

        Map<String, Object> imageResult = new HashMap<>();
        imageResult.put("level", "pass");
        when(greenImageScan.imageScan(anyString())).thenReturn(imageResult);

        com.heima.model.audit.AuditResult result = pinsAuditService.audit(context);

        assertTrue(result.isPassed());
        assertEquals(ApPins.Status.PUBLISHED.getCode(), pins.getStatus().byteValue());
        assertNotNull(pins.getReviewTime());
    }

    @Test
    void audit_ShouldFail_WhenContentIsViolation() {
        Long pinsId = 1L;
        ApPins pins = createPins(pinsId, ApPins.Status.SUBMIT.getCode());
        when(apPinsMapper.selectById(pinsId)).thenReturn(pins);

        AuditContext context = new AuditContext(AuditEntityType.PINS, pinsId, 100L)
            .withContent("违规内容");

        com.heima.content.service.BailianAiService aiService =
            (com.heima.content.service.BailianAiService) ReflectionTestUtils.getField(pinsAuditService, "bailianAiService");
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("is_violation", true);
        aiResult.put("violation_type", "涉政");
        aiResult.put("violation_reason", "包含敏感词汇");
        when(aiService.checkViolation(anyLong(), any(), anyString())).thenReturn(aiResult);

        com.heima.model.audit.AuditResult result = pinsAuditService.audit(context);

        assertFalse(result.isPassed());
        assertEquals(ApPins.Status.FAIL.getCode(), pins.getStatus().byteValue());
        assertEquals("涉政: 包含敏感词汇", pins.getReason());
    }
}