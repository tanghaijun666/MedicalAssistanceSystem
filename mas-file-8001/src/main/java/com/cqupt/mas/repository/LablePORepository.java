package com.cqupt.mas.repository;

import com.cqupt.mas.entity.po.LablePO;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author 唐海军
 * @create 2023-03-13 10:52
 */


public interface LablePORepository extends MongoRepository<LablePO, String> {

}