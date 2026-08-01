package com.heima.course.controller.v1;

import com.heima.course.service.SettlementService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementControllerTest {

    @Mock
    private SettlementService settlementService;

    @InjectMocks
    private SettlementController settlementController;

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

    // ==================== getMonthlyList() tests ====================

    @Test
    void testGetMonthlyListSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(settlementService.getMonthlyList(1L)).thenReturn(ResponseResult.okResult("list"));

        ResponseResult result = settlementController.getMonthlyList();
        assertEquals(200, result.getCode());
    }

    @Test
    void testGetMonthlyListNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = settlementController.getMonthlyList();
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(settlementService, never()).getMonthlyList(any());
    }

    // ==================== getSettlementDetail() tests ====================

    @Test
    void testGetSettlementDetailSuccess() {
        when(settlementService.getSettlementDetail(1L)).thenReturn(ResponseResult.okResult("detail"));

        ResponseResult result = settlementController.getSettlementDetail(1L);
        assertEquals(200, result.getCode());
    }

    @Test
    void testGetSettlementDetailNotFound() {
        when(settlementService.getSettlementDetail(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = settlementController.getSettlementDetail(999L);
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }
}