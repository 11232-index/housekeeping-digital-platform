package com.smart.handler;

import com.smart.constant.MessageConstant;
import com.smart.exception.BaseException;
import com.smart.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获sql异常信息
     * @param ex
     * @return
     */

    @ExceptionHandler
// username已存在异常捕获
    public Result sqlExceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        // 打印message
        log.error("异常信息：{}", message);
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            return Result.error("账号" + username + MessageConstant.AREADY_EXIST);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
