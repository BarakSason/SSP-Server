package com.barak.sspserver.mongodb;

import java.util.HashMap;
import java.util.LinkedList;

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
	String collectionName;

	public static MongoManager getMongoManager() {
		if (mongoManager == null) {
			mongoManager = new MongoManager();
		}

		return mongoManager;
	}

	private MongoManager() {
		db = "Storage";
		collectionName = "Files";

		try {
			// Creating a Mongo client
			mongoClient = new MongoClient("localhost", 27017);

			// Accessing the database
			mongoDB = mongoClient.getDatabase(db);

			try {
				// Creating a collection
				mongoDB.getCollection(collectionName);
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

	public LinkedList<String> listAll() {
		LinkedList<String> entries = new LinkedList<>();

		MongoCollection<Document> collection = mongoDB.getCollection(collectionName);
		FindIterable<Document> docIter = collection.find();
		MongoCursor<Document> it = docIter.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			entries.add(
					"Disk path: " + document.getString("diskpath") + " File path: " + document.getString("filePath"));
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
		MongoCollection<Document> collection = mongoDB.getCollection(collectionName);

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

	public LinkedList<String> ls(String dirPath) {
		LinkedList<String> entries = new LinkedList<String>();

		// Search the db for all files in a given dir
		BasicDBObject filter = new BasicDBObject();
		filter.put("dirPath", dirPath);
		MongoCollection<Document> collection = mongoDB.getCollection(collectionName);
		FindIterable<Document> docIter = collection.find(Filters.and(filter));
		MongoCursor<Document> it = docIter.iterator();
		while (it.hasNext()) {
			Document document = it.next();
			entries.add(
					"File path: " + document.getString("filePath") + " Disk path: " + document.getString("diskpath"));
		}
		it.close();

		return entries;
	}
}
