// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.io.*;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		Tasks tasks = new Tasks();
		Commands commands = new Commands(tasks, System.out);
		
		// reload previous tasks from a file
		File file = new File("tasks.txt");
		if (file.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(new File("tasks.txt")));
			
			String line;
			do {
				line = reader.readLine();
				if (line != null && !line.isEmpty()) {
					tasks.addTask(line);
				}
			}
			while (line != null);
		}
		
		String command;
		Scanner scanner = new Scanner(System.in);
		do {
			command = scanner.nextLine();
			
			if (!command.equals("exit")) {
				try {
					commands.execute(command);
				}
				catch (RuntimeException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		while (!command.equals("exit"));
		
		// save to a file
		// TODO we'll do this better in the future, right now this will lose all the current id's, you can't persist the active task with this
		FileOutputStream output = new FileOutputStream(new File("tasks.txt"));
		
		for (Task task : tasks.getTasks()) {
			output.write(task.task.getBytes());
			output.write("\n".getBytes());
		}
	}
}
