package com.cqupt.mas.service.impl;

import com.cqupt.mas.service.FileService;
import com.cqupt.mas.utils.DisplayTagUtil;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author 唐海军
 * @create 2022-11-07 16:09
 */

@Service
@Slf4j
public class FileServiceImple implements FileService {

    @Value("${dicom.file.location}")
    private String dicomFileLocation;

    @Override
    public void uploadDicomFile(MultipartFile file) throws IOException {
        Attributes attrs = null;
        attrs = DisplayTagUtil.loadDicomObject(file.getInputStream());
        String patientId = DisplayTagUtil.getAttribute(attrs, Tag.PatientID);
        String date = DisplayTagUtil.getAttribute(attrs, Tag.StudyDate);
        String studyInstanceUID = DisplayTagUtil.getAttribute(attrs, Tag.StudyInstanceUID);
        String seriesInstanceUID = DisplayTagUtil.getAttribute(attrs, Tag.SeriesInstanceUID);
//        String SOPInstanceUID = DisplayTagUtil.getAttribute(attrs, Tag.SOPInstanceUID);
        String InstanceNumber = DisplayTagUtil.getAttribute(attrs, Tag.InstanceNumber);
        String filePath = dicomFileLocation + "/" + patientId + "/" + date + "/" + studyInstanceUID + "/" + seriesInstanceUID;
        String fileName = InstanceNumber + ".dcm";
        String absoluteFilePath = filePath + "/" + fileName;
        createFile(filePath, fileName);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(file.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(absoluteFilePath));

            //循环的读取文件，并写入到 destFilePath
            byte[] buff = new byte[10241];
            int readLen = 0;
            //当返回 -1 时，就表示文件读取完毕
            while ((readLen = bis.read(buff)) != -1) {
                bos.write(buff, 0, readLen);
            }

        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public HashMap<String, LinkedList<String>> dicomFilePath(String patienID) {
        File file = new File(dicomFileLocation + "/" + patienID);
        LinkedList<String> fileName = new LinkedList<>();
        getAllFiles(file, fileName);
        HashMap<String, LinkedList<String>> map = new HashMap<>();
        for (int i = 0; i < fileName.size(); i++) {
            //路径中的uid都是六十四位，文件名（包括点4位），分隔符一位
            Integer seriesInstanceUIDStart = fileName.get(i).length() - 64 - 4 - 1 - 64;
            String seriesInstanceUID = fileName.get(i).substring(seriesInstanceUIDStart, seriesInstanceUIDStart + 64);
            if (map.containsKey(seriesInstanceUID)) {
                map.get(seriesInstanceUID).add(fileName.get(i));
            } else {
                LinkedList<String> temp = new LinkedList<>();
                temp.add(fileName.get(i));
                map.put(seriesInstanceUID, temp);
            }
        }
        return map;
    }

    @Override
    public HashMap<String, String> getFileInfo(MultipartFile file, String attributes) throws IOException {
        String[] split = attributes.split(";");
        HashMap<String, String> map = new HashMap<>();
        Class<?> tagClass = null;
        try {
            tagClass = Class.forName("org.dcm4che3.data.Tag");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Attributes attributes1 = DisplayTagUtil.loadDicomObject(file.getInputStream());
        for (String s : split) {
            Field declaredField = null;
            try {
                declaredField = tagClass.getDeclaredField(s);
            } catch (NoSuchFieldException e) {
                map.put(s, "不存在该属性");
                continue;
            }
            try {
                map.put(s, DisplayTagUtil.getAttribute(attributes1, (Integer) declaredField.get(null)));
            } catch (IllegalAccessException e) {
                map.put(s, "");
            }
        }

        return map;
    }

    public void getAllFiles(File file, LinkedList<String> fileName) {
        File[] files = file.listFiles();
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                String result = files[i].isFile() ? "一个文件" : "一个目录";
                if ("一个目录".equals(result)) {
                    getAllFiles(files[i], fileName);
                } else {
                    fileName.add(files[i].getAbsolutePath());
                }
            }
        }
    }

    public void createFile(String filePath, String fileName) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.mkdirs();
            }
            File f1 = new File(filePath + "/" + fileName);
            if (!f1.exists()) {
                f1.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getAllPatienId() {
        File file = new File(dicomFileLocation);
        String[] list = file.list();
        return list;
    }
}

