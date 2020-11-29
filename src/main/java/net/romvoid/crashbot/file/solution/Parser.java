package net.romvoid.crashbot.file.solution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

public class Parser {

	private static String[] notSupported = new String[] { "Minecraft Version: 1.7.10", "Minecraft Version: 1.8.9" };
	public static String version;

	public static boolean checkVersion(String message) {
		boolean result = true;
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(IOUtils.toInputStream(message, Charset.defaultCharset())));
			String line = null;
			int currentLine = 0;
			while ((line = reader.readLine()) != null) {
				currentLine++;
				for (String ver : notSupported) {
					if (line.contains(ver)) {
						version = ver.replace("Minecraft Version: ", "");
						System.out.println(version);
						return false;
					}
				}
			}
			reader.close();
		} catch (IOException e) {

		}
		return result;
	}

	public static Solution find(String message) {
//		ArrayList<String> result = new ArrayList<>();
//		InputStreamReader isr = new InputStreamReader(IOUtils.toInputStream(message, Charset.defaultCharset()));
//		try(BufferedReader br = new BufferedReader(isr)) {
//		    while (br.ready()) {
//		        result.add(br.readLine());
//		    }
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		for (Solution solution : SolutionFile.getSolutions()) {
			if(Arrays.stream(solution.getKeys().toArray(new String[0])).parallel().allMatch(message::contains))  {
				return solution;
			}
//			int arraySize = result.size();
//			for (int i = 0; i < arraySize; i ++) {
//				for(String key : solution.getKeys()) {
//					if(result.get(i).contains(key)) {
//						System.out.println("Line #" + i + " matches Key: " + key);
//						return solution;
//					}
//				}
//			}
		}
		return null;
	}

}
