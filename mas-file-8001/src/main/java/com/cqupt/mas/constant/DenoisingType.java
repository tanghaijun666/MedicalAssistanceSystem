package com.cqupt.mas.constant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author 唐海军
 * @create 2023-01-07 14:57
 */

@ApiModel("去噪类型")
public class DenoisingType {

    @ApiModelProperty("高斯滤波")
    public static final Integer GAUSSIAN_FILTER = 1;
     @ApiModelProperty("均值滤波")
    public static final Integer AVERAGE_FILTER = 2;
     @ApiModelProperty("中值滤波")
    public static final Integer MEDIAN_FILTER = 3;

}
