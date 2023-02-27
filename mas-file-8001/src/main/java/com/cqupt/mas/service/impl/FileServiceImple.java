package com.cqupt.mas.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.cqupt.mas.entity.po.DicomFilePO;
import com.cqupt.mas.entity.vo.FileExportVo;
import com.cqupt.mas.entity.vo.MainShow;
import com.cqupt.mas.repository.DicomFilePORepository;
import com.cqupt.mas.service.FileService;
import com.cqupt.mas.utils.DenoisingUtil;
import com.cqupt.mas.utils.DisplayTagUtil;
import com.cqupt.mas.utils.MD5Util;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 唐海军
 * @create 2022-11-07 16:09
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileServiceImple implements FileService {

    private final DicomFilePORepository dicomFilePORepository;
    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    /**
     * 多文件上传
     *
     * @param files
     * @return
     */
    @Override
    public List<FileExportVo> uploadFiles(List<MultipartFile> files) {

        return files.stream().map(file -> {
            try {
                return this.uploadFile(file);
            } catch (Exception e) {
                log.error("文件上传失败", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 文件上传
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public FileExportVo uploadFile(MultipartFile file) throws Exception {
        if (file.getSize() > 16777216) {
            return this.saveGridFsFile(file);
        } else {
            return this.saveBinaryFile(file);
        }
    }

    /**
     * 文件下载
     *
     * @param fileId
     * @return
     */
    @Override
    public FileExportVo downloadFile(String fileId) {
        Optional<DicomFilePO> option = this.getBinaryFileById(fileId);

        if (option.isPresent()) {
            DicomFilePO dicomFilePO = option.get();
            if (Objects.isNull(dicomFilePO.getContent())) {
                option = this.getGridFsFileById(fileId);
            }
        }

        return option.map(FileExportVo::new).orElse(null);
    }

    public List<FileExportVo> getFilesBySeriesInstanceUID(String seriesInstanceUID) {
        Criteria criteria = new Criteria();
        criteria.and("seriesInstanceUID").is(seriesInstanceUID);
        Query query = new Query(criteria);
        List<DicomFilePO> dicomFilePOS = mongoTemplate.find(query, DicomFilePO.class);
        if (dicomFilePOS.isEmpty()) {
            return null;
        }
        List<FileExportVo> collect = dicomFilePOS.stream().map(FileExportVo::new).collect(Collectors.toList());
        return collect;
    }


    public String getFileIdBySeriesInstanceUIDAndInstanceNumber(String seriesInstanceUID, String instanceNumber) {
        Criteria criteria = new Criteria();
        criteria.and("seriesInstanceUID").is(seriesInstanceUID);
        criteria.and("instanceNumber").is(instanceNumber);
        Query query = new Query(criteria);
        query.fields().include("_id");
        DicomFilePO one = mongoTemplate.findOne(query, DicomFilePO.class);
        if (one == null) {
            return null;
        }
        return one.getId();
    }

    @Override
    public List<String> getInstanceNumbers(String seriesInstanceUID) {
        Criteria criteria = new Criteria();
        criteria.and("seriesInstanceUID").is(seriesInstanceUID);
        Query query = new Query(criteria);
        query.fields().include("instanceNumber");
        List<DicomFilePO> dicomFilePOS = mongoTemplate.find(query, DicomFilePO.class);
        return dicomFilePOS.stream().map(n -> n.getInstanceNumber()).collect(Collectors.toList());
    }

    /**
     * 文件删除
     *
     * @param fileId
     */
    @Override
    public void removeFile(String fileId) {
        Optional<DicomFilePO> option = this.getBinaryFileById(fileId);

        if (option.isPresent()) {
            if (Objects.nonNull(option.get().getGridFsId())) {
                this.removeGridFsFile(fileId);
            } else {
                this.removeBinaryFile(fileId);
            }
        }
    }

    /**
     * 删除Binary文件
     *
     * @param fileId
     */
    public void removeBinaryFile(String fileId) {
        dicomFilePORepository.deleteById(fileId);
    }

    /**
     * 删除GridFs文件
     *
     * @param fileId
     */
    public void removeGridFsFile(String fileId) {
        // TODO 根据id查询文件
        DicomFilePO dicomFilePO = mongoTemplate.findById(fileId, DicomFilePO.class);
        if (Objects.nonNull(dicomFilePO)) {
            // TODO 根据文件ID删除fs.files和fs.chunks中的记录
            Query deleteFileQuery = new Query().addCriteria(Criteria.where("filename").is(dicomFilePO.getGridFsId()));
            gridFsTemplate.delete(deleteFileQuery);
            // TODO 删除集合dicomFilePO中的数据
            Query deleteQuery = new Query(Criteria.where("id").is(fileId));
            mongoTemplate.remove(deleteQuery, DicomFilePO.class);
        }
    }

    /**
     * 保存Binary文件（小文件）
     *
     * @param file
     * @return
     * @throws Exception
     */
    public FileExportVo saveBinaryFile(MultipartFile file) throws Exception {

        String suffix = getFileSuffix(file);
        DicomFilePO dicomFilePO1 = this.toDicomFile(file);
        if (this.getFileIdBySeriesInstanceUIDAndInstanceNumber(dicomFilePO1.getSeriesInstanceUID(), dicomFilePO1.getInstanceNumber()) == null) {
            dicomFilePORepository.save(dicomFilePO1);
        }
        return new FileExportVo(dicomFilePO1);
    }

    /**
     * 保存GridFs文件（大文件）
     *
     * @param file
     * @return
     * @throws Exception
     */
    public FileExportVo saveGridFsFile(MultipartFile file) throws Exception {
        String suffix = getFileSuffix(file);

        String gridFsId = this.storeFileToGridFS(file.getInputStream(), file.getContentType());

        DicomFilePO dicomFilePO = this.toDicomFile(file);
        if (this.getFileIdBySeriesInstanceUIDAndInstanceNumber(dicomFilePO.getSeriesInstanceUID(), dicomFilePO.getInstanceNumber()) == null) {
            dicomFilePO = mongoTemplate.save(this.toDicomFile(file));
        }
        return new FileExportVo(dicomFilePO);
    }

    /**
     * 上传文件到Mongodb的GridFs中
     *
     * @param in
     * @param contentType
     * @return
     */
    public String storeFileToGridFS(InputStream in, String contentType) {
        String gridFsId = IdUtil.simpleUUID();
        // TODO 将文件存储进GridFS中
        gridFsTemplate.store(in, gridFsId, contentType);
        return gridFsId;
    }

    /**
     * 获取Binary文件
     *
     * @param id
     * @return
     */
    public Optional<DicomFilePO> getBinaryFileById(String id) {
        return dicomFilePORepository.findById(id);
    }

    /**
     * 获取Grid文件
     *
     * @param id
     * @return
     */
    public Optional<DicomFilePO> getGridFsFileById(String id) {
        DicomFilePO dicomFilePO = mongoTemplate.findById(id, DicomFilePO.class);
        if (Objects.nonNull(dicomFilePO)) {
            Query gridQuery = new Query().addCriteria(Criteria.where("filename").is(dicomFilePO.getGridFsId()));
            try {
                // TODO 根据id查询文件
                GridFSFile fsFile = gridFsTemplate.findOne(gridQuery);
                // TODO 打开流下载对象
                GridFSDownloadStream in = gridFSBucket.openDownloadStream(fsFile.getObjectId());
                if (in.getGridFSFile().getLength() > 0) {
                    // TODO 获取流对象
                    GridFsResource resource = new GridFsResource(fsFile, in);
                    // TODO 获取数据
                    dicomFilePO.setContent(new Binary(IoUtil.readBytes(resource.getInputStream())));
                    return Optional.of(dicomFilePO);
                } else {
                    return Optional.empty();
                }
            } catch (IOException e) {
                log.error("获取MongoDB大文件失败", e);
            }
        }

        return Optional.empty();
    }

    /**
     * 获取文件后缀
     *
     * @param file
     * @return
     */
    private String getFileSuffix(MultipartFile file) {
        String suffix = "";
        if (Objects.requireNonNull(file.getOriginalFilename()).contains(".")) {
            suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        }
        return suffix;
    }


    public DicomFilePO toDicomFile(MultipartFile file) throws Exception {
        Attributes attrs = DisplayTagUtil.loadDicomObject(file.getInputStream());
        String patientId = DisplayTagUtil.getAttribute(attrs, Tag.PatientID);
        String studyDate = DisplayTagUtil.getAttribute(attrs, Tag.StudyDate);
        String studyInstanceUID = DisplayTagUtil.getAttribute(attrs, Tag.StudyInstanceUID);
        String seriesInstanceUID = DisplayTagUtil.getAttribute(attrs, Tag.SeriesInstanceUID);
        String SOPInstanceUID = DisplayTagUtil.getAttribute(attrs, Tag.SOPInstanceUID);
        String instanceNumber = DisplayTagUtil.getAttribute(attrs, Tag.InstanceNumber).trim();
        String patientName = DisplayTagUtil.getAttribute(attrs, Tag.PatientName);
        String modality = DisplayTagUtil.getAttribute(attrs, Tag.Modality);
        String accessionNumber = DisplayTagUtil.getAttribute(attrs, Tag.AccessionNumber);
        String patientSex = DisplayTagUtil.getAttribute(attrs, Tag.PatientSex);
        String patientAge = DisplayTagUtil.getAttribute(attrs, Tag.PatientAge);
        String seriesNumber = DisplayTagUtil.getAttribute(attrs, Tag.SeriesNumber).trim();
        String studyTime = DisplayTagUtil.getAttribute(attrs, Tag.StudyTime);


        String suffix = getFileSuffix(file);
        DicomFilePO dicomFilePO = DicomFilePO.builder()
                .fileSize(file.getSize())
                .content(new Binary(file.getBytes()))
                .contentType(file.getContentType())
                .uploadDate(new Date())
                .suffix(suffix)
                .md5(MD5Util.getMD5(file.getInputStream()))
                .studyDate(studyDate)
                .patientId(patientId)
                .studyInstanceUID(studyInstanceUID)
                .seriesInstanceUID(seriesInstanceUID)
                .instanceNumber(instanceNumber)
                .fileName(file.getOriginalFilename())
                .patientName(patientName)
                .modality(modality)
                .accessionNumber(accessionNumber)
                .patientAge(patientAge)
                .studyTime(studyTime)
                .patientSex(patientSex)
                .seriesNumber(seriesNumber)
                .SOPInstanceUID(SOPInstanceUID)
                .build();
        return dicomFilePO;
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


    @Override
    public Set<MainShow> getMainShow() {
        Query query = new Query();
        query.fields().include("patientName", "studyDate", "patientId",
                "modality", "seriesInstanceUID", "accessionNumber");
        List<DicomFilePO> list = mongoTemplate.find(query, DicomFilePO.class);
        Set<MainShow> collect = list.stream().map(n -> {
            return MainShow.builder()
                    .accessionNumber(n.getAccessionNumber())
                    .modality(n.getModality())
                    .patientName(n.getPatientName())
                    .studyDate(n.getStudyDate())
                    .patientId(n.getPatientId())
                    .seriesInstanceUID(n.getSeriesInstanceUID())
                    .build();
        }).collect(Collectors.toSet());
        return collect;
    }


    @Override
    public FileExportVo getDicomFileBySeriesInstanceUIDAndInstanceNumber(String seriesInstanceUID, String instanceNumber) {

        String fileId = this.getFileIdBySeriesInstanceUIDAndInstanceNumber(seriesInstanceUID, instanceNumber);
        if (fileId == null) {
            return null;
        }
        return this.downloadFile(this.getFileIdBySeriesInstanceUIDAndInstanceNumber(seriesInstanceUID, instanceNumber));
    }

    @Override
    public FileExportVo getDenoisingFileBySeriesInstanceUIDAndInstanceNumber(String seriesInstanceUID, String instanceNumber, Integer type) {
        //存储到数据库的有bug，seriesInstanceUID后面会多一个空格，所以加上"\u0000"
        FileExportVo resultfile = this.getDicomFileBySeriesInstanceUIDAndInstanceNumber(seriesInstanceUID + type.toString() + "\u0000", instanceNumber);
        if (resultfile != null) {
            return resultfile;
        }

        DicomFilePO one = dicomFilePORepository
                .findById(this.getFileIdBySeriesInstanceUIDAndInstanceNumber(seriesInstanceUID, instanceNumber))
                .get();
        String dcmPath = null;
        try {
            dcmPath = DenoisingUtil.denoisingTool(one, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dcmPath == null) {
            return null;
        }
        File file = new File(dcmPath);
        MockMultipartFile mockMultipartFile = null;
        try {
            mockMultipartFile = new MockMultipartFile(file.getName(), new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileExportVo fileExportVo = null;
        if (file.exists()) {
            file.delete();
        }
        try {
            fileExportVo = this.uploadFile(mockMultipartFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileExportVo;
    }
}