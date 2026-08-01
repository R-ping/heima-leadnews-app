package com.heima.course.controller.v1;

import com.heima.course.service.DiscountService;
import com.heima.model.article.dtos.CourseDiscountDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountControllerTest {

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private DiscountController discountController;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== createDiscount() tests ====================

    @Test
    void testCreateDiscountSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        CourseDiscountDto dto = new CourseDiscountDto();
        when(discountService.createDiscount(dto, 1L)).thenReturn(ResponseResult.okResult("success"));

        ResponseResult result = discountController.createDiscount(dto);
        assertEquals(200, result.getCode());
    }

    @Test
    void testCreateDiscountNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        CourseDiscountDto dto = new CourseDiscountDto();

        ResponseResult result = discountController.createDiscount(dto);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(discountService, never()).createDiscount(any(), any());
    }

    // ==================== listDiscounts() tests ====================

    @Test
    void testListDiscountsSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(discountService.listDiscounts(100L, 1L)).thenReturn(ResponseResult.okResult("list"));

        ResponseResult result = discountController.listDiscounts(100L);
        assertEquals(200, result.getCode());
    }

    @Test
    void testListDiscountsNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = discountController.listDiscounts(100L);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(discountService, never()).listDiscounts(any(), any());
    }

    // ==================== disableDiscount() tests ====================

    @Test
    void testDisableDiscountSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        Map<String, Object> params = new HashMap<>();
        params.put("discountId", "1");
        when(discountService.disableDiscount(1L, 1L)).thenReturn(ResponseResult.okResult("disabled"));

        ResponseResult result = discountController.disableDiscount(params);
        assertEquals(200, result.getCode());
    }

    @Test
    void testDisableDiscountNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        Map<String, Object> params = new HashMap<>();
        params.put("discountId", "1");

        ResponseResult result = discountController.disableDiscount(params);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(discountService, never()).disableDiscount(any(), any());
    }

    @Test
    void testDisableDiscountWithoutId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        Map<String, Object> params = new HashMap<>();
        when(discountService.disableDiscount(null, 1L)).thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = discountController.disableDiscount(params);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== validateDiscount() tests ====================

    @Test
    void testValidateDiscountSuccess() {
        when(discountService.validateDiscountForPreview("CODE", 100L))
                .thenReturn(ResponseResult.okResult("valid"));

        ResponseResult result = discountController.validateDiscount("CODE", 100L);
        assertEquals(200, result.getCode());
    }

    @Test
    void testValidateDiscountInvalid() {
        when(discountService.validateDiscountForPreview("INVALID", 100L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "折扣码无效或已过期"));

        ResponseResult result = discountController.validateDiscount("INVALID", 100L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }
}