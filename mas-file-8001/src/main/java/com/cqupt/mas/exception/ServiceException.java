package com.cqupt.mas.exception;


import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
    private String code;
    private String fields;

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public ServiceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String code, String message, String fields) {
        super(message);
        this.code = code;
        this.fields = fields;
    }

    public ServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ServiceException(String code, String message, String fields, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.fields = fields;
    }

    public ServiceException(String msg) {
        super(msg);
        this.code = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public String getCode() {
        return code;
    }

    public String getFields() {
        return fields;
    }
}
