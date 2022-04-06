package com.barak.sspserver.controller;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
	public ResponseEntity<String> create(@RequestParam("file") MultipartFile file,
			@RequestParam("dirPath") String dirPath) {
		if (!dirPath.endsWith("/")) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body("Dir path must end with \"" + "/" + "\" " + dirPath);
		}

		String fileName = file.getOriginalFilename();
		String filePath = dirPath + fileName;
		String diskPath = storageManager.allocateDisk(filePath);

		// Create the file on disk
		try {
			storageManager.create(file, diskPath + filePath);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Error creating file " + filePath);
		}

		// Add file to db
		if (mongoManager.addEntry(diskPath, dirPath, filePath) != 0) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("File already exist " + filePath);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("You successfully uploaded " + filePath);
	}

	// TODO: for debugging only
	@GetMapping("/listall")
	public ResponseEntity<Object> listAll() {
		HashMap<String, String> entries = mongoManager.listAll();

		return ResponseEntity.status(HttpStatus.OK).body(entries);
	}

	@PostMapping("/mkdir")
	public ResponseEntity<String> mkdir(@RequestParam("dirPath") String dirPath) {
		if (storageManager.mkdir(dirPath) == 0) {
			return ResponseEntity.status(HttpStatus.OK).body("Dir created successfully " + dirPath);
		}
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to create dir " + dirPath);
	}

	@GetMapping("/ls")
	public ResponseEntity<Object> ls(@RequestParam("dirPath") String dirPath) {
		HashMap<String, String> entries = mongoManager.ls(dirPath);

		return ResponseEntity.status(HttpStatus.OK).body(entries);
	}

	@DeleteMapping("/rm")
	public ResponseEntity<Object> rm(@RequestParam("dirPath") String dirPath,
			@RequestParam("fileName") String fileName) {
		String filePath = dirPath + fileName;
		String diskPath = mongoManager.removeEntry(filePath);
		if (diskPath == null) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("No such file " + filePath);
		}

		String fullFilePath = diskPath + filePath;
		if (storageManager.rm(fullFilePath) == 0) {
			return ResponseEntity.status(HttpStatus.OK).body("File deleted " + filePath);
		}

		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to delete file " + fullFilePath);
	}
}
