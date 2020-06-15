package net.romvoid.crashbot.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ErrorsParser {
	
    private File file;
    private JsonObject json;
    public JsonParser jsonParser;
	
    /**
     * Sets the value of a key in config
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key
     */
    public <T extends String> ErrorsParser set(final String key, final T val) {
        if (json.has(key)) {
            json.remove(key);
        }
        if (val != null) {
            json.addProperty(key, val);
        }
        return this.save();
    }
    
    /**
     * Sets tha value of a key in config
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key
     */
    public <T extends Character> ErrorsParser set(final String key, final T val) {
        if (json.has(key)) {
            json.remove(key);
        }
        if (val != null) {
            json.addProperty(key, val);
        }
        return this.save();
    }

    /**
     * Sets the value of a key in config
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key
     */
    public <T extends Boolean> ErrorsParser set(final String key, final T val) {
        if (json.has(key)) {
            json.remove(key);
        }
        if (val != null) {
            json.addProperty(key, val);
        }
        return this.save();
    }

    /**
     * Sets the value of a key in config
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key
     */
    public <T extends Number> ErrorsParser set(final String key, final T val) {
        if (json.has(key)) {
            json.remove(key);
        }
        this.json.addProperty(key, val);
        return this.save();
    }

    /**
     * @param key
     * @description Removes key from config
     */
    public ErrorsParser unset(final String key) {
        if (json.has(key))
            json.remove(key);

        return this.save();
    }

    /**
     * @description Saves the config
     */
    private ErrorsParser save() {
        try {
            if (json.entrySet().size() == 0) {
                if (file.exists()) {
                    file.delete();
                }
            } else {
                if (!file.exists()) {
                    file.createNewFile();
                }

                BufferedWriter br = new BufferedWriter(new FileWriter(file));
                br.write(json.toString());
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * @param key
     * @return Value of key in config as string
     */
    public String getString(final String key) {
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param key
     * @return Value of key in config as integer
     */
    public int getInt(final String key) {
        if (json.has(key)) {
            return json.get(key).getAsInt();
        }
        return 0;
    }

    /**
     * @param key
     * @return If key exists
     */
    public boolean has(final String key) {
        try {
            return json.has(key);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    public List<String> keySet() {
        List<String> keys = new ArrayList<>();
        Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    public List<String> values() {
        List<String> values = new ArrayList<>();
        Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            values.add(entry.getValue().getAsString());
        }
        return values;
    }

}
