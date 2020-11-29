package net.romvoid.crashbot.file.solution;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SolutionFile {
	
	private static List<Solution> solutions = new ArrayList<Solution>();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static Writer writer;
	private static Reader reader;

	public static List<Solution> read() {
		try {
			reader = Files.newBufferedReader(Paths.get("solutions/solutions.json"));
			//setSolutions(gson.fromJson(reader, new TypeToken<List<Solution>>() {}.getType()));
			List<Solution> list = Arrays.asList(gson.fromJson(reader, Solution[].class));
			reader.close();
			return list;
		} catch (Exception e) {
			e.getStackTrace();
		}
		return null;
	}

	public static void write(Solution solution) {
		solutions.addAll(getSolutions());
		solutions.add(solution);
		try {

			writer = Files.newBufferedWriter(Paths.get("solutions/solutions.json"));
			gson.toJson(solutions, writer);
			for(Solution s : solutions) {
				System.out.println(s.toString());
			}
			writer.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	
	public static List<Solution> getSolutions() {
		solutions = read();
		return solutions;
	}
}
