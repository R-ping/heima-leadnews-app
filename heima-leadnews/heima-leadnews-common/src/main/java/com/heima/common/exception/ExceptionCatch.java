package com.heima.common.exception;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice  //控制器增强类
@Slf4j
public class ExceptionCatch {

    /**
     * 处理不可控异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception e, HttpServletResponse response){
        log.error("catch exception:", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }

    /**
     * 处理可控异常  自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult exception(CustomException e, HttpServletResponse response){
        log.error("catch custom exception:", e);
        response.setStatus(mapHttpStatus(e.getAppHttpCodeEnum()));
        return ResponseResult.errorResult(e.getAppHttpCodeEnum());
    }

    /**
     * 将 AppHttpCodeEnum 映射为 HTTP 状态码
     */
    private int mapHttpStatus(AppHttpCodeEnum appHttpCodeEnum) {
        if (appHttpCodeEnum == null) {
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        int code = appHttpCodeEnum.getCode();
        if (code >= 1 && code <= 50) {
            return HttpServletResponse.SC_UNAUTHORIZED; // 401
        }
        if (code >= 50 && code <= 100) {
            return HttpServletResponse.SC_UNAUTHORIZED; // 401
        }
        if (code >= 500 && code <= 1000) {
            return HttpServletResponse.SC_BAD_REQUEST; // 400
        }
        if (code == 1001 || code == 1002) {
            return HttpServletResponse.SC_NOT_FOUND; // 404
        }
        if (code >= 3000 && code <= 3500) {
            return HttpServletResponse.SC_FORBIDDEN; // 403
        }
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
    }
}
