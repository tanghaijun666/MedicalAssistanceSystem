package com.cqupt.mas.repository;

import com.cqupt.mas.entity.po.DicomFilePO;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author 唐海军
 * @create 2022-12-10 23:14
 */


public interface DicomFilePORepository extends MongoRepository<DicomFilePO, String> {

}