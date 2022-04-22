package com.barak.sspserver.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barak.sspserver.model.FileData;
import com.barak.sspserver.mongodb.DBFileObject;
import com.barak.sspserver.mongodb.MongoManager;
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
			mongoManager
					.insert(new DBFileObject(fileData.getFileName(), fileData.getDirPath(), filePath, diskPath, false));
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
		String parentDirPath = null;

		/*
		 * Get the path of the parent dir out of the dirPath and user it as a dirPath
		 * value for this dir - Used in ls implementation to list dirs as well as files
		 * in a dir
		 */
		if (!dirPath.equals(File.separator)) {
			String temp = dirPath.substring(0, dirPath.lastIndexOf(File.separator));
			parentDirPath = temp.substring(0, temp.lastIndexOf(File.separator) + 1);
		} else {
			parentDirPath = dirPath;
		}

		// Create dir on disk
		if (storageManager.mkdir(dirPath) != 0) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to create dir " + dirPath);
		}

		// Add dir to db
		try {
			mongoManager.insert(new DBFileObject(dirPath, parentDirPath, "", "", true));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("DB error " + e.getMessage());
		}

		return ResponseEntity.status(HttpStatus.OK).body("Dir created successfully " + dirPath);
	}

	@GetMapping("/ls")
	public ResponseEntity<LinkedList<String>> ls(@RequestParam("dirPath") String dirPath) {
		/*
		 * The "ls" op is implemented through a db query, as a local FS implementation
		 * requires aggregating the files and dir from the multiple local disks, which
		 * might be a slow operation on large scale. Not sure if te db is faster - worth
		 * testing on large scale
		 */
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
		/* Delete dir from DB */
		if (mongoManager.deleteDir(dirPath) != 0) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed to delete dir from DB " + dirPath);
		}

		/* Delete dir from disks */
		if (storageManager.deleteDir(dirPath) != 0) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body("Failed to delete dir from disk " + dirPath);
		}

		return ResponseEntity.status(HttpStatus.OK).body("Dir deleted successfully " + dirPath);
	}

	private ResponseEntity<String> deleteFile(String dirPath, String fileName) {
		String filePath = dirPath + fileName;

		/* Delete file from disks */
		String diskPath = mongoManager.removeEntry(filePath);
		if (diskPath == null) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("No such file " + filePath);
		}

		/* Delete file from db */
		String fullFilePath = diskPath + filePath;
		if (storageManager.deleteFile(fullFilePath) == 0) {
			return ResponseEntity.status(HttpStatus.OK).body("File deleted " + filePath);
		}

		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to delete file " + fullFilePath);
	}
}
