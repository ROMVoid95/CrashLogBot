package net.romvoid.crashbot.reply;

public class CrashLogReplies {
		
	public static String downgradeMod(String modName, String currentVersion, String neededVersion) {
		return "Please downgrade " + modName + "from " + currentVersion + "to " + neededVersion;
	}

}
