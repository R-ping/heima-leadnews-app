package com.heima.common.exception;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExceptionCatch 单元测试")
class ExceptionCatchTest {

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ExceptionCatch exceptionCatch;

    // ==================== exception (通用异常) ====================

    @Nested
    @DisplayName("通用异常处理测试")
    class GeneralExceptionTests {

        @Test
        @DisplayName("处理通用Exception - 返回SERVER_ERROR")
        void shouldHandleGeneralException() {
            Exception e = new RuntimeException("未知错误");

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
            assertEquals(AppHttpCodeEnum.SERVER_ERROR.getErrorMessage(), result.getMessage());
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("处理NullPointerException")
        void shouldHandleNullPointerException() {
            Exception e = new NullPointerException("空指针异常");

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== exception (CustomException) ====================

    @Nested
    @DisplayName("自定义异常处理测试")
    class CustomExceptionTests {

        @Test
        @DisplayName("处理CustomException - 登录错误返回401")
        void shouldHandleCustomExceptionLoginError() {
            CustomException e = new CustomException(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("处理CustomException - Token错误返回401")
        void shouldHandleCustomExceptionTokenError() {
            CustomException e = new CustomException(AppHttpCodeEnum.TOKEN_INVALID);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.TOKEN_INVALID.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("处理CustomException - 参数错误返回400")
        void shouldHandleCustomExceptionParamError() {
            CustomException e = new CustomException(AppHttpCodeEnum.PARAM_REQUIRE);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("处理CustomException - 数据不存在返回404")
        void shouldHandleCustomExceptionDataNotFound() {
            CustomException e = new CustomException(AppHttpCodeEnum.DATA_NOT_EXIST);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        @Test
        @DisplayName("处理CustomException - 权限错误返回403")
        void shouldHandleCustomExceptionForbidden() {
            CustomException e = new CustomException(AppHttpCodeEnum.NO_OPERATOR_AUTH);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.NO_OPERATOR_AUTH.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("处理CustomException - 社交登录错误返回400")
        void shouldHandleCustomExceptionSocialError() {
            CustomException e = new CustomException(AppHttpCodeEnum.SOCIAL_ALREADY_BOUND);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SOCIAL_ALREADY_BOUND.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("处理CustomException - 素材错误返回400")
        void shouldHandleCustomExceptionMaterialError() {
            CustomException e = new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        @Test
        @DisplayName("处理CustomException - 未知code范围返回500")
        void shouldHandleCustomExceptionUnknownCode() {
            // 使用一个code在映射范围之外的枚举值
            CustomException e = new CustomException(AppHttpCodeEnum.SUCCESS);

            ResponseResult result = exceptionCatch.exception(e, response);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}