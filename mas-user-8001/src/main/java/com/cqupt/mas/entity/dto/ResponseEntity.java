package com.cqupt.mas.entity.dto;

import com.cqupt.mas.constant.ResponseStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据传输对象（DTO）(Data Transfer Object)
 * DTO 是一种设计模式之间传输数据的软件应用系统。
 * 数据传输目标往往是数据访问对象从数据库中检索数据。
 * 数据传输对象与数据交互对象或数据访问对象之间的差异是一个以不具有任何行为除了存储和检索的数据（访问和存取器）。
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
public class ResponseEntity<T> {
    @ApiModelProperty("0:成功; 1:参数错误; 2:没有登录; 3:文件传输数据错误; 4:第三方服务错误;  5:服务器运行错误;  6:用户操作错误")
    private int status;

    @ApiModelProperty("提示信息")
    private String msg;

    @ApiModelProperty("业务数据")
    private T data;

    public ResponseEntity(T data) {
        this(ResponseStatus.SUCCESS_RESPONSE, null, data);
    }
}
