package com.cqupt.mas.exception;

import com.cqupt.mas.constant.ErrorMessage;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LuCong
 * @since 2020-09-22
 **/
@RestControllerAdvice
@Order(Integer.MAX_VALUE - 1)
public class GlobalExceptionHandler {

    public GlobalExceptionHandler() {
    }

    @ExceptionHandler({ServiceException.class})
    @ResponseStatus(HttpStatus.OK)
    public ErrorMessage onServiceException(HttpServletRequest request, HttpServletResponse response, ServiceException exception) {

        return new ErrorMessage(exception.getCode(), exception.getMessage(), exception.getFields());
    }

    //由于还在开发阶段，所以不做全局异常处理，方便排错
//    @ExceptionHandler({Exception.class})
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ErrorMessage onUnknownException(HttpServletRequest request, HttpServletResponse response, Exception exception) {
//
//        return new ErrorMessage("1000", exception.getMessage());
//    }

}
