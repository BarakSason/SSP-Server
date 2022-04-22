package com.barak.sspserver.mongodb;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import com.barak.sspserver.repository.FileDataRep;

@Component
public class MongoManager {
	@Autowired
	FileDataRep rep;

	public void insert(DBFileObject dbFileObject) throws Exception {
		rep.save(dbFileObject);
	}

	public DBFileObject searchByPath(String filePath) {
		DBFileObject dbFileObject = new DBFileObject();
		dbFileObject.setFilePath(filePath);
		Example<DBFileObject> dbFileObjectExample = Example.of(dbFileObject);
		Optional<DBFileObject> match = rep.findOne(dbFileObjectExample);

		if (match.isEmpty()) {
			return null;
		}

		return match.get();
	}

	public LinkedList<String> listAll() {
		LinkedList<String> files = new LinkedList<>();

		List<DBFileObject> dbFiles = rep.findAll();

		Iterator<DBFileObject> it = dbFiles.iterator();
		while (it.hasNext()) {
			DBFileObject dbFileObject = it.next();
			files.add("Disk path: " + dbFileObject.getDiskPath() + " File path: " + dbFileObject.getFilePath());
		}

		return files;
	}

	public LinkedList<String> ls(String dirPath) {
		LinkedList<String> files = new LinkedList<String>();

		/* Get files */
		DBFileObject dbFileObject = new DBFileObject();
		dbFileObject.setDirPath(dirPath);
		Example<DBFileObject> dbFileObjectExample = Example.of(dbFileObject);
		List<DBFileObject> dbFiles = rep.findAll(dbFileObjectExample);

		Iterator<DBFileObject> it = dbFiles.iterator();
		while (it.hasNext()) {
			DBFileObject cur = it.next();
			files.add(cur.getFileName());
		}

		/* Get dirs */
		dbFileObject = new DBFileObject();
		dbFileObject.setDir(true);
		dbFileObjectExample = Example.of(dbFileObject);
		dbFiles = rep.findAll(dbFileObjectExample);

		it = dbFiles.iterator();
		while (it.hasNext()) {
			DBFileObject cur = it.next();
			files.add(cur.getFileName());
		}

		return files;
	}

	public String removeEntry(String filePath) {
		DBFileObject dbFileObject = new DBFileObject();
		dbFileObject.setFilePath(filePath);

		Example<DBFileObject> dbFileObjectExample = Example.of(dbFileObject);
		Optional<DBFileObject> match = rep.findOne(dbFileObjectExample);

		if (match.isEmpty()) {
			return null;
		}

		dbFileObject = match.get();
		rep.delete(dbFileObject);
		return dbFileObject.getDiskPath();
	}

	public int deleteDir(String dirPath) {
		DBFileObject dbFileObject = new DBFileObject();
		dbFileObject.setFileName(dirPath);
		dbFileObject.setDir(true);
		Example<DBFileObject> dbFileObjectExample = Example.of(dbFileObject);

		Optional<DBFileObject> match = rep.findOne(dbFileObjectExample);

		if (match.isEmpty()) {
			return -1;
		}

		dbFileObject = match.get();
		rep.delete(dbFileObject);
		return 0;
	}

}
