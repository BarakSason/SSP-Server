package com.barak.sspserver.model;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import org.springframework.data.annotation.Id;

@Getter
@Setter
public class FileData {
	@Id
	private final String id;
	private final String fileName;
	private final String dirPath;
	private final byte[] fileContent;

	public FileData(String filePath, String fileName, String dirPath, byte[] fileContent) throws IOException {
		this.fileName = fileName;
		this.dirPath = dirPath;
		this.fileContent = fileContent;
		id = String.valueOf(this.hashCode() & Integer.MAX_VALUE);
	}
}
