// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import com.andrewauclair.todo.git.GitControls;
import com.andrewauclair.todo.os.OSInterface;

import java.io.*;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		Tasks tasks = new Tasks(new TaskWriter(new FileCreator()));
		Commands commands = new Commands(tasks, System.out);
		
		File git_data = new File("git-data");
		boolean mkdir = git_data.mkdir();
		
		System.out.println(mkdir);
		
		GitControls gitControls = new GitControls();
		OSInterface osInterface = new OSInterface();
		osInterface.runGitCommand(new GitCommand("git init"));
		
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
