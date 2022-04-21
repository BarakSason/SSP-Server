package com.barak.sspserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.barak.sspserver.mongodb.DBFileObject;

@Repository
public interface FileDataRep extends MongoRepository<DBFileObject, String>{

}
