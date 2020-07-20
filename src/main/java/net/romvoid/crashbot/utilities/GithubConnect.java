package net.romvoid.crashbot.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import net.romvoid.crashbot.Bot;

public class GithubConnect {
	
	public static void send(File file) throws IOException {
		GitHub github = new GitHubBuilder().withOAuthToken(Bot.getConfiguration().getString("gtoken")).build();
		github.getRepository("ROMVoid95/CrashBot").createContent().content(contents(file)).path("finders/" + file.getName()).branch("development").message("Added " + file.getName() + " to finders db").commit();

	}
	
	private static byte[] contents(File file) throws IOException {
		byte[] bytesArray = new byte[(int) file.length()];

		FileInputStream fis = new FileInputStream(file);
		fis.read(bytesArray); //read file into bytes[]
		fis.close();

		return bytesArray;
	}

}
