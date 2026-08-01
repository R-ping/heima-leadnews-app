package com.heima.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.course.mapper.ApCourseDiscountMapper;
import com.heima.model.article.dtos.CourseDiscountDto;
import com.heima.model.article.pojos.ApCourseDiscount;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    @Mock
    private ApCourseDiscountMapper discountMapper;

    @InjectMocks
    private DiscountServiceImpl discountService;

    // ==================== createDiscount() tests ====================

    @Test
    void testCreateDiscountSuccess() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setCourseId(100L);
        dto.setDiscountType(1);
        dto.setDiscountValue(new BigDecimal("20.00"));
        dto.setCode("MYCODE");
        dto.setMaxUses(50);
        dto.setStartTime(new Date(System.currentTimeMillis() - 10000));
        dto.setEndTime(new Date(System.currentTimeMillis() + 100000));
        when(discountMapper.insert(any(ApCourseDiscount.class))).thenReturn(1);

        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        ApCourseDiscount discount = (ApCourseDiscount) result.getData();
        assertEquals("MYCODE", discount.getCode());
        assertEquals(1, discount.getDiscountType());
        assertEquals(new BigDecimal("20.00"), discount.getDiscountValue());
        assertEquals(50, discount.getMaxUses());
        assertEquals(0, discount.getUsedCount());
        assertEquals(1, discount.getStatus());
    }

    @Test
    void testCreateDiscountNullCourseId() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setDiscountType(1);
        dto.setDiscountValue(new BigDecimal("20.00"));
        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateDiscountNullDiscountType() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setCourseId(100L);
        dto.setDiscountValue(new BigDecimal("20.00"));
        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateDiscountNullDiscountValue() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setCourseId(100L);
        dto.setDiscountType(1);
        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateDiscountWithNullCodeGeneratesCode() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setCourseId(100L);
        dto.setDiscountType(1);
        dto.setDiscountValue(new BigDecimal("10.00"));
        dto.setCode(null);
        when(discountMapper.insert(any(ApCourseDiscount.class))).thenReturn(1);

        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(200, result.getCode());
        ApCourseDiscount discount = (ApCourseDiscount) result.getData();
        assertNotNull(discount.getCode());
        assertTrue(discount.getCode().startsWith("COURSE"));
    }

    @Test
    void testCreateDiscountWithNullMaxUses() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setCourseId(100L);
        dto.setDiscountType(1);
        dto.setDiscountValue(new BigDecimal("10.00"));
        dto.setMaxUses(null);
        when(discountMapper.insert(any(ApCourseDiscount.class))).thenReturn(1);

        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(200, result.getCode());
        ApCourseDiscount discount = (ApCourseDiscount) result.getData();
        assertEquals(100, discount.getMaxUses());
    }

    @Test
    void testCreateDiscountWithNullTimes() {
        CourseDiscountDto dto = new CourseDiscountDto();
        dto.setCourseId(100L);
        dto.setDiscountType(1);
        dto.setDiscountValue(new BigDecimal("10.00"));
        dto.setStartTime(null);
        dto.setEndTime(null);
        when(discountMapper.insert(any(ApCourseDiscount.class))).thenReturn(1);

        ResponseResult result = discountService.createDiscount(dto, 1L);
        assertEquals(200, result.getCode());
        ApCourseDiscount discount = (ApCourseDiscount) result.getData();
        assertNotNull(discount.getStartTime());
        assertNotNull(discount.getEndTime());
    }

    // ==================== listDiscounts() tests ====================

    @Test
    void testListDiscountsSuccess() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setId(1L);
        discount.setCourseId(100L);
        discount.setCode("CODE1");
        List<ApCourseDiscount> list = Collections.singletonList(discount);
        when(discountMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        ResponseResult result = discountService.listDiscounts(100L, 1L);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
    }

    @Test
    void testListDiscountsEmpty() {
        when(discountMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(null);
        ResponseResult result = discountService.listDiscounts(100L, 1L);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
    }

    // ==================== disableDiscount() tests ====================

    @Test
    void testDisableDiscountSuccess() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setId(1L);
        discount.setStatus(1);
        when(discountMapper.selectById(1L)).thenReturn(discount);

        ResponseResult result = discountService.disableDiscount(1L, 1L);
        assertEquals(200, result.getCode());
        verify(discountMapper).updateById(Mockito.<ApCourseDiscount>argThat(d -> d.getStatus() == 0));
    }

    @Test
    void testDisableDiscountNotFound() {
        when(discountMapper.selectById(999L)).thenReturn(null);
        ResponseResult result = discountService.disableDiscount(999L, 1L);
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
        verify(discountMapper, never()).updateById(any(ApCourseDiscount.class));
    }

    // ==================== validateDiscount() tests ====================

    @Test
    void testValidateDiscountNullCode() {
        ApCourseDiscount result = discountService.validateDiscount(null, 100L);
        assertNull(result);
    }

    @Test
    void testValidateDiscountEmptyCode() {
        ApCourseDiscount result = discountService.validateDiscount("", 100L);
        assertNull(result);
    }

    @Test
    void testValidateDiscountNotFound() {
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        ApCourseDiscount result = discountService.validateDiscount("NONE", 100L);
        assertNull(result);
    }

    @Test
    void testValidateDiscountCourseMismatch() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setCourseId(200L);
        discount.setStartTime(new Date(System.currentTimeMillis() - 10000));
        discount.setEndTime(new Date(System.currentTimeMillis() + 10000));
        discount.setUsedCount(0);
        discount.setMaxUses(100);
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(discount);

        ApCourseDiscount result = discountService.validateDiscount("CODE", 100L);
        assertNull(result);
    }

    @Test
    void testValidateDiscountExpired() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setCourseId(100L);
        discount.setStartTime(new Date(System.currentTimeMillis() + 100000));
        discount.setEndTime(new Date(System.currentTimeMillis() + 200000));
        discount.setUsedCount(0);
        discount.setMaxUses(100);
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(discount);

        ApCourseDiscount result = discountService.validateDiscount("CODE", 100L);
        assertNull(result);
    }

    @Test
    void testValidateDiscountUsedUp() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setCourseId(100L);
        discount.setStartTime(new Date(System.currentTimeMillis() - 10000));
        discount.setEndTime(new Date(System.currentTimeMillis() + 10000));
        discount.setUsedCount(100);
        discount.setMaxUses(100);
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(discount);

        ApCourseDiscount result = discountService.validateDiscount("CODE", 100L);
        assertNull(result);
    }

    @Test
    void testValidateDiscountSuccess() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setCourseId(100L);
        discount.setStartTime(new Date(System.currentTimeMillis() - 10000));
        discount.setEndTime(new Date(System.currentTimeMillis() + 10000));
        discount.setUsedCount(5);
        discount.setMaxUses(100);
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(discount);

        ApCourseDiscount result = discountService.validateDiscount("CODE", 100L);
        assertNotNull(result);
    }

    // ==================== getDiscountByCode() tests ====================

    @Test
    void testGetDiscountByCodeNull() {
        ApCourseDiscount result = discountService.getDiscountByCode(null);
        assertNull(result);
    }

    @Test
    void testGetDiscountByCodeEmpty() {
        ApCourseDiscount result = discountService.getDiscountByCode("");
        assertNull(result);
    }

    @Test
    void testGetDiscountByCodeSuccess() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setCode("CODE");
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(discount);

        ApCourseDiscount result = discountService.getDiscountByCode("CODE");
        assertNotNull(result);
        assertEquals("CODE", result.getCode());
    }

    // ==================== validateDiscountForPreview() tests ====================

    @Test
    void testValidateDiscountForPreviewInvalid() {
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        ResponseResult result = discountService.validateDiscountForPreview("INVALID", 100L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testValidateDiscountForPreviewSuccess() {
        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setCourseId(100L);
        discount.setCode("CODE");
        discount.setDiscountType(1);
        discount.setDiscountValue(new BigDecimal("20.00"));
        discount.setStartTime(new Date(System.currentTimeMillis() - 10000));
        discount.setEndTime(new Date(System.currentTimeMillis() + 10000));
        discount.setUsedCount(0);
        discount.setMaxUses(100);
        when(discountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(discount);

        ResponseResult result = discountService.validateDiscountForPreview("CODE", 100L);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(1, data.get("discountType"));
        assertEquals(new BigDecimal("20.00"), data.get("discountValue"));
        assertEquals("CODE", data.get("code"));
    }
}