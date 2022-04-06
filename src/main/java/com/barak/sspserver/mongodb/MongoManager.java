package com.barak.sspserver.mongodb;

import java.io.File;
import java.util.HashMap;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

public class MongoManager {
	private static MongoManager mongoManager = null;
	MongoClient mongoClient;
	MongoDatabase mongoDB;
	String db;
	String collection;

	public static MongoManager getMongoManager() {
		if (mongoManager == null) {
			mongoManager = new MongoManager();
		}

		return mongoManager;
	}

	private MongoManager() {
		db = "Storage";
		collection = "Files";

		try {
			// Creating a Mongo client
			mongoClient = new MongoClient("localhost", 27017);

			System.out.println("Connected to the database successfully");

			// Accessing the database
			mongoDB = mongoClient.getDatabase(db);

			try {
				// Creating a collection
				mongoDB.getCollection(collection);
				System.out.println("Collection created successfully");
			} catch (MongoCommandException e) {
				if (e.getCode() != 48) {
					throw e;
				} else {
					System.out.println("Collection already exists");
				}
			}
		} catch (Exception e) {
			if (mongoDB != null) {
				mongoDB.drop();
			}
			if (mongoClient != null) {
				mongoClient.close();
			}
			throw e;
		}
	}

	public int addEntry(String diskpath, String dirPath, String filePath) {
		MongoCollection<Document> collection = mongoDB.getCollection("Files");

		// Search the db - If a match is found then this file already exist
		BasicDBObject filter = new BasicDBObject();
		filter.put("diskpath", diskpath);
		filter.put("filePath", filePath);
		FindIterable<Document> docIter = collection.find(Filters.and(filter));
		MongoCursor<Document> it = docIter.iterator();
		if (it.hasNext()) {
			it.close();
			return -1;
		}

		// Add an entry
		Document document = new Document("diskpath", diskpath).append("dirPath", dirPath).append("filePath", filePath);
		collection.insertOne(document);

		return 0;
	}

	public HashMap<String, String> listAll() {
		HashMap<String, String> entries = new HashMap<>();

		MongoCollection<Document> collection = mongoDB.getCollection("Files");
		FindIterable<Document> docIter = collection.find();
		MongoCursor<Document> it = docIter.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			entries.put("Disk path : " + document.getString("diskpath") + File.separator,
					"File path : " + document.getString("filePath"));
		}
		it.close();

		return entries;
	}

	public void mongoReset() {
		if (mongoDB != null) {
			mongoDB.drop();
		}
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	public String removeEntry(String filePath) {
		String diskPath = null;
		MongoCollection<Document> collection = mongoDB.getCollection("Files");

		// Search the db - If a match is found, delete it
		BasicDBObject filter = new BasicDBObject();
		filter.put("filePath", filePath);
		FindIterable<Document> docIter = collection.find(Filters.and(filter));
		MongoCursor<Document> it = docIter.iterator();
		if (it.hasNext()) {
			Document document = it.next();
			diskPath = document.getString("diskpath");
		}

		DeleteResult deleteRes = collection.deleteOne(Filters.and(filter));

		if (deleteRes.getDeletedCount() == 1) {
			return diskPath;
		}
		return null;
	}

	public HashMap<String, String> ls(String dirPath) {
		HashMap<String, String> entries = new HashMap<>();

		// Search the db for all files in a given dir
		BasicDBObject filter = new BasicDBObject();
		filter.put("dirPath", dirPath);
		MongoCollection<Document> collection = mongoDB.getCollection("Files");
		FindIterable<Document> docIter = collection.find(Filters.and(filter));
		MongoCursor<Document> it = docIter.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			entries.put("File path : " + (String) document.get("filePath"),
					"Disk path : " + (String) document.get("diskpath"));
		}
		it.close();

		return entries;
	}
}
