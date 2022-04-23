package com.barak.sspserver;

import java.util.LinkedList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.barak.sspserver.storage.StorageManager;

@SpringBootApplication
public class SSP_Server {

	public static void main(String[] args) {
		if (args[0] == null) {
			System.out.println("Provide paths for storage");
		}

		LinkedList<String> diskList = new LinkedList<String>();
		String[] paths = args[0].split(";");
		for (String path : paths) {
			if (!path.equals("")) {
				diskList.add(path);
			}
		}

		try {
			StorageManager.initStorageManager(diskList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Run Server
		SpringApplication.run(SSP_Server.class, args);
	}
}
