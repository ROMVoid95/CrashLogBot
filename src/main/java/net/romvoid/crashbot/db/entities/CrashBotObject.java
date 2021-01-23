package net.romvoid.crashbot.db.entities;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.romvoid.crashbot.db.ManagedObject;

public class CrashBotObject implements ManagedObject {
    public static final String DB_TABLE = "crashbot";
    public static final String id = "crashbot";
    public List<String> blackListedGuilds;
    public List<String> blackListedUsers;

    @ConstructorProperties({"blackListedGuilds"})
    @JsonCreator
    public CrashBotObject(@JsonProperty("blackListedGuilds") List<String> blackListedGuilds,
    					  @JsonProperty("blackListedUsers") List<String> blackListedUsers) {
        this.blackListedGuilds = blackListedGuilds;
        this.blackListedUsers = blackListedUsers;
    }

    public CrashBotObject() { }

    public static CrashBotObject create() {
        return new CrashBotObject(new ArrayList<>(), new ArrayList<>());
    }

    @Override
	@Nonnull
    public String getId() {
        return id;
    }

    @JsonIgnore
    @Override
    @Nonnull
    public String getTableName() {
        return DB_TABLE;
    }

    public List<String> getBlackListedGuilds() {
        return this.blackListedGuilds;
    }

    public void setBlackListedGuilds(List<String> blackListedGuilds) {
        this.blackListedGuilds = blackListedGuilds;
    }
    
    public List<String> getBlackListedUsers() {
        return this.blackListedUsers;
    }

    public void setBlackListedUsers(List<String> blackListedUsers) {
        this.blackListedUsers = blackListedUsers;
    }
}
