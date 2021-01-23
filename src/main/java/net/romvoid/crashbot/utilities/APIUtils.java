package net.romvoid.crashbot.utilities;

import java.io.IOException;

import net.romvoid.crashbot.CrashBotInfo;
import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.data.CrashBotData;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class APIUtils {
    private static final MainConfig config = CrashBotData.config().get();
    private static final OkHttpClient httpClient = new OkHttpClient();

    public static String getFrom(String route) throws IOException {
        var request = new Request.Builder()
                .url(config.apiTwoUrl + route)
                .addHeader("Authorization", config.getApiAuthKey())
                .addHeader("User-Agent", CrashBotInfo.USER_AGENT)
                .get()
                .build();

        try(var response = httpClient.newCall(request).execute()) {
            var body = response.body();
            if (body == null) {
                throw new IllegalStateException("Body is null");
            }

            return body.string();
        }
    }

}
