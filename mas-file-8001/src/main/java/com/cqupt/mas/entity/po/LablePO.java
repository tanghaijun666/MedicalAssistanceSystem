package com.cqupt.mas.entity.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 唐海军
 * @create 2023-03-13 10:38
 */

@ApiModel
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document
public class LablePO {

    @ApiModelProperty("序号")
    private int num;
    @ApiModelProperty("影像编号")
    private int imageNum;
    @ApiModelProperty("面积")
    private double area;
    @ApiModelProperty("平均CT值")
    private double averageCT;
    @ApiModelProperty("结节ID")
    private int noduleId;
    @ApiModelProperty("密度")
    private String density;
    @ApiModelProperty("表征")
    private String representation;
    @ApiModelProperty("恶性程度")
    private String gradeMalignancy;
    @ApiModelProperty("位置")
    private String location;


}
