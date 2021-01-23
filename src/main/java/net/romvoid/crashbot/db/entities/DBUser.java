package net.romvoid.crashbot.db.entities;

import java.beans.ConstructorProperties;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.romvoid.crashbot.CrashBot;
import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.db.ManagedObject;
import net.romvoid.crashbot.db.entities.helpers.UserData;

public class DBUser implements ManagedObject {
    public static final String DB_TABLE = "users";
    private final UserData data;
    private final String id;
    
    @JsonIgnore
    private final MainConfig config = CrashBotData.config().get();
    
    @JsonCreator
    @ConstructorProperties({"id", "data"})
    public DBUser(@JsonProperty("id") String id, @JsonProperty("data") UserData data) {
        this.id = id;
        this.data = data;
    }
    
    public static DBUser of(String id) {
        return new DBUser(id, new UserData());
    }

    @JsonIgnore
    public User getUser(JDA jda) {
        return jda.retrieveUserById(getId()).complete();
    }

    @JsonIgnore
    public User getUser() {
        return CrashBot.getInstance().getShardManager().retrieveUserById(getId()).complete();
    }
    
    public UserData getData() {
        return this.data;
    }

    @Nonnull
    public String getId() {
        return this.id;
    }

    @JsonIgnore
    @Override
    @Nonnull
    public String getTableName() {
        return DB_TABLE;
    }
    
    public MainConfig getConfig() {
        return this.config;
    }
}
