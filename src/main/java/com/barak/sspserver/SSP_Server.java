package com.barak.sspserver;

import java.util.LinkedList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.barak.sspserver.mongodb.MongoManager;
import com.barak.sspserver.storage.StorageManager;

@SpringBootApplication
public class SSP_Server {

	public static void main(String[] args) {
		LinkedList<String> diskList = new LinkedList<String>();
		diskList.add("/home/barak/uploads");
		try {
			StorageManager.initStorageManager(diskList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MongoManager mongoManager = MongoManager.getMongoManager();

		// Run Server
		SpringApplication.run(SSP_Server.class, args);

//		mongoManager.mongoReset();
	}
}
