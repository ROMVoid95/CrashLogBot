package net.romvoid.crashbot.db.entities;

import java.beans.ConstructorProperties;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.romvoid.crashbot.db.ManagedObject;
import net.romvoid.crashbot.db.entities.helpers.GuildData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DBGuild implements ManagedObject {
    public static final String DB_TABLE = "guilds";
    private final GuildData data;
    private final String id;

    @JsonCreator
    @ConstructorProperties({"id", "data"})
    public DBGuild(@JsonProperty("id") String id, @JsonProperty("data") GuildData data) {
        this.id = id;
        this.data = data;
    }

    public static DBGuild of(String id) {
        return new DBGuild(id, new GuildData());
    }

    public Guild getGuild(JDA jda) {
        return jda.getGuildById(getId());
    }

    public GuildData getData() {
        return this.data;
    }

    @Override
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
}
