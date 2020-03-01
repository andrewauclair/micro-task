// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Main;
import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.os.PicocliFactory;
import com.andrewauclair.todo.task.Tasks;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("CanBeFinal")
public class Commands implements CommandLine.IExecutionExceptionHandler {
	private final Tasks tasks;

	private final Map<String, Runnable> commands = new HashMap<>();

	private final Map<String, String> aliases = new HashMap<>();
	private final GitLabReleases gitLabReleases;
	private final OSInterface osInterface;
	private final DefaultParser defaultParser = new DefaultParser();
	private final PicocliFactory factory;
	private final Main.CliCommands cliCommands = new Main.CliCommands();
	private CommandLine.IExecutionExceptionHandler defaultHandler;

	public Commands(Tasks tasks, GitLabReleases gitLabReleases, OSInterface osInterface) {
		this.tasks = tasks;

		this.gitLabReleases = gitLabReleases;
		this.osInterface = osInterface;
		factory = new PicocliFactory(this, tasks);
		setCommands(tasks, this.gitLabReleases, this.osInterface);
	}

	private void setCommands(Tasks tasks, GitLabReleases gitLabReleases, OSInterface osInterface) {
		commands.put("finish", new FinishCommand(tasks, osInterface));
		commands.put("start", new StartCommand(tasks, osInterface));
		commands.put("stop", new StopCommand(tasks, osInterface));
		commands.put("add", new AddCommand(tasks, this));
		commands.put("active", new ActiveCommand(tasks, osInterface));
		commands.put("list", new ListCommand(tasks, osInterface));
		commands.put("times", new TimesCommand(tasks, osInterface));//, new TaskFilterBuilder()));
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
		commands.put("alias", new AliasCommand(this, osInterface));
		commands.put("next", new NextCommand(tasks));
		commands.put("info", new InfoCommand(tasks, osInterface));
	}

	private Runnable createCommand(String command) {
		switch (command) {
			case "finish":
				return new FinishCommand(tasks, osInterface);
			case "start":
				return new StartCommand(tasks, osInterface);
			case "stop":
				return new StopCommand(tasks, osInterface);
			case "add":
				return new AddCommand(tasks, this);
			case "active":
				return new ActiveCommand(tasks, osInterface);
			case "list":
				return new ListCommand(tasks, osInterface);
			case "times":
				return new TimesCommand(tasks, osInterface);
			case "debug":
				return new DebugCommand();
			case "rename":
				return new RenameCommand(tasks);
			case "search":
				return new SearchCommand(tasks);
			case "version":
				return new VersionCommand(osInterface);
			case "update":
				return new UpdateCommand(gitLabReleases, tasks, this, osInterface);
			case "exit":
				return new ExitCommand(osInterface);
			case "move":
				return new MoveCommand(tasks);
			case "set-task":
				return new SetCommand.SetTaskCommand(tasks);
			case "set-list":
				return new SetCommand.SetListCommand(tasks);
			case "set-group":
				return new SetCommand.SetGroupCommand(tasks);
			case "mk":
				return new MakeCommand(tasks);
			case "ch":
				return new ChangeCommand(tasks);
			case "eod":
				return new EndOfDayCommand(tasks, osInterface);
			case "alias":
				return new AliasCommand(this, osInterface);
			case "next":
				return new NextCommand(tasks);
			case "info":
				return new InfoCommand(tasks, osInterface);
		}

		for (String alias : aliases.keySet()) {
			if (command.equals(alias)) {
				return new Runnable() {
					@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
					private boolean help;

					@Override
					public void run() {
						System.out.print(ConsoleColors.ANSI_BOLD);
						System.out.print(aliases.get(alias));
						System.out.print(ConsoleColors.ANSI_RESET);
						System.out.println();

						Commands.this.execute(System.out, aliases.get(alias));
					}
				};
			}
		}

		return null;
	}

	public CommandLine buildCommandLineWithAllCommands() {
		CommandLine cmdLine = new CommandLine(new Main.CliCommands(), new PicocliFactory(this, tasks));

		cmdLine.setTrimQuotes(true);

		commands.keySet().forEach(name -> cmdLine.addSubcommand(name, commands.get(name)));

		aliases.keySet().forEach(name -> {

			Runnable cmd = new Runnable() {
				@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
				private boolean help;

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

		setHandlers(cmdLine);

		return cmdLine;
	}

	private void setHandlers(CommandLine cmdLine) {
		defaultHandler = cmdLine.getExecutionExceptionHandler();
		CommandLine.IParameterExceptionHandler defaultParamHandler = cmdLine.getParameterExceptionHandler();

		cmdLine.setExecutionExceptionHandler(this);

		cmdLine.setParameterExceptionHandler((ex, args) -> {
			System.out.println(ex.getMessage());
			System.out.println();
			return defaultParamHandler.handleParseException(ex, args);
		});
	}

	public CommandLine buildCommandLine(String command) {
		CommandLine cmdLine = new CommandLine(cliCommands, factory);

		cmdLine.setTrimQuotes(true);

		Runnable realCommand = createCommand(command);

		if (realCommand == null) {
			return buildCommandLineWithAllCommands();
		}

		cmdLine.addSubcommand(command, realCommand);

		setHandlers(cmdLine);

		return cmdLine;
	}

	public void execute(PrintStream output, String command) {
		System.setOut(output);

		ParsedLine parse = defaultParser.parse(command, 0, Parser.ParseContext.UNSPECIFIED);
		String[] strings = parse.words().toArray(new String[0]);

		CommandLine commandLine = buildCommandLine(strings[0]); // rebuild
		commandLine.execute(strings);
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

		try (Scanner scanner = new Scanner(osInterface.createInputStream("git-data/aliases.txt"))) {
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
	}

	void removeAlias(String name) {
		aliases.remove(name);
	}

	Map<String, String> getAliases() {
		return aliases;
	}

	@Override
	public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) throws Exception {
		System.out.println(ex.getMessage());
		System.out.println();
		if (!(ex instanceof TaskException)) {
			return defaultHandler.handleExecutionException(ex, commandLine, parseResult);
		}
		return 0;
	}
}
