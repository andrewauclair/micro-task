// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Main;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.os.PicocliFactory;
import com.andrewauclair.todo.task.TaskFilterBuilder;
import com.andrewauclair.todo.task.Tasks;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Commands {
	private final Tasks tasks;

	public final Map<String, Command> commands = new HashMap<>();

	private final Map<String, String> aliases = new HashMap<>();
	private final GitLabReleases gitLabReleases;
	private final OSInterface osInterface;
	private CommandLine commandLine;
	private DefaultParser defaultParser = new DefaultParser();

	public Commands(Tasks tasks, GitLabReleases gitLabReleases, OSInterface osInterface) {
		this.tasks = tasks;

		this.gitLabReleases = gitLabReleases;
		this.osInterface = osInterface;
		setCommands(tasks, this.gitLabReleases, this.osInterface);

		setCommands(tasks, gitLabReleases, osInterface);

		commandLine = buildCommandLine();
	}

	private void setCommands(Tasks tasks, GitLabReleases gitLabReleases, OSInterface osInterface) {
//		commands.put("mklist", new ListCreateCommand(tasks));
//		commands.put("chlist", new ListSwitchCommand(tasks));
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
		commands.put("update", new UpdateCommand(gitLabReleases, tasks, this, osInterface));
		commands.put("exit", new ExitCommand(osInterface));
		commands.put("move", new MoveCommand(tasks));
		commands.put("set-task", new SetCommand.SetTaskCommand(tasks));
		commands.put("set-list", new SetCommand.SetListCommand(tasks));
		commands.put("set-group", new SetCommand.SetGroupCommand(tasks));
		commands.put("mk", new MakeCommand(tasks));
		commands.put("ch", new ChangeCommand(tasks));
		commands.put("eod", new EndOfDayCommand(tasks, osInterface));
		commands.put("git", new GitCommand(osInterface));
		commands.put("alias", new AliasCommand(this, osInterface));
		commands.put("next", new NextCommand(tasks));
	}

	public CommandLine buildCommandLine() {
		CommandLine cmdLine = new CommandLine(new Main.CliCommands(), new PicocliFactory(this, tasks));

		commands.keySet().forEach(name -> cmdLine.addSubcommand(name, commands.get(name)));

		aliases.keySet().forEach(name -> {

			Command cmd = new Command() {
				@Override
				public void run() {
					System.out.print(ConsoleColors.ANSI_BOLD);
					System.out.print(aliases.get(name));
					System.out.print(ConsoleColors.ANSI_RESET);
					System.out.println();

					Commands.this.execute(System.out, aliases.get(name));
				}
			};

			cmdLine.addSubcommand(name, cmd);
		});

		CommandLine.IExecutionExceptionHandler defaultHandler = cmdLine.getExecutionExceptionHandler();
		CommandLine.IParameterExceptionHandler defaultParamHandler = cmdLine.getParameterExceptionHandler();

		cmdLine.setExecutionExceptionHandler((ex, commandLine1, parseResult) -> {
			System.out.println(ex.getMessage());
			System.out.println();
			return defaultHandler.handleExecutionException(ex, commandLine1, parseResult);
		});

		cmdLine.setParameterExceptionHandler((ex, args) -> {
			System.out.println(ex.getMessage());
			System.out.println();
			return defaultParamHandler.handleParseException(ex, args);
		});

		return cmdLine;
	}

	public void execute(PrintStream output, String command) {
		System.setOut(output);

		ParsedLine parse = defaultParser.parse(command, 0, Parser.ParseContext.UNSPECIFIED);
		String[] strings = parse.words().toArray(new String[0]);

		commandLine.execute(strings);

		setCommands(tasks, this.gitLabReleases, this.osInterface);
		commandLine = buildCommandLine(); // rebuild
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

	public DebugCommand getDebugCommand() {
		return (DebugCommand) commands.get("debug");
	}

	public void loadAliases() {
		if (!osInterface.fileExists("git-data/aliases.txt")) {
			return;
		}

		try {
			Scanner scanner = new Scanner(osInterface.createInputStream("git-data/aliases.txt"));

			aliases.clear();

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				String[] split = line.split("=");

				String name = split[0];
				String command = split[1].substring(1, split[1].length() - 1);

				addAlias(name, command);
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	public void addAlias(String name, String command) {
		aliases.put(name, command);

		setCommands(tasks, this.gitLabReleases, this.osInterface);
		commandLine = buildCommandLine();
	}

	void removeAlias(String name) {
		aliases.remove(name);

		setCommands(tasks, this.gitLabReleases, this.osInterface);
		commandLine = buildCommandLine();
	}

	Map<String, String> getAliases() {
		return aliases;
	}

	TimesCommand getTimesCommand() {
		return (TimesCommand) commands.get("times");
	}

	// TODO This is not making things any better, it's to support getTimesCommand in the tests correctly
	void updateTimesCommand() {
		commandLine = buildCommandLine();
	}
}
