package com.cqupt.mas.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author 唐海军
 * @create 2022-12-08 15:38
 */

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DicomFilePO {
    /**
     * 主键
     */
    @Id
    public String id;

    private String patientId;

    private String studyDate;

    private String studyInstanceUID;

    private String seriesInstanceUID;

    private String SOPInstanceUID;

    private String instanceNumber;

    private String patientName;

    private String modality;

    private String accessionNumber;

    private String patientSex;

    private String patientAge;

    private String seriesNumber;

    private String studyTime;

    /**
     * 文件名称
     */
    public String fileName;

    /**
     * 文件大小
     */
    public long fileSize;

    /**
     * 上传时间
     */
    public Date uploadDate;

    /**
     * MD5值
     */
    public String md5;


    /**
     * 文件类型
     */
    public String contentType;

    /**
     * 文件后缀名
     */
    public String suffix;


    /**
     * 大文件管理GridFS的ID
     */
    private String gridFsId;

    /**
     * 文件内容
     */
    private Binary content;
}
