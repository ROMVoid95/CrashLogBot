package net.romvoid.crashbot.commands.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CommonError {
    private final String message;
    private final String[] keys;
    private final boolean matchCase;
    private final boolean invert;

    public CommonError(String message, String[] keys, boolean matchCase, boolean invert) {
        this.message = message;
        this.keys = keys;
        this.matchCase = matchCase;
        this.invert = invert;
    }

    public CommonError(String message, String[] keys) {
        this(message, keys, false, false);
    }

    public String getMessage() {
        return message;
    }

    public String[] getKeys() {
        return keys;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public boolean isInvert() {
        return invert;
    }

    public static class Deserializer implements JsonDeserializer<CommonError> {
        @Override
        public CommonError deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String message = object.get("message").getAsString();
            JsonArray k = object.get("keys").getAsJsonArray();
            boolean matchCase = false;
            boolean invert = false;
            String[] keys = new String[k.size()];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = k.get(i).getAsString();
            }
            if (object.has("invert")) {
                invert = object.get("invert").getAsBoolean();
            }

            if (object.has("match_case")) {
                matchCase = object.get("match_case").getAsBoolean();
            }
            return new CommonError(message, keys, matchCase, invert);
        }
    }
}
