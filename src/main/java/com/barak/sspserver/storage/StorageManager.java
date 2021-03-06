package com.barak.sspserver.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class StorageManager {
	private static StorageManager storageManager = null;
	private final LinkedHashSet<String> disks;
	final String[] disksArray;

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

	public String allocateDisk(String id) {
		/*
		 * Files are created on a single disk based on the id of the corresponding db
		 * object
		 */
		int fileHash = Integer.parseInt(id);
		int diskNum = fileHash % disks.size();

		return disksArray[diskNum];
	}

	public int mkdir(String dirPath) {
		/* Dirs are created across all disks */
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
		/* Dirs are deleted from all disks */
		try {
			for (String diskPath : disksArray) {
				Files.delete(Paths.get(diskPath + dirPath));
			}
		} catch (IOException e) {
			return -1;
		}
		return 0;
	}

	public byte[] readFile(String fullPath) {
		byte[] fileContent = null;
		File file = new File(fullPath);
		try {
			fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			return null;
		}

		return fileContent;
	}
}
