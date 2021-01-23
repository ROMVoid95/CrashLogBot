package net.romvoid.crashbot.db.entities.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.romvoid.crashbot.utilities.annotations.ConfigName;

public class GuildData {

    @ConfigName("Haste URL")
    private String hasteUrl;
    @ConfigName("Server custom prefix")
    private String guildCustomPrefix = null;
    @ConfigName("Disabled Members")
    private List<String> disabledUsers = new ArrayList<>();
    @ConfigName("Disabled Channels")
    private Set<String> disabledChannels = new HashSet<>();
    @ConfigName("Disabled Commands")
    private Set<String> disabledCommands = new HashSet<>();
    @ConfigName("Commands disabled in channels")
    private HashMap<String, List<String>> channelSpecificDisabledCommands = new HashMap<>();
    

    public GuildData() { }

    public String getHasteUrl() {
        return this.hasteUrl;
    }

    public void setHasteUrl(String birthdayChannel) {
        this.hasteUrl = birthdayChannel;
    }
    
    public String getGuildCustomPrefix() {
        return this.guildCustomPrefix;
    }

    public void setGuildCustomPrefix(String guildCustomPrefix) {
        this.guildCustomPrefix = guildCustomPrefix;
    }
    
    public List<String> getDisabledUsers() {
        return this.disabledUsers;
    }

    public void setDisabledUsers(List<String> disabledUsers) {
        this.disabledUsers = disabledUsers;
    }
    
    public Set<String> getDisabledCommands() {
        return this.disabledCommands;
    }

    public void setDisabledCommands(Set<String> disabledCommands) {
        this.disabledCommands = disabledCommands;
    }
    
    public Set<String> getDisabledChannels() {
        return this.disabledChannels;
    }

    public void setDisabledChannels(Set<String> disabledChannels) {
        this.disabledChannels = disabledChannels;
    }
    
    public HashMap<String, List<String>> getChannelSpecificDisabledCommands() {
        return this.channelSpecificDisabledCommands;
    }

    public void setChannelSpecificDisabledCommands(HashMap<String, List<String>> channelSpecificDisabledCommands) {
        this.channelSpecificDisabledCommands = channelSpecificDisabledCommands;
    }
}
