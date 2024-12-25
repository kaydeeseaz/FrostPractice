package dev.demeng.frost.database;

import dev.demeng.frost.Frost;
import dev.demeng.pluginbase.mongo.Mongo;
import dev.demeng.pluginbase.mongo.MongoCredentials;
import dev.demeng.pluginbase.mongo.lib.bson.Document;
import dev.demeng.pluginbase.mongo.lib.driver.client.MongoCollection;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;

public class MongoHandler extends Mongo {

  private static final boolean DISABLE_LOGGING = false;

  @Getter
  private final MongoCollection<Document> players;
  @Getter
  private final MongoCollection<Document> matchHistory;

  public MongoHandler(Frost i) {
    super(MongoCredentials.of(
        i.getSettingsConfig().getConfig().getString("MONGO.URI"),
        i.getSettingsConfig().getConfig().getString("MONGO.DATABASE")));

    this.players = getDatabase().getCollection("players");
    this.matchHistory = getDatabase().getCollection("matchHistory");

    if (DISABLE_LOGGING) {
      final Logger mongoLogger = Logger.getLogger("org.mongodb.driver.cluster");
      mongoLogger.setLevel(Level.SEVERE);
    }
  }
}
