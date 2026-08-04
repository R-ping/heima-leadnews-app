package com.heima.content.service.impl;

import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.content.mapper.ApColumnMapper;
import com.heima.model.article.pojos.ApColumn;
import com.heima.model.audit.AuditContext;
import com.heima.model.audit.AuditEntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class ColumnAuditServiceTest {

    @Mock
    private ApColumnMapper apColumnMapper;

    @Mock
    private GreenImageScanPlus greenImageScan;

    @InjectMocks
    private ColumnAuditService columnAuditService;

    private ApColumn createColumn(Long id, Byte status) {
        ApColumn column = new ApColumn();
        column.setId(id);
        column.setAuthorId(100L);
        column.setTitle("测试专栏");
        column.setDescription("专栏简介");
        column.setStatus(status);
        return column;
    }

    @BeforeEach
    void setUp() {
        // 注入BailianAiService mock
        ReflectionTestUtils.setField(columnAuditService, "bailianAiService", mock(com.heima.content.service.BailianAiService.class));
    }

    @Test
    void handlePassed_ShouldUpdateStatusToPublished() {
        Long columnId = 1L;
        ApColumn column = createColumn(columnId, ApColumn.Status.SUBMIT.getCode());
        when(apColumnMapper.selectById(columnId)).thenReturn(column);

        AuditContext context = new AuditContext(AuditEntityType.COLUMN, columnId, 100L);
        columnAuditService.handlePassed(context);

        assertEquals(ApColumn.Status.PUBLISHED.getCode(), column.getStatus().byteValue());
        verify(apColumnMapper).updateById(column);
    }

    @Test
    void handlePassed_ShouldLogWarning_WhenColumnNotFound() {
        when(apColumnMapper.selectById(anyLong())).thenReturn(null);

        AuditContext context = new AuditContext(AuditEntityType.COLUMN, 999L, 100L);
        columnAuditService.handlePassed(context);

        verify(apColumnMapper, never()).updateById(Mockito.<ApColumn>any());
    }

    @Test
    void handleFailed_ShouldUpdateStatusToFail() {
        Long columnId = 1L;
        ApColumn column = createColumn(columnId, ApColumn.Status.SUBMIT.getCode());
        when(apColumnMapper.selectById(columnId)).thenReturn(column);

        AuditContext context = new AuditContext(AuditEntityType.COLUMN, columnId, 100L);
        columnAuditService.handleFailed(context, "标题包含违规内容");

        assertEquals(ApColumn.Status.FAIL.getCode(), column.getStatus().byteValue());
        verify(apColumnMapper).updateById(column);
    }

    @Test
    void handleFailed_ShouldLogWarning_WhenColumnNotFound() {
        when(apColumnMapper.selectById(anyLong())).thenReturn(null);

        AuditContext context = new AuditContext(AuditEntityType.COLUMN, 999L, 100L);
        columnAuditService.handleFailed(context, "违规");

        verify(apColumnMapper, never()).updateById(Mockito.<ApColumn>any());
    }

    @Test
    void audit_ShouldPass_WhenTitleAndDescriptionAreValid() throws Exception {
        Long columnId = 1L;
        ApColumn column = createColumn(columnId, ApColumn.Status.SUBMIT.getCode());
        when(apColumnMapper.selectById(columnId)).thenReturn(column);

        AuditContext context = new AuditContext(AuditEntityType.COLUMN, columnId, 100L)
            .withTitle("测试专栏")
            .withContent("专栏简介内容")
            .withImageUrl("http://example.com/cover.jpg");

        com.heima.content.service.BailianAiService aiService =
            (com.heima.content.service.BailianAiService) ReflectionTestUtils.getField(columnAuditService, "bailianAiService");
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("is_violation", false);
        when(aiService.checkViolation(anyLong(), any(), anyString())).thenReturn(aiResult);

        Map<String, Object> imageResult = new HashMap<>();
        imageResult.put("level", "pass");
        when(greenImageScan.imageScan(anyString())).thenReturn(imageResult);

        com.heima.model.audit.AuditResult result = columnAuditService.audit(context);

        assertTrue(result.isPassed());
        assertEquals(ApColumn.Status.PUBLISHED.getCode(), column.getStatus().byteValue());
    }

    @Test
    void audit_ShouldFail_WhenTitleIsViolation() {
        Long columnId = 1L;
        ApColumn column = createColumn(columnId, ApColumn.Status.SUBMIT.getCode());
        when(apColumnMapper.selectById(columnId)).thenReturn(column);

        AuditContext context = new AuditContext(AuditEntityType.COLUMN, columnId, 100L)
            .withTitle("违规标题")
            .withContent("专栏简介内容");

        com.heima.content.service.BailianAiService aiService =
            (com.heima.content.service.BailianAiService) ReflectionTestUtils.getField(columnAuditService, "bailianAiService");
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("is_violation", true);
        aiResult.put("violation_type", "违规");
        aiResult.put("violation_reason", "标题包含敏感词");
        when(aiService.checkViolation(anyLong(), any(), anyString())).thenReturn(aiResult);

        com.heima.model.audit.AuditResult result = columnAuditService.audit(context);

        assertFalse(result.isPassed());
        assertEquals(ApColumn.Status.FAIL.getCode(), column.getStatus().byteValue());
    }
}