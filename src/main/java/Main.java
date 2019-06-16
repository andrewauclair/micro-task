// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Tasks tasks = new Tasks();
		
		String command;
		Scanner scanner = new Scanner(System.in);
		do {
			command = scanner.nextLine();
			
			if (command.startsWith("add")) {
				String[] s = command.split(" ");
				
				String task = command.substring(5, command.length() - 1);//s[1].substring(1, s[1].length() - 1);
				
				int newTaskID = tasks.addTask(task);
				
				System.out.println("Added task " + newTaskID + " \"" + task + "\"");
			}
			else if (command.startsWith("start")) {
				String[] s = command.split(" ");
				int taskID = Integer.parseInt(s[1]);
				
				tasks.startTask(taskID);
				
				System.out.println("Started task: " + taskID);
			}
			else if (command.equals("active")) {
				int activeTask = tasks.getActiveTask();
				
				if (activeTask == -1) {
					System.out.println("No active task");
				}
				else {
					System.out.println("Active task: " + activeTask);
				}
			}
			else {
				System.out.println("Unknown Command");
			}
		}
		while (!command.equals("exit"));
	}
}
