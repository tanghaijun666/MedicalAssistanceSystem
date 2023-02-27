package com.cqupt.mas.entity.vo;

import cn.hutool.core.bean.BeanUtil;
import com.cqupt.mas.entity.po.DicomFilePO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Objects;

/**
 * @author lit@epsoft.com.cn
 * @version 1.0
 * @Description 统一文件下载vo
 * @date Apr 8, 2022
 */
@Data
public class FileExportVo {

    private String fileId;

    private String patientSex;

    private String studyDate;

    private String patientAge;

    private String seriesNumber;

    private String studyTime;

    private String patientName;

    private String patientId;

    private String instanceNumber;

    /**
     * 文件大小
     */
    public long fileSize;

    /**
     * 文件类型
     */
    public String contentType;

    @JsonIgnore
    private byte[] data;

    public FileExportVo(DicomFilePO dicomFilePO) {
        if (dicomFilePO != null) {
            BeanUtil.copyProperties(dicomFilePO, this);
            if (Objects.nonNull(dicomFilePO.getContent())) {
                this.data = dicomFilePO.getContent().getData();
            }
            this.fileId = dicomFilePO.getId();
        }
    }

}
