package com.barak.sspserver.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

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

	public int addEntry(String diskpath, String fileName) {
		MongoCollection<Document> collection = mongoDB.getCollection("Files");

		// Search the db - If a match is found then this file already exist
		BasicDBObject filter = new BasicDBObject();
		filter.put("diskpath", diskpath);
		filter.put("fileName", fileName);
		FindIterable<Document> docIter = collection.find(Filters.and(filter));
		MongoCursor<Document> it = docIter.iterator();
		if (it.hasNext()) {
			it.close();
			return -1;
		}

		// Add an entry
		Document document = new Document("diskpath", diskpath).append("fileName", fileName);
		collection.insertOne(document);

		return 0;
	}

	public LinkedList<String> listEntries() {
		LinkedList<String> entries = new LinkedList<String>();

		MongoCollection<Document> collection = mongoDB.getCollection("Files");
		FindIterable<Document> docIter = collection.find();
		MongoCursor<Document> it = docIter.iterator();
		while (it.hasNext()) {
			entries.add((String) it.next().get("fileName"));
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
}
