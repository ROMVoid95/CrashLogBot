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
package net.romvoid.crashbot.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


public class FileUtil {

	public static final Pattern CRASHLOG = Pattern.compile(
			"(crash-([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))_(?:[01]\\d|2[0123]).(?:[012345]\\d).(?:[012345]\\d)-(client|server).(txt)|(message).(txt))");
	public static final Pattern LOGS_TXT = Pattern.compile("(latest|message).(txt|log)");
	public static final Pattern LOG_GZ = Pattern.compile("(([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))-([1-9]))");
	public static final Pattern ARCHIVE = Pattern.compile("(tar|gz|zip|7z|rar)");

	public final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
	public final static Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username, $2
																									// -> discriminator
	public final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
	public final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
	public final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID
	public final static Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{17,20})>");
	
	public static boolean matchToExt(Pattern pattern, String filename) {
        Matcher matcher = pattern.matcher(filename);
        return matcher.matches();
	}

	public static String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}

	public static boolean isArchive(File file) {
		return Pattern.matches("log.gz", getFileExtension(file.getName()));
	}
	
	public static void add(File in) {
		int num = 0;
		String save = "f";
		File dest = new File("finders/" + save);
		while(dest.exists()) {
			save = "f_" + (num++);
			dest = new File("finders/" + save);
		}
		try {
			copyFile(in, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void copyFile(File source, File dest) throws IOException {
	    FileUtils.copyFile(source, dest);
	}
}
