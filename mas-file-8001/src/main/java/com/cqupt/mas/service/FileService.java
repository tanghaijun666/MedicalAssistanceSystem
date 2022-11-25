package com.cqupt.mas.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author 唐海军
 * @create 2022-11-07 16:09
 */

public interface FileService {

    void uploadDicomFile(MultipartFile file) throws IOException;

    HashMap<String, LinkedList<String>> dicomFilePath(String patienID);

    HashMap<String, String> getFileInfo(MultipartFile file, String attributes) throws IOException;

    public String[] getAllPatienId();
}
