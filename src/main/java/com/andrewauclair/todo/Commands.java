// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.command.*;
import com.andrewauclair.todo.os.GitLabReleases;
import org.jline.builtins.Completers.TreeCompleter.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands {
	private final Tasks tasks;

	private final Map<String, Command> commands = new HashMap<>();

	Commands(Tasks tasks, GitLabReleases gitLabReleases) {
		this.tasks = tasks;

		commands.put("create-list", new ListCreateCommand(tasks));
		commands.put("switch-list", new ListSwitchCommand(tasks));
		commands.put("finish", new FinishCommand(tasks));
		commands.put("start", new StartCommand(tasks));
		commands.put("stop", new StopCommand(tasks));
		commands.put("add", new AddCommand(tasks));
		commands.put("active", new ActiveCommand(tasks));
		commands.put("list", new ListCommand(tasks));
		commands.put("times", new TimesCommand(tasks));
		commands.put("debug", new DebugCommand());
		commands.put("rename", new RenameCommand(tasks));
		commands.put("search", new SearchCommand(tasks));
		commands.put("version", new VersionCommand(tasks));
		commands.put("update", new UpdateCommand(gitLabReleases));
		commands.put("clear", new ClearCommand(tasks));
		commands.put("exit", new ExitCommand(tasks));
		commands.put("move", new MoveCommand(tasks));
	}

	public void execute(PrintStream output, String command) {
		try {
			commands.keySet().stream()
					.filter(command::startsWith)
					.findFirst()
					.ifPresentOrElse(name -> commands.get(name).execute(output, command),
							() -> unknownCommand(output));
		}
		catch (RuntimeException e) {
			output.println(e.getMessage());
			output.println();
		}
	}

	private void unknownCommand(PrintStream output) {
		output.println("Unknown command.");
		output.println();
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

		for (com.andrewauclair.todo.command.Command value : commands.values()) {
			nodes.addAll(value.getAutoCompleteNodes());
		}

		return nodes;
	}

	public DebugCommand getDebugCommand() {
		return (DebugCommand) commands.get("debug");
	}
}
