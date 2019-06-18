// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import java.io.PrintStream;

class Commands {
	private final Tasks tasks;
	private final PrintStream output;

	Commands(Tasks tasks, PrintStream output) {
		this.tasks = tasks;
		this.output = output;
	}

	void execute(String command) {
		if (command.equals("finish")) {
			Task task = tasks.finishTask();

			output.println("Finished task " + task);
		}
		else if (command.startsWith("start")) {
			String[] s = command.split(" ");
			int taskID = Integer.parseInt(s[1]);

			Task task = tasks.startTask(taskID);

			output.println("Started task " + task);
		}
		else if (command.equals("stop")) {
			Task task = tasks.stopTask();
			
			output.println("Stopped task " + task);
		}
		else {
			String task = command.substring(5, command.length() - 1);

			int newTaskID = tasks.addTask(task);

			output.println("Added task " + newTaskID + " \"" + task + "\"");
		}
	}
}
