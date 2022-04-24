package com.barak.sspserver;

import java.util.LinkedList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.barak.sspserver.storage.StorageManager;

@SpringBootApplication
public class SSP_Server {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Provide paths for storage");
			System.exit(0);
		}

		/* Get disk pathes from args */
		LinkedList<String> diskList = new LinkedList<String>();
		String[] paths = args[0].split(";");
		for (String path : paths) {
			if (!path.equals("")) {
				diskList.add(path);
			}
		}

		/* Send disk paths to the storage manager */
		try {
			StorageManager.initStorageManager(diskList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Run server */
		SpringApplication.run(SSP_Server.class, args);
	}
}
