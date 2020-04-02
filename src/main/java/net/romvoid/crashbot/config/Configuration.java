/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 ROMVoid
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.romvoid.crashbot.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Configuration {
    
    private File file;
    private JsonObject json;
    public JsonParser jsonParser;

    public Configuration(final File file) {

        this.file = file;
        String cont = null;
        jsonParser = new JsonParser();

        try {
            if (file.exists()) {
            	BufferedReader reader = new BufferedReader(new FileReader(file));
                cont = reader.lines().collect(Collectors.joining("\n"));
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cont == null || cont.equals("")) {
            cont = "{}";
        }
        json = jsonParser.parse(cont).getAsJsonObject();
        
    }

    /**
     * @param key
     * @param val
     * @description Sets tha value of a key in config
     */
    public Configuration set(final String key, final String val) {
        if (json.has(key)) {
            json.remove(key);
        }
        if (val != null) {
            json.addProperty(key, val);
        }
        return this.save();
    }

    /**
     * @param key
     * @param val
     * @description Sets the value of a key in config
     */
    public Configuration set(final String key, final int val) {
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
    public Configuration unset(final String key) {
        if (json.has(key))
            json.remove(key);

        return this.save();
    }

    /**
     * @description Saves the config
     */
    private Configuration save() {
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