// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.io.*;
import java.util.List;
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
					parseCommand(tasks, commands, command);
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
	
	private static void parseCommand(Tasks tasks, Commands commands, String command) {
		if (command.startsWith("add")) {
			commands.execute(command);
		}
		else if (command.startsWith("start")) {
			String[] s = command.split(" ");
			int taskID = Integer.parseInt(s[1]);
			
			tasks.startTask(taskID);
			
			System.out.println("Started task: " + taskID);
		}
		else if (command.startsWith("stop")) {
			Task task = tasks.stopTask();
			
			System.out.println("Stopped task: " + task);
		}
		else if (command.startsWith("finish")) {
			Task task = tasks.finishTask();
			
			System.out.println("Finished task: " + task);
		}
		else if (command.equals("active")) {
			Task activeTask = tasks.getActiveTask();
			
			System.out.println("Active task: " + activeTask);
		}
		else if (command.equals("list")) {
			List<Task> tasksList = tasks.getTasks();
			
			Task activeTask = new Task(-1, "");
			try {
				activeTask = tasks.getActiveTask();
			}
			catch (RuntimeException ignored) {
			}
			
			for (Task task : tasksList) {
				System.out.print(task.id == activeTask.id ? "* " : "  ");
				System.out.println(task);
			}
			
			if (tasksList.size() == 0) {
				System.out.println("No tasks.");
			}
		}
		else {
			System.out.println("Unknown Command");
		}
	}
}
