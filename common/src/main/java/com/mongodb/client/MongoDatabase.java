package com.mongodb.client;

import org.bson.Document;

public interface MongoDatabase {
    MongoCollection<Document> getCollection(String collectionName);
}
