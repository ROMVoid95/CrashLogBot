package net.romvoid.crashbot;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.romvoid.crashbot.file.FileHandler;
import net.romvoid.crashbot.file.Patterns;

public class MessageListener extends ListenerAdapter {

	private String[] pasteSites = { "pastebin.com", "paste.ubuntu.com", "pastebin.ubuntu.com", "ghostbin.co",
			"paste.ee", "paste.dimdev.org" };
	private String[] special = { "pastebin.ubuntu.com", "paste.ubuntu.com" };
	private String url;
	private boolean foundUrl;
	private Matcher matcher;
	private boolean isPasteURL;
	private boolean isUbuntuURL;
	private Matcher urlSplit;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) {return;}
		if (!event.getMessage().getAttachments().isEmpty()) {
			new FileHandler(event);
			return;
		}
		if (event.getMessage().getAttachments().isEmpty()) {
			matcher = Patterns.Url.PASTEURL.matcher(event.getMessage().getContentStripped());
			foundUrl = matcher.find();
			System.out.println("Found URL: " + foundUrl);
			if (foundUrl) {
				url = matcher.group(0);
				isPasteURL = isPasteSite(url);
				System.out.println("is PasteSite: " + isPasteURL);
				if (isPasteURL) {
					Pattern pattern = Pattern.compile(Patterns.Url.DIVIDEURL);
					urlSplit = pattern.matcher(url);
					if (urlSplit.find())
						isUbuntuURL = isSpecialPasteSite(urlSplit.group(1));
					System.out.println("is UbuntuPaste: " + isUbuntuURL);
					if (isUbuntuURL) {
						new FileHandler(event, url);
						return;
					} else {
						new FileHandler(event, urlSplit.group(1), urlSplit.group(2));
						return;
					}
				}
			}
		}
	}

	private boolean isSpecialPasteSite(String message) {
		return Arrays.stream(special).parallel().anyMatch(message::contains);
	}

	private boolean isPasteSite(String message) {
		return Arrays.stream(pasteSites).parallel().anyMatch(message::contains);
	}

}
