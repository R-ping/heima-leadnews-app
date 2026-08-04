package com.heima.content.service.impl;

import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.content.service.BailianAiService;
import com.heima.model.audit.AuditContext;
import com.heima.model.audit.AuditEntityType;
import com.heima.model.audit.AuditResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractAuditServiceTest {

    @Mock
    private BailianAiService bailianAiService;

    @Mock
    private GreenImageScanPlus greenImageScan;

    private TestAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new TestAuditService();
        auditService.setBailianAiService(bailianAiService);
        auditService.setGreenImageScan(greenImageScan);
    }

    @AfterEach
    void tearDown() {
        // 重置测试服务的状态标志
        auditService.handlePassedCalled = false;
        auditService.handleFailedCalled = false;
        auditService.failedReason = null;
    }

    @Test
    void audit_ShouldFail_WhenNullContext() {
        AuditResult result = auditService.audit(null);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("审核参数不完整"));
        assertFalse(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldFail_WhenNullEntityId() {
        AuditContext context = new AuditContext(AuditEntityType.PINS, null, 1L);
        AuditResult result = auditService.audit(context);
        assertFalse(result.isPassed());
        assertTrue(result.getReason().contains("审核参数不完整"));
        assertFalse(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldPass_WhenNoContentAndNoImages() {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L);

        AuditResult result = auditService.audit(context);

        assertTrue(result.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldPass_WhenContentPassesViolationCheck() {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withContent("正常内容");

        Map<String, Object> result = new HashMap<>();
        result.put("is_violation", false);
        when(bailianAiService.checkViolation(anyLong(), any(), anyString())).thenReturn(result);

        AuditResult auditResult = auditService.audit(context);

        assertTrue(auditResult.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
        verify(bailianAiService).checkViolation(1L, null, "正常内容");
    }

    @Test
    void audit_ShouldFail_WhenContentFailsViolationCheck() {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withContent("违规内容");

        Map<String, Object> result = new HashMap<>();
        result.put("is_violation", true);
        result.put("violation_type", "涉政");
        result.put("violation_reason", "包含敏感词汇");
        when(bailianAiService.checkViolation(anyLong(), any(), anyString())).thenReturn(result);

        AuditResult auditResult = auditService.audit(context);

        assertFalse(auditResult.isPassed());
        assertTrue(auditResult.getReason().contains("敏感词汇"));
        assertTrue(auditService.handleFailedCalled);
        assertEquals("涉政: 包含敏感词汇", auditService.failedReason);
        assertFalse(auditService.handlePassedCalled);
    }

    @Test
    void audit_ShouldPassDegrade_WhenViolationCheckThrowsException() {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withContent("正常内容");

        when(bailianAiService.checkViolation(anyLong(), any(), anyString()))
            .thenThrow(new RuntimeException("AI服务异常"));

        AuditResult auditResult = auditService.audit(context);

        // 异常降级为通过，继续执行后续步骤
        assertTrue(auditResult.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldFail_WhenImageScanReturnsHigh() throws Exception {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("http://example.com/image.jpg"));

        Map<String, Object> scanResult = new HashMap<>();
        scanResult.put("level", "high");
        when(greenImageScan.imageScan(anyString())).thenReturn(scanResult);

        AuditResult auditResult = auditService.audit(context);

        assertFalse(auditResult.isPassed());
        assertTrue(auditResult.getViolationType().contains("图片违规"));
        assertTrue(auditResult.getReason().contains("图片存在违规内容"));
        verify(greenImageScan).imageScan(anyString());
        assertFalse(auditService.handlePassedCalled);
    }

    @Test
    void audit_ShouldFail_WhenImageScanReturnsMedium() throws Exception {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("http://example.com/image.jpg"));

        Map<String, Object> scanResult = new HashMap<>();
        scanResult.put("level", "medium");
        when(greenImageScan.imageScan(anyString())).thenReturn(scanResult);

        AuditResult auditResult = auditService.audit(context);

        assertFalse(auditResult.isPassed());
        assertTrue(auditResult.getViolationType().contains("图片违规"));
        assertTrue(auditResult.getReason().contains("图片存在不确定内容"));
        verify(greenImageScan).imageScan(anyString());
        assertFalse(auditService.handlePassedCalled);
    }

    @Test
    void audit_ShouldPass_WhenImageScanReturnsPass() throws Exception {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("http://example.com/image.jpg"));

        Map<String, Object> scanResult = new HashMap<>();
        scanResult.put("level", "pass");
        when(greenImageScan.imageScan(anyString())).thenReturn(scanResult);

        AuditResult auditResult = auditService.audit(context);

        assertTrue(auditResult.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldPass_WhenImageScanReturnsNull() throws Exception {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("http://example.com/image.jpg"));

        when(greenImageScan.imageScan(anyString())).thenReturn(null);

        AuditResult auditResult = auditService.audit(context);

        assertTrue(auditResult.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldFail_WhenImageScanThrowsException() throws Exception {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("http://example.com/image.jpg"));

        when(greenImageScan.imageScan(anyString()))
            .thenThrow(new RuntimeException("图片审核异常"));

        AuditResult auditResult = auditService.audit(context);

        assertFalse(auditResult.isPassed());
        assertTrue(auditResult.getReason().contains("图片审核异常"));
    }

    @Test
    void audit_ShouldSkipImageScan_WhenGreenImageScanIsNull() {
        auditService.setGreenImageScan(null);

        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("http://example.com/image.jpg"));

        AuditResult auditResult = auditService.audit(context);

        assertTrue(auditResult.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    @Test
    void audit_ShouldSkipEmptyImageUrl() {
        AuditContext context = new AuditContext(AuditEntityType.PINS, 1L, 1L)
            .withImageUrls(Arrays.asList("", null));

        AuditResult auditResult = auditService.audit(context);

        assertTrue(auditResult.isPassed());
        assertTrue(auditService.handlePassedCalled);
        assertFalse(auditService.handleFailedCalled);
    }

    /**
     * 测试用的抽象审核服务实现
     */
    static class TestAuditService extends AbstractAuditService {
        boolean handlePassedCalled = false;
        boolean handleFailedCalled = false;
        String failedReason;

        @Override
        protected void handlePassed(AuditContext context) {
            handlePassedCalled = true;
        }

        @Override
        protected void handleFailed(AuditContext context, String reason) {
            handleFailedCalled = true;
            this.failedReason = reason;
        }

        void setBailianAiService(BailianAiService service) {
            try {
                var field = AbstractAuditService.class.getDeclaredField("bailianAiService");
                field.setAccessible(true);
                field.set(this, service);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void setGreenImageScan(GreenImageScanPlus scan) {
            try {
                var field = AbstractAuditService.class.getDeclaredField("greenImageScan");
                field.setAccessible(true);
                field.set(this, scan);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}