package com.barak.sspserver.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.springframework.web.multipart.MultipartFile;

public class StorageManager {
	private static StorageManager storageManager = null;
	private LinkedHashSet<String> disks; // To be modified at each disk addition or removal
	String[] disksArray;

	public static StorageManager getStorageManager() throws Exception {
		if (storageManager == null) {
			throw new Exception("First call initStorageManager");
		}

		return storageManager;
	}

	private StorageManager(LinkedList<String> diskList) {
		disks = new LinkedHashSet<String>();
		for (String disk : diskList) {
			disks.add(disk);
		}

		disksArray = new String[disks.size()];
		System.arraycopy(disks.toArray(), 0, disksArray, 0, disks.size());
	}

	public static void initStorageManager(LinkedList<String> diskList) throws Exception {
		if (storageManager != null) {
			throw new Exception("Already initialized");
		}

		storageManager = new StorageManager(diskList);
	}

	public void create(byte[] fileContent, String fullFilePath) throws IOException {
		Path fullPath = Paths.get(fullFilePath);
		Files.write(fullPath, fileContent, StandardOpenOption.CREATE_NEW);
	}

	public String allocateDisk(String fileName) {
		int fileHash = fileName.hashCode() & 0x7FFFFFFF;
		int diskNum = fileHash % disks.size();

		return (String) disksArray[diskNum];
	}

	public int mkdir(String dirPath) {
		try {
			for (String diskPath : disksArray) {
				Files.createDirectory(Paths.get(diskPath + dirPath));
			}
		} catch (IOException e) {
			return -1;
		}

		return 0;
	}

	public int deleteFile(String fullFilePath) {
		File file = new File(fullFilePath);
		if (file.delete()) {
			return 0;
		}
		return -1;
	}

	public int deleteDir(String dirPath) {
		try {
			for (String diskPath : disksArray) {
				Files.delete(Paths.get(diskPath + dirPath));
			}
		} catch (IOException e) {
			return -1;
		}
		return 0;
	}
}
