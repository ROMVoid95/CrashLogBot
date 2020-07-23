package net.romvoid.crashbot.file;

import java.util.regex.Pattern;

public final class Patterns {

	public static final class Filenames {
		private final static String DATE = "(19|20)[0-9]{2}[- \\/.](0[1-9]|1[012])[- \\/.](0[1-9]|[12][0-9]|3[01])";
		private final static String TIME = "(?:[01]\\d|2[0123]).(?:[012345]\\d).(?:[012345]\\d)";

		public static final Pattern CRASHLOG = Pattern
				.compile("(crash-" + DATE + "_" + TIME + "-(client|server).(txt)|(message).(txt))");
		public static final Pattern LOGS_TXT = Pattern.compile("(latest|message).(txt|log)");
		public static final Pattern LOG_GZ = Pattern
				.compile("([12]\\d{3})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])-([1-9]).(log.gz)");
		public static final Pattern ARCHIVE = Pattern.compile("(tar|gz|zip|7z|rar)");

		public final static Pattern DISCORD_MSG_URL = Pattern
				.compile("(https:\\/\\/cdn.discordapp.com\\/attachments\\/)(\\d{17,20})\\/(\\d{17,20})\\/");
	}

	public static final class Discord {
		public final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
		public final static Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username,
																										// $2
																										// ->
																										// discriminator
		public final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
		public final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
		public final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID
		public final static Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{17,20})>");
	}
}
