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
	private String id; /* ID of the stored object */
	private String fileName; /* File name of the stored object */
	private String dirPath; /* Path of the parent dir of the stored object */
	private String filePath; /* Path relative to the mountpoint of the stored object */
	private String diskPath; /* Path of the disk which was allocated to the stored object */

	public DBFileObject(String fileName, String dirPath, String filePath) {
		this.fileName = fileName;
		this.dirPath = dirPath;
		this.filePath = filePath;
		id = String.valueOf(this.hashCode() & Integer.MAX_VALUE);
	}
}
