// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		Tasks tasks = new Tasks();
		
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
					parseCommand(tasks, command);
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
		BufferedOutputStream outputStream = new BufferedOutputStream(output);
		
		for (Tasks.Task task : tasks.getTasks()) {
			output.write(task.task.getBytes());
			output.write("\n".getBytes());
		}
	}
	
	private static void parseCommand(Tasks tasks, String command) {
		if (command.startsWith("add")) {
			String task = command.substring(5, command.length() - 1);
			
			int newTaskID = tasks.addTask(task);
			
			System.out.println("Added task " + newTaskID + " \"" + task + "\"");
		}
		else if (command.startsWith("start")) {
			String[] s = command.split(" ");
			int taskID = Integer.parseInt(s[1]);
			
			tasks.startTask(taskID);
			
			System.out.println("Started task: " + taskID);
		}
		else if (command.startsWith("finish")) {
			Tasks.Task task = tasks.finishTask();
			
			System.out.println("Finished task: " + task);
		}
		else if (command.equals("active")) {
			Tasks.Task activeTask = tasks.getActiveTask();
			
			System.out.println("Active task: " + activeTask);
		}
		else if (command.equals("list")) {
			List<Tasks.Task> tasksList = tasks.getTasks();
			
			Tasks.Task activeTask = new Tasks.Task(-1, "");
			try {
				activeTask = tasks.getActiveTask();
			}
			catch (RuntimeException ignored) {
			}
			
			for (Tasks.Task task : tasksList) {
				System.out.print(task.id == activeTask.id ? "* " : "  ");
				System.out.println(task.task);
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
