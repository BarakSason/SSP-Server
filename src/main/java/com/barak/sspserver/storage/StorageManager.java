package com.barak.sspserver.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;

import org.springframework.web.multipart.MultipartFile;

public class StorageManager {
	private static StorageManager storageManager = null;
	private HashSet<String> disks;

	public static StorageManager getStorageManager() throws Exception {
		if (storageManager == null) {
			throw new Exception("Call init first");
		}

		return storageManager;
	}

	private StorageManager(LinkedList<String> diskList) {
		disks = new HashSet<String>();
		for (String disk : diskList) {
			disks.add(disk);
		}
	}

	public static void initStorageManager(LinkedList<String> diskList) throws Exception {
		if (storageManager != null) {
			throw new Exception("Already initialied");
		}

		storageManager = new StorageManager(diskList);
	}

	public void saveFile(MultipartFile file, String diskpath, String fileName) throws IOException {
		byte[] bytes = file.getBytes();
		Path path = Paths.get(diskpath + File.separator + fileName);
		Files.write(path, bytes);
	}

	public int validateDiskPath(String diskPath) {
		return disks.contains(diskPath) ? 0 : -1;
	}
}
