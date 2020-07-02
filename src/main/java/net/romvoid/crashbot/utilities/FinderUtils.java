/**
 * CrashBot
 * Copyright (C) 2020
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.romvoid.crashbot.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import net.romvoid.crashbot.hastebin.Hastebin;

public class FinderUtils {

	public FinderUtils() {
		getFinders();
	};

	public static LinkedHashMap<String, String> finderSet = new LinkedHashMap<String, String>();

	public static void getFinders() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("finders");
		String path = url.getPath();
		List<File> files = new ArrayList<File>(Arrays.asList(new File(path).listFiles()));
		if (!files.isEmpty()) {
			for (File file : files) {
				try {
					BufferedReader s = new BufferedReader(new FileReader(file));
					String[] finder = s.readLine().split(";;");
					s.close();
					finderSet.put(finder[0], finder[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean find(String hasteString, URI url, String entry) {
		String id = hasteString.replace(Hastebin.getPasteURL(), "");
		String URLString = Hastebin.getPasteURL() + "raw/" + id + "/";
		boolean result = false;
		try {
			URL URL = new URL(URLString);
			HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
			connection.setDoOutput(true);
			connection.setConnectTimeout(10000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains(entry)) {
					return true;
				}
			}
			reader.close();
		} catch (IOException e) {

		}
		return result;
	}
}
