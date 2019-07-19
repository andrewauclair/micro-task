// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.command.*;
import org.jline.builtins.Completers.TreeCompleter.Node;

import java.io.PrintStream;
import java.util.*;

public class Commands {
	private final Tasks tasks;
	private final Map<String, Command> commands = new HashMap<>();
	
	private final Map<String, com.andrewauclair.todo.command.Command> newCommands = new HashMap<>();
	
	Commands(Tasks tasks) {
		this.tasks = tasks;
		
		newCommands.put("create-list", new ListCreateCommand(tasks));
		newCommands.put("switch-list", new ListSwitchCommand(tasks));
		newCommands.put("finish", new FinishCommand(tasks));
		newCommands.put("start", new StartCommand(tasks));
		newCommands.put("stop", new StopCommand(tasks));
		newCommands.put("add", new AddCommand(tasks));
		newCommands.put("active", new ActiveCommand(tasks));
		newCommands.put("list", new ListCommand(tasks));
		newCommands.put("times", new TimesCommand(tasks));
		newCommands.put("debug", new DebugCommand(tasks));
		newCommands.put("rename", new RenameCommand(tasks));
		newCommands.put("search", new SearchCommand(tasks));
		
		commands.put("clear", (output1, command) -> tasks.osInterface.clearScreen());
		commands.put("exit", (output1, command) -> tasks.osInterface.exit());
	}
	
	public void execute(PrintStream output, String command) {
		try {
			commands.keySet().stream()
					.filter(command::startsWith)
					.findFirst()
					.ifPresentOrElse(name -> commands.get(name).execute(output, command),
							() -> {
								Optional<String> newCommand = newCommands.keySet().stream()
										.filter(command::startsWith)
										.findFirst();
								
								if (newCommand.isPresent()) {
									newCommands.get(newCommand.get()).print(output, command);
								}
								else {
									output.println("Unknown command.");
									output.println();
								}
							});
		}
		catch (RuntimeException e) {
			output.println(e.getMessage());
			output.println();
		}
	}

	String getListName() {
		return tasks.getCurrentList();
	}

	boolean hasListWithName(String listName) {
		return tasks.hasListWithName(listName);
	}

	String getPrompt() {
		String prompt = tasks.getCurrentList() + " - ";
		try {
			prompt += tasks.getActiveTask().id;
		}
		catch (RuntimeException e) {
			prompt += "none";
		}
		return prompt + ">";
	}
	
	public List<Node> getAutoCompleteNodes() {
		List<Node> nodes = new ArrayList<>();
		
		for (com.andrewauclair.todo.command.Command value : newCommands.values()) {
			nodes.addAll(value.getAutoCompleteNodes());
		}
		
		return nodes;
	}
	
	public DebugCommand getDebugCommand() {
		return (DebugCommand) newCommands.get("debug");
	}
	
	private interface Command {
		void execute(PrintStream output, String command);
	}
}
