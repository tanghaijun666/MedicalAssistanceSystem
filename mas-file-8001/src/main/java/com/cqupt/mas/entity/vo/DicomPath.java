package com.cqupt.mas.entity.vo;

import lombok.Data;

/**
 * @author 唐海军
 * @create 2022-11-08 9:25
 */

@Data
public class DicomPath {
    String date;
    String studyInstanceUID;
    String SOPInstanceUID;
}
