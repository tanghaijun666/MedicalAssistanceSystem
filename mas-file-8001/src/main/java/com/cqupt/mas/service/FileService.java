package com.cqupt.mas.service;

import com.cqupt.mas.entity.po.LablePO;
import com.cqupt.mas.entity.vo.FileExportVo;
import com.cqupt.mas.entity.vo.MainShow;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author 唐海军
 * @create 2022-11-07 16:09
 */

public interface FileService {

    /**
     * 标签保存
     */
    public Boolean saveLabele(LablePO lable) throws Exception;

    /**
     * 文件上传
     */
    FileExportVo uploadFile(MultipartFile file) throws Exception;


    void uploadDicomFile(MultipartFile file) throws IOException;

    HashMap<String, LinkedList<String>> dicomFilePath(String patienID);

    /**
     * 多文件上传
     */
    List<FileExportVo> uploadFiles(List<MultipartFile> files);

    /**
     * 文件下载
     */
    FileExportVo downloadFile(String fileId);

    /**
     * 文件删除
     */
    void removeFile(String fileId);

    HashMap<String, String> getFileInfo(MultipartFile file, String attributes) throws IOException;

    Set<MainShow> getMainShow();

    List<FileExportVo> getFilesBySeriesInstanceUID(String seriesInstanceUID);

    List<String> getInstanceNumbers(String seriesInstanceUID);

    FileExportVo getDicomFileBySeriesInstanceUIDAndInstanceNumber(String seriesInstanceUID, String instanceNumber);

    FileExportVo getDenoisingFileBySeriesInstanceUIDAndInstanceNumber(String seriesInstanceUID, String instanceNumber, Integer type);
}
