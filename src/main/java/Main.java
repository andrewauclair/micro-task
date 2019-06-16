// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.util.List;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Tasks tasks = new Tasks();
		
		String command;
		Scanner scanner = new Scanner(System.in);
		do {
			command = scanner.nextLine();
			
			try {
				parseCommand(tasks, command);
			}
			catch (RuntimeException e) {
				System.out.println(e.getMessage());
			}
		}
		while (!command.equals("exit"));
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
			String[] s = command.split(" ");
			int taskID = Integer.parseInt(s[1]);
			
			tasks.finishTask(taskID);
			
			System.out.println("Finished task: " + taskID);
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
