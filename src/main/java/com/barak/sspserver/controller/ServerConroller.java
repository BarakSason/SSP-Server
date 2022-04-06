package com.barak.sspserver.controller;

import java.io.IOException;
import java.util.LinkedList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.barak.sspserver.mongodb.MongoManager;
import com.barak.sspserver.storage.StorageManager;

@RestController
public class ServerConroller {
	MongoManager mongoManager;
	StorageManager storageManager;

	public ServerConroller() throws Exception {
		mongoManager = MongoManager.getMongoManager();
		storageManager = StorageManager.getStorageManager();
	}

	@PostMapping("/upload")
	public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file,
			@RequestParam("diskpath") String diskpath) {
		// Check if requested disk path is valid
		if (storageManager.validateDiskPath(diskpath) != 0) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid disk path " + diskpath);
		}

		String fileName = file.getOriginalFilename();
		// Check if the file doesn't already exist
		if (mongoManager.addEntry(diskpath, fileName) != 0) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("File already exist " + fileName);
		}

		// Create the file on disk
		try {
			storageManager.saveFile(file, diskpath, fileName);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.OK).body("Error saving file " + fileName);
		}

		return ResponseEntity.status(HttpStatus.OK).body("You successfully uploaded " + fileName);
	}

	@GetMapping("/list")
	public ResponseEntity<Object> listFiles() {

		LinkedList<String> entries = mongoManager.listEntries();

		return ResponseEntity.status(HttpStatus.OK).body(entries);
	}
}
