package com.barak.sspserver.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Files")
public class DBFileObject {
	@Id
	private String id;
	private String fileName;
	private String dirPath;
	private String filePath;
	private String diskPath;

	public DBFileObject(String fileName, String dirPath, String filePath) {
		this.fileName = fileName;
		this.dirPath = dirPath;
		this.filePath = filePath;
		id = String.valueOf(this.hashCode() & Integer.MAX_VALUE);
	}
}
