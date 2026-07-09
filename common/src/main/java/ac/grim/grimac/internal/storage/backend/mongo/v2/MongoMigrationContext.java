package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.registry.MigrationContext;
import com.mongodb.client.MongoDatabase;

import java.util.logging.Logger;

public final class MongoMigrationContext implements MigrationContext {
    public MongoMigrationContext(MongoDatabase db, Logger logger) {
    }
}
