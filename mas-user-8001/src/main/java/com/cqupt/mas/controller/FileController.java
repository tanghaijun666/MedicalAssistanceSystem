package com.cqupt.mas.controller;

import com.cqupt.mas.constant.ResponseStatus;
import com.cqupt.mas.entity.dto.ResponseEntity;
import com.cqupt.mas.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

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
                fileService.uploadDicomFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(ResponseStatus.FAILURE_RESPONSE, "上传失败，请检查文件是否正确", null);
        }
        return new ResponseEntity(ResponseStatus.SUCCESS_RESPONSE, "上传成功！！！", null);
    }

    @PostMapping("/getDicomFilePath")
    @ApiOperation("获取文件路径")
    @ApiImplicitParam(name = "patientID", value = "病人ID", dataType = "String")
    public ResponseEntity<HashMap<String, LinkedList<String>>> getDicomFilePath(@RequestParam("patientID") String patientID) {
        HashMap<String, LinkedList<String>> stringLinkedListHashMap = fileService.dicomFilePath(patientID);
        return new ResponseEntity<>(ResponseStatus.SUCCESS_RESPONSE, "成功！！！", stringLinkedListHashMap);
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
        } catch (IOException e) {
            return new ResponseEntity(ResponseStatus.FAILURE_RESPONSE, "上传失败，请检查文件是否正确", null);
        }
        return new ResponseEntity<>(ResponseStatus.SUCCESS_RESPONSE, "成功！", fileInfo);
    }

    @GetMapping("/testConnect")
    @ApiOperation("用来测试是否可以成功与服务器连接，无特殊作用")
    public ResponseEntity testConnect(){
        return new ResponseEntity<>(ResponseStatus.SUCCESS_RESPONSE, "连接成功！", null);
    }
}
