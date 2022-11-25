package com.cqupt.mas.constant;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 响应状态码
 * <p>
 * 0:成功; 1:参数错误; 2:没有登录; 3:文件传输数据错误; 4:第三方服务错误;  5:服务器运行错误;  6:用户操作错误
 */
@ApiModel("响应信息状态码")
public class ResponseStatus {

    @ApiModelProperty("参数错误")
    public static final int PARAMETER_INVALIATE = 1;
    @ApiModelProperty("文化转换失败")
    public static final int FILE_TRANSFER_FIAL = 3;
    @ApiModelProperty("服务器错误")
    public static final int SERVER_EXCUTE_FAIL = 5;
    @ApiModelProperty("用户操作错误")
    public static final int USER_OPERATION_ERROR = 6;
    @ApiModelProperty("成功响应状态码")
    public static final int SUCCESS_RESPONSE = 200;
    @ApiModelProperty("失败响应状态码")
    public static final int FAILURE_RESPONSE = 1000;
}
