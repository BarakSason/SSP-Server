package com.barak.sspserver.model;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileData {
	@Id
	private final String id;
	private final String fileName;
	private final String dirPath;
	private final byte[] fileContent;
}
