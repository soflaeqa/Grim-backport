package com.mongodb.client;

public interface MongoCollection<T> {
    FindIterable<T> find();
}
