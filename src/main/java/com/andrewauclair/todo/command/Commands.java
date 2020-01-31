// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.TaskFilterBuilder;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers.TreeCompleter.Node;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands {
	private final Tasks tasks;
	
	private final Map<String, Command> commands = new HashMap<>();
	
	private final Map<String, String> aliases = new HashMap<>();
	
	public Commands(Tasks tasks, GitLabReleases gitLabReleases, OSInterface osInterface) {
		this.tasks = tasks;
		
		commands.put("mklist", new ListCreateCommand(tasks));
		commands.put("chlist", new ListSwitchCommand(tasks));
		commands.put("finish", new FinishCommand(tasks, osInterface));
		commands.put("start", new StartCommand(tasks, osInterface));
		commands.put("stop", new StopCommand(tasks, osInterface));
		commands.put("add", new AddCommand(tasks, this));
		commands.put("active", new ActiveCommand(tasks, osInterface));
		commands.put("list", new ListCommand(tasks));
		commands.put("times", new TimesCommand(tasks, osInterface, new TaskFilterBuilder()));
		commands.put("debug", new DebugCommand());
		commands.put("rename", new RenameCommand(tasks));
		commands.put("search", new SearchCommand(tasks));
		commands.put("version", new VersionCommand(osInterface));
		commands.put("update", new UpdateCommand(gitLabReleases, tasks, osInterface));
		commands.put("exit", new ExitCommand(osInterface));
		commands.put("move", new MoveCommand(tasks));
		commands.put("set", new SetCommand(tasks));
		commands.put("mkgrp", new GroupCreateCommand(tasks));
		commands.put("chgrp", new GroupSwitchCommand(tasks));
		commands.put("eod", new EndOfDayCommand(tasks, osInterface));
		commands.put("git", new GitCommand(osInterface));
		commands.put("alias", new AliasCommand(this, osInterface));
		commands.put("next", new NextCommand(tasks));
	}
	
	public void execute(PrintStream output, String command) {
		try {
			commands.keySet().stream()
					.filter(command::startsWith)
					.findFirst()
					.ifPresentOrElse(name -> commands.get(name).execute(output, command),
							() -> aliases.keySet().stream()
									.filter(command::equals)
									.findFirst()
									.ifPresentOrElse(name -> {
										output.print(ConsoleColors.ANSI_BOLD);
										output.print(aliases.get(name));
										output.print(ConsoleColors.ANSI_RESET);
										output.println();
										execute(output, aliases.get(name));
									}, () -> unknownCommand(output)));
		}
		catch (RuntimeException e) {
			if (e instanceof TaskException) {
				output.println(e.getMessage());
			}
			else {
				e.printStackTrace(output);
			}
			output.println();
			
			if (output != System.out && !(e instanceof TaskException)) {
				e.printStackTrace(System.err);
				System.out.println();
			}
		}
	}
	
	private void unknownCommand(PrintStream output) {
		output.println("Unknown command.");
		output.println();
	}
	
	public String getPrompt() {
		String prompt = "";
		
		if (tasks.getGroupForList(tasks.getActiveList()).equals(tasks.getActiveGroup())) {
			prompt += tasks.getActiveList();
		}
		else {
			prompt += tasks.getActiveGroup().getFullPath();
		}
		
		prompt += " - ";
		prompt += tasks.hasActiveTask() ? tasks.getActiveTaskID() : "none";
		
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
	
	public void addAlias(String name, String command) {
		aliases.put(name, command);
	}
	
	void removeAlias(String name) {
		aliases.remove(name);
	}
	
	Map<String, String> getAliases() {
		return aliases;
	}
	
	TimesCommand getTimesCommand() {
		return (TimesCommand) commands.get("times");
	}
}
