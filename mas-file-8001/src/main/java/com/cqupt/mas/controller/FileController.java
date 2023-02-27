package com.cqupt.mas.controller;

import com.cqupt.mas.constant.ResponseStatus;
import com.cqupt.mas.entity.dto.ResponseEntity;
import com.cqupt.mas.entity.vo.FileExportVo;
import com.cqupt.mas.entity.vo.MainShow;
import com.cqupt.mas.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author 唐海军
 * @create 2022-11-07 17:45
 */

@RequestMapping("file")
@RestController
@Api(tags = "文件模块")
@Slf4j
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/uploadDicomFile")
    @ApiOperation("上传dicom文件")
    @ApiImplicitParam(name = "file", value = "需要上传的dicom文件,可以是多个文件", dataType = ".dcm")
    public ResponseEntity uploadDicomFile(@RequestParam("file") MultipartFile[] files) {
        try {
            for (MultipartFile file : files) {
                fileService.uploadFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(ResponseStatus.FAILURE_RESPONSE, "上传失败，请检查文件是否正确", null);
        }
        return new ResponseEntity(ResponseStatus.SUCCESS_RESPONSE, "上传成功！！！", null);
    }

// 想一次返回该系列的所有文件，但还没有实现。
//    @GetMapping("/getFilesBySeriesInstanceUID")
//    @ApiOperation("通过序列uid获取文件 ")
//    @ApiImplicitParam(name = "seriesInstanceUID", value = "序列uid", dataType = "String")
//    public ResponseEntity<List<FileExportVo>> getFilesBySeriesInstanceUID(@RequestParam("seriesInstanceUID") String seriesInstanceUID) {
//        List<FileExportVo> filesBySeriesInstanceUID = fileService.getFilesBySeriesInstanceUID(seriesInstanceUID);
//        return new ResponseEntity<>(ResponseStatus.SUCCESS_RESPONSE, "成功！！！", filesBySeriesInstanceUID);
//    }


    @GetMapping("/getInstanceNumbers")
    @ApiOperation("获取一个序列dicom所有文件序列（如1,2,3,4")
    public ResponseEntity<List<String>> getInstanceNumbers(@RequestParam("seriesInstanceUID") String seriesInstanceUID) {
        List<String> instanceNumbers = fileService.getInstanceNumbers(seriesInstanceUID);
        return new ResponseEntity<List<String>>(ResponseStatus.SUCCESS_RESPONSE, "成功！", instanceNumbers);
    }

    @GetMapping("/getDicomFileBySeriesInstanceUIDAndInstanceNumber")
    @ApiOperation("通过序列号（InstanceNumber）和序列uid（SeriesInstanceUID）获取文件")
    public org.springframework.http.ResponseEntity<Object> getDicomFileBySeriesInstanceUIDAndInstanceNumber(
            @RequestParam("seriesInstanceUID") String seriesInstanceUID,
            @RequestParam("instanceNumber") String instanceNumber) {

//        //这行代码是因为seriesInstanceUID的bug，去噪之后存数据库的值后面有"\u0000"
//        if (seriesInstanceUID.substring(seriesInstanceUID.length()-1,seriesInstanceUID.length()).equals(" ")){
//            seriesInstanceUID=seriesInstanceUID.substring(0,seriesInstanceUID.length()-1)+"\u0000";
//        }
        FileExportVo fileExportVo = fileService.getDicomFileBySeriesInstanceUIDAndInstanceNumber(
                seriesInstanceUID, instanceNumber
        );

        if (Objects.nonNull(fileExportVo)) {
            return org.springframework.http.ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"" + fileExportVo.getInstanceNumber() + ".dcm\"")
                    .header(HttpHeaders.CONTENT_TYPE, fileExportVo.getContentType())
                    .header(HttpHeaders.CONTENT_LENGTH, fileExportVo.getFileSize() + "").header("Connection", "close")
                    .body(fileExportVo.getData());
        } else {
            return org.springframework.http.ResponseEntity.status(HttpStatus.NOT_FOUND).body("file does not exist");
        }
    }


    @GetMapping("/getDenoisingFileBySeriesInstanceUIDAndInstanceNumber")
    @ApiOperation("通过序列号（InstanceNumber）和序列uid（SeriesInstanceUID）和去噪类型（高斯：1；均值：2；中值：3）获取文件")
    public org.springframework.http.ResponseEntity<Object> getDenoisingFileBySeriesInstanceUIDAndInstanceNumber(
            @RequestParam("seriesInstanceUID") String seriesInstanceUID,
            @RequestParam("instanceNumber") String instanceNumber,
            @RequestParam("type") Integer type) {
        FileExportVo fileExportVo = fileService.getDenoisingFileBySeriesInstanceUIDAndInstanceNumber(
                seriesInstanceUID, instanceNumber, type
        );

        if (Objects.nonNull(fileExportVo)) {
            return org.springframework.http.ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"" + fileExportVo.getInstanceNumber() + ".dcm\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .header(HttpHeaders.CONTENT_LENGTH, fileExportVo.getFileSize() + "").header("Connection", "close")
                    .body(fileExportVo.getData());
        } else {
            return org.springframework.http.ResponseEntity.status(HttpStatus.NOT_FOUND).body("file does not exist");
        }
    }


    @PostMapping("/getDicomFileInfo")
    @ApiOperation("获取文件信息，同一系列信息基本一样")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "需要上传的dicom文件,同一序列图除了" +
                    "SOPInstanceUID不一样，其余数据基本一样，没有查找到的会返回空字符串,属性名不存在会返回‘不存在该属性’",
                    dataType = "org.springframework.web.multipart.MultipartFile"),
            @ApiImplicitParam(name = "attributes", value = "需要用的属性值，不同属性之间用英文的分号" +
                    "隔开如：PatientID;PatientAge;PatientAddress   返回属性值的顺序随机，和参数定义的顺序不一样",
                    dataType = "String")
    })
    public ResponseEntity<HashMap<String, String>> getDicomFileInfo(
            @RequestBody MultipartFile file, @RequestParam("attributes") String attributes) {
        HashMap<String, String> fileInfo = null;
        try {
            fileInfo = fileService.getFileInfo(file, attributes);
        } catch (Exception e) {
            return new ResponseEntity(ResponseStatus.FAILURE_RESPONSE, "失败，请检查文件是否正确", null);
        }
        return new ResponseEntity<>(ResponseStatus.SUCCESS_RESPONSE, "成功！", fileInfo);
    }

    @GetMapping("/testConnect")
    @ApiOperation("用来测试是否可以成功与服务器连接，无特殊作用")
    public ResponseEntity testConnect() {
        return new ResponseEntity<>(ResponseStatus.SUCCESS_RESPONSE, "连接成功！", null);
    }

    @PostMapping("/getMainShow")
    @ApiOperation("展示界面需要的信息，包含所有文件的病人Id，病人名字等信息")
    public ResponseEntity<Set<MainShow>> getMainShow() {
        Set<MainShow> mainShow = fileService.getMainShow();
        return new ResponseEntity(ResponseStatus.SUCCESS_RESPONSE, "成功", mainShow);
    }


}
