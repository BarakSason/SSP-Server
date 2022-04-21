package com.barak.sspserver.controller;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.barak.sspserver.model.FileData;
import com.barak.sspserver.mongodb.DBFileObject;
import com.barak.sspserver.mongodb.MongoManager;
import com.barak.sspserver.repository.FileDataRep;
import com.barak.sspserver.storage.StorageManager;

//TODO: change return values of several APIs from LinkedList<String> to a data structure composed of
// a dedicated class which represents the return info (something like "FileInfo" which has diskPath 
// and filePath fields)
@RestController
public class ServerConroller {
	@Autowired
	MongoManager mongoManager;
	StorageManager storageManager;

	public ServerConroller() throws Exception {
		storageManager = StorageManager.getStorageManager();
	}

	@PostMapping("/create")
	public ResponseEntity<String> create(@RequestBody FileData fileData) {
		String filePath = fileData.getDirPath() + fileData.getFileName();
		String diskPath = storageManager.allocateDisk(filePath);

		// Create the file on disk
		try {
			storageManager.create(fileData.getFileContent(), diskPath + filePath);
		} catch (FileAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("File already exist " + e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Error creating file " + e.getMessage());
		}

		// Search the db to verify this file doesn't exist in it
		DBFileObject match = mongoManager.searchByPath(filePath);

		if (match != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("File already exist in db" + filePath);
		}

		// Add file to db
		try {
			mongoManager.insert(new DBFileObject(fileData.getId(), fileData.getFileName(), fileData.getDirPath(),
					filePath, diskPath));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("DB error " + e.getMessage());
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created " + filePath);
	}

	// TODO: for debugging only
	@GetMapping("/listall")
	public ResponseEntity<LinkedList<String>> listAll() {
		LinkedList<String> files = mongoManager.listAll();

		return ResponseEntity.status(HttpStatus.OK).body(files);
	}

	@PostMapping("/mkdir")
	public ResponseEntity<String> mkdir(@RequestParam("dirPath") String dirPath) {
		if (storageManager.mkdir(dirPath) == 0) {
			return ResponseEntity.status(HttpStatus.OK).body("Dir created successfully " + dirPath);
		}
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to create dir " + dirPath);
	}

	@GetMapping("/ls")
	public ResponseEntity<LinkedList<String>> ls(@RequestParam("dirPath") String dirPath) {
		LinkedList<String> files = mongoManager.ls(dirPath);

		return ResponseEntity.status(HttpStatus.OK).body(files);
	}

	@DeleteMapping("/rm")
	public ResponseEntity<String> rm(@RequestParam("dirPath") String dirPath,
			@RequestParam("fileName") String fileName) {
		if (fileName.equals("")) {
			return deleteDir(dirPath);
		} else {
			return deleteFile(dirPath, fileName);
		}
	}

	private ResponseEntity<String> deleteDir(String dirPath) {
		if (storageManager.deleteDir(dirPath) == 0) {
			return ResponseEntity.status(HttpStatus.OK).body("Dir deleted " + dirPath);
		}

		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to delete dir " + dirPath);
	}

	private ResponseEntity<String> deleteFile(String dirPath, String fileName) {
		String filePath = dirPath + fileName;

		String diskPath = mongoManager.removeEntry(filePath);
		if (diskPath == null) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("No such file " + filePath);
		}

		String fullFilePath = diskPath + filePath;
		if (storageManager.deleteFile(fullFilePath) == 0) {
			return ResponseEntity.status(HttpStatus.OK).body("File deleted " + filePath);
		}

		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to delete file " + fullFilePath);
	}
}
