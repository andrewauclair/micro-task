// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import com.andrewauclair.todo.os.OSInterface;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		OSInterface osInterface = new OSInterface();
		Tasks tasks = new Tasks(new TaskWriter(osInterface), osInterface);
		Commands commands = new Commands(tasks, System.out);

		osInterface.setCommands(commands);

		File git_data = new File("git-data");

		boolean exists = git_data.exists();

		if (!exists) {
			boolean mkdir = git_data.mkdir();

			System.out.println(mkdir);

			osInterface.runGitCommand(new GitCommand("git init"));
			osInterface.runGitCommand(new GitCommand("git config user.email \"mightymalakai33@gmail.com\""));
			osInterface.runGitCommand(new GitCommand("git config user.name \"Andrew Auclair\""));
		}

		TaskReader reader = new TaskReader(osInterface);

		File[] files = git_data.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(".txt")) {
					Task task = reader.readTask("git-data/" + file.getName());

					tasks.addTask(task);
				}
			}
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
	}
}
