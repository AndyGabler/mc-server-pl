package com.gabler.huntersmc.util.uuid;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;

// Stolen from https://github.com/ThexXTURBOXx/McUUIDFetcher/blob/master/UUIDFetcher.java
public class UuidFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";

    public UuidFetcher() {}

    /**
     * Returns the UUID of the searched player.
     *
     * @param playername The name of the player.
     * @return The UUID of the given player.
     */
    public UUID getUUID(String playername) throws IOException {
        String output = callURL(UUID_URL + playername);

        // My addition
        if (new Gson().fromJson(output, MojangUuidResponse.class).getError() != null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        readData(output, result);
        String u = result.toString();
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i <= 31; i++) {
            uuid.append(u.charAt(i));
            if (i == 7 || i == 11 || i == 15 || i == 19) {
                uuid.append('-');
            }
        }
        return UUID.fromString(uuid.toString());
    }

    private static void readData(String toRead, StringBuilder result) {
        for (int i = toRead.length() - 3; i >= 0; i--) {
            if (toRead.charAt(i) != '"') {
                result.insert(0, toRead.charAt(i));
            } else {
                break;
            }
        }
    }

    private static String callURL(String urlStr) throws IOException {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in;

        URL url = new URL(urlStr);
        urlConn = url.openConnection();
        if (urlConn != null) {
            urlConn.setReadTimeout(60 * 1000);
        }
        if (urlConn != null && urlConn.getInputStream() != null) {
            in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
            BufferedReader bufferedReader = new BufferedReader(in);
            int cp;
            while ((cp = bufferedReader.read()) != -1) {
                sb.append((char) cp);
            }
            bufferedReader.close();
            in.close();
        }
        return sb.toString();
    }

}
