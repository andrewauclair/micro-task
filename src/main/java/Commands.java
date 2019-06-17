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
		String task = command.substring(5, command.length() - 1);
		
		int newTaskID = tasks.addTask(task);
		
		output.println("Added task " + newTaskID + " \"" + task + "\"");
	}
}
