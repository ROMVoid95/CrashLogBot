package net.romvoid.crashbot.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class MainConfig {

    public String dbDb = "crashlogbot";
    public String dbHost = "localhost";
    public String dbPassword;
    public int dbPort = 28015;
    public String dbUser;
    public List<String> owners = new ArrayList<>();
    public String[] prefix = {"~>", "->"};
    public String shardWebhookUrl;
    public String token;
    public int totalShards = 0;
    public String webhookUrl;
    public String apiTwoUrl = "http://127.0.0.1:5874";
    public boolean needApi = true;
    public String apiAuthKey;
    public String clientId; //why not ig.
    public String jedisPoolAddress = "127.0.0.1";
    public int jedisPoolPort = 6379;
    public long dailyMaxPeriodMilliseconds = TimeUnit.HOURS.toMillis(50);
    public boolean isSelfHost = false;
    public int memberCacheSize = 10_000;
    public boolean handleRatelimits = true;
    public int bucketFactor = 4;
    
    public MainConfig() { }

    public boolean isOwner(Member member) {
        return isOwner(member.getUser());
    }

    public boolean isOwner(User user) {
        return isOwner(user.getId());
    }

    public boolean isOwner(String id) {
        return owners.contains(id);
    }

    public String getDbDb() {
        return this.dbDb;
    }

    public String getDbHost() {
        return this.dbHost;
    }

    public String getDbPassword() {
        return this.dbPassword;
    }

    public int getDbPort() {
        return this.dbPort;
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public List<String> getOwners() {
        return this.owners;
    }

    public String[] getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String[] prefix) {
        this.prefix = prefix;
    }

    public String getShardWebhookUrl() {
        return this.shardWebhookUrl;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTotalShards() {
        return this.totalShards;
    }

    public String getWebhookUrl() {
        return this.webhookUrl;
    }

    public String getApiTwoUrl() {
        return this.apiTwoUrl;
    }

    public boolean isNeedApi() {
        return this.needApi;
    }

    public String getApiAuthKey() {
        return this.apiAuthKey;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getJedisPoolAddress() {
        return this.jedisPoolAddress;
    }

    public int getJedisPoolPort() {
        return this.jedisPoolPort;
    }

    public long getDailyMaxPeriodMilliseconds(){
        return this.dailyMaxPeriodMilliseconds;
    }

    public boolean isSelfHost() {
        return isSelfHost;
    }

    public void setSelfHost(boolean selfHost) {
        isSelfHost = selfHost;
    }

    public boolean isHandleRatelimits() {
        return handleRatelimits;
    }
    
    public int getBucketFactor() {
        return this.bucketFactor;
    }
}
