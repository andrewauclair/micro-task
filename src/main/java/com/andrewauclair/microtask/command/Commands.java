// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.command.group.AddGroupCommand;
import com.andrewauclair.microtask.command.group.RenameGroupCommand;
import com.andrewauclair.microtask.command.group.SetGroupCommand;
import com.andrewauclair.microtask.command.group.StartGroupCommand;
import com.andrewauclair.microtask.command.list.AddListCommand;
import com.andrewauclair.microtask.command.list.RenameListCommand;
import com.andrewauclair.microtask.command.list.SetListCommand;
import com.andrewauclair.microtask.command.list.StartListCommand;
import com.andrewauclair.microtask.command.project.*;
import com.andrewauclair.microtask.command.tags.StartTagsCommand;
import com.andrewauclair.microtask.command.task.AddTaskCommand;
import com.andrewauclair.microtask.command.task.RenameTaskCommand;
import com.andrewauclair.microtask.command.task.SetTaskCommand;
import com.andrewauclair.microtask.command.task.StartTaskCommand;
import com.andrewauclair.microtask.os.*;
import com.andrewauclair.microtask.picocli.*;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.NewID;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("CanBeFinal")
public class Commands implements CommandLine.IExecutionExceptionHandler {
	private final Tasks tasks;
	private final Projects projects;

	private final Map<String, Runnable> commands = new HashMap<>();

	private final Map<String, String> aliases = new HashMap<>();
	private final GitLabReleases gitLabReleases;
	private final LocalSettings localSettings;
	private final OSInterface osInterface;
	private final DefaultParser defaultParser = new DefaultParser();
	private final PicocliFactory factory;
	private final MainConsole.CliCommands cliCommands = new MainConsole.CliCommands();
	private CommandLine.IExecutionExceptionHandler defaultHandler;

	public Commands(Tasks tasks, Projects projects, GitLabReleases gitLabReleases, LocalSettings localSettings, OSInterface osInterface) {
		this.tasks = tasks;
		this.projects = projects;

		this.gitLabReleases = gitLabReleases;
		this.localSettings = localSettings;
		this.osInterface = osInterface;
		factory = new PicocliFactory(this, tasks, projects, osInterface);
		setCommands(tasks, this.osInterface);
	}

	private void setCommands(Tasks tasks, OSInterface osInterface) {
		commands.put("finish", new FinishCommand(tasks, osInterface));
//		commands.put("start", new StartCommand(tasks, osInterface));
		commands.put("stop", new StopCommand(tasks, osInterface));
//		commands.put("add", new AddCommand(tasks, this));
		commands.put("active", new ActiveCommand(tasks, osInterface));
		commands.put("list", new ListCommand(tasks, osInterface));
		commands.put("times", new TimesCommand(tasks, projects, osInterface));//, new TaskFilterBuilder()));
		commands.put("debug", new DebugCommand(localSettings));
//		commands.put("rename", new RenameCommand(tasks));
		commands.put("search", new SearchCommand(tasks));
		commands.put("version", new VersionCommand(osInterface));
//		commands.put("update", new UpdateCommand(tasks, this, localSettings, osInterface));
		commands.put("exit", new ExitCommand(osInterface));
		commands.put("move", new MoveCommand(tasks));
//		commands.put("set-task", new SetCommand.SetTaskCommand(tasks));
//		commands.put("set-list", new SetCommand.SetListCommand(tasks));
//		commands.put("set-group", new SetCommand.SetGroupCommand(tasks));
//		commands.put("mk", new MakeCommand(tasks, projects));
		commands.put("ch", new ChangeCommand(tasks, projects, localSettings));
		commands.put("day", new DayCommand(tasks, localSettings, osInterface));
		commands.put("alias", new AliasCommand(this, osInterface));
		commands.put("next", new NextCommand(tasks, osInterface));
		commands.put("info", new InfoCommand(tasks, projects, osInterface));
		commands.put("focus", new FocusCommand(osInterface));
		commands.put("status", new StatusCommand(this, osInterface));
		commands.put("project", new ProjectCommand(tasks, projects, localSettings, osInterface));
		commands.put("group", new GroupCommand(tasks));
		commands.put("tasks", new TasksCommand(tasks, projects, osInterface));
		commands.put("milestone", new MilestoneCommand(projects));
	}

	private CommandLine createCommand(CommandLine cmdLine, String command) {
//		CommandLine cmdLine = new CommandLine(cliCommands, factory);

		Runnable runnable;
		switch (command) {
			case "add":
				cmdLine.addSubcommand("add",
						new CommandLine(new AddCommand(), factory)
								.addSubcommand(new AddTaskCommand(tasks, this, osInterface))
								.addSubcommand(new AddListCommand(tasks))
								.addSubcommand(new AddGroupCommand(tasks))
								.addSubcommand(new AddProjectCommand(projects))
								.addSubcommand(new AddFeatureCommand(tasks, projects))
								.addSubcommand(new AddMilestoneCommand(projects))
				);
				break;
			case "start":
				cmdLine.addSubcommand("start",
						new CommandLine(new StartCommand(), factory)
								.addSubcommand(new StartTaskCommand(tasks, osInterface))
								.addSubcommand(new StartListCommand(tasks))
								.addSubcommand(new StartGroupCommand(tasks))
								.addSubcommand(new StartProjectCommand(tasks, projects))
								.addSubcommand(new StartFeatureCommand(tasks, projects))
								.addSubcommand(new StartMilestoneCommand(tasks, projects))
								.addSubcommand(new StartTagsCommand(tasks))
				);
				break;
			case "update":
				cmdLine.addSubcommand("update",
						new CommandLine(new UpdateCommand(tasks, this, localSettings, osInterface), factory)
								.addSubcommand(new UpdateAppCommand(gitLabReleases, osInterface))
								.addSubcommand(new UpdateRepoCommand(tasks, osInterface, localSettings, projects, this))
				);
				break;
			case "set":
				cmdLine.addSubcommand("set",
						new CommandLine(new SetCommand(), factory)
								.addSubcommand(new SetTaskCommand(tasks, osInterface))
								.addSubcommand(new SetListCommand(tasks, osInterface))
								.addSubcommand(new SetGroupCommand(tasks, osInterface))
				);
				break;
			case "rename":
				cmdLine.addSubcommand("rename",
						new CommandLine(new RenameCommand(), factory)
								.addSubcommand(new RenameTaskCommand(tasks))
								.addSubcommand(new RenameListCommand(tasks))
								.addSubcommand(new RenameGroupCommand(tasks))
				);
				break;
		}

		if (command.equals("json")) {
			cmdLine.addSubcommand("json", new Runnable() {
				@Option(names = {"--obj"}
				)
				private boolean obj;

				@Option(names = {"-c"})
				private String command;

				@Override
				public void run() {
					try {
						if (obj) {
							System.out.println(GitLabReleases.getJSONObject(command, Proxy.NO_PROXY));
						}
						else {
							System.out.println(GitLabReleases.getJSONArray(command, Proxy.NO_PROXY));
						}
					}
					catch (IOException e) {
						e.printStackTrace(System.out);
					}
				}
			});
		}

		return cmdLine;
	}

	private Runnable createCommandOld(String command) {
		switch (command) {
			case "finish":
				return new FinishCommand(tasks, osInterface);
//			case "start":
//				return new StartCommand(tasks, osInterface);
			case "stop":
				return new StopCommand(tasks, osInterface);
//			case "add":
//				return new AddCommand(tasks, this);
			case "active":
				return new ActiveCommand(tasks, osInterface);
			case "list":
				return new ListCommand(tasks, osInterface);
			case "times":
				return new TimesCommand(tasks, projects, osInterface);
			case "debug":
				return new DebugCommand(localSettings);
//			case "rename":
//				return new RenameCommand(tasks);
			case "search":
				return new SearchCommand(tasks);
			case "version":
				return new VersionCommand(osInterface);
			case "update":
				return new UpdateCommand(tasks, this, localSettings, osInterface);
			case "exit":
				return new ExitCommand(osInterface);
			case "move":
				return new MoveCommand(tasks);
//			case "set-task":
//				return new SetCommand.SetTaskCommand(tasks);
//			case "set-list":
//				return new SetCommand.SetListCommand(tasks);
//			case "set-group":
//				return new SetCommand.SetGroupCommand(tasks);
//			case "mk":
//				return new MakeCommand(tasks, projects);
			case "ch":
				return new ChangeCommand(tasks, projects, localSettings);
			case "day":
				return new DayCommand(tasks, localSettings, osInterface);
			case "alias":
				return new AliasCommand(this, osInterface);
			case "next":
				return new NextCommand(tasks, osInterface);
			case "info":
				return new InfoCommand(tasks, projects, osInterface);
			case "focus":
				return new FocusCommand(osInterface);
			case "status":
				return new StatusCommand(this, osInterface);
			case "project":
				return new ProjectCommand(tasks, projects, localSettings, osInterface);
			case "datagen":
				return new RandomDataGenerator(tasks);
			case "group":
				return new GroupCommand(tasks);
			case "tasks":
				return new TasksCommand(tasks, projects, osInterface);
			case "milestone":
				return new MilestoneCommand(projects);
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

		if (command.equals("json")) {
			return new Runnable() {
				@Option(names = {"--obj"}
				)
				private boolean obj;

				@Option(names = {"-c"})
				private String command;

				@Override
				public void run() {
					try {
						if (obj) {
							System.out.println(GitLabReleases.getJSONObject(command, Proxy.NO_PROXY));
						}
						else {
							System.out.println(GitLabReleases.getJSONArray(command, Proxy.NO_PROXY));
						}
					}
					catch (IOException e) {
						e.printStackTrace(System.out);
					}
				}
			};
		}

		return null;
	}

	public CommandLine buildCommandLineWithoutAliases() {
		CommandLine cmdLine = new CommandLine(new MainConsole.CliCommands(), new PicocliFactory(this, tasks, projects, osInterface));

		commands.keySet().forEach(name -> cmdLine.addSubcommand(name, commands.get(name)));

		return cmdLine;
	}

	public CommandLine buildCommandLineWithAllCommands() {
		CommandLine cmdLine = new CommandLine(new MainConsole.CliCommands(), new PicocliFactory(this, tasks, projects, osInterface));

		commands.keySet().forEach(name -> cmdLine.addSubcommand(name, commands.get(name)));

		createCommand(cmdLine, "update");
		createCommand(cmdLine, "set");
		createCommand(cmdLine, "rename");
		createCommand(cmdLine, "add");
		createCommand(cmdLine, "start");

//		aliases.keySet().forEach(name -> {
//
//			Runnable cmd = new Runnable() {
//				@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
//				private boolean help;
//
//				@Override
//				public void run() {
//					System.out.print(ConsoleColors.ANSI_BOLD);
//					System.out.print(aliases.get(name));
//					System.out.print(ConsoleColors.ANSI_RESET);
//					System.out.println();
//
//					Commands.this.execute(System.out, aliases.get(name));
//
//				}
//			};
//
//			cmdLine.addSubcommand(name, cmd);
//		});

		// has to be done after we add the subcommands
		cmdLine.setTrimQuotes(true);

		setHandlers(cmdLine);

		cmdLine.registerConverter(ExistingListName.class, s -> new ExistingListNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(NewTaskListName.class, s -> new NewTaskListNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(ExistingGroupName.class, s -> new ExistingGroupNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(NewTaskGroupName.class, s -> new NewTaskGroupNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(ExistingID.class, s -> new ExistingIDTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(NewID.class, s -> new NewIDTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(ExistingProject.class, s -> new ExistingProjectNameTypeConverter(projects).convert(s));
		cmdLine.registerConverter(NewProject.class, s -> new NewProjectNameTypeConverter(projects).convert(s));

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

		if (command.equals("update") || command.equals("set") || command.equals("rename") || command.equals("add") || command.equals("start")) {
			createCommand(cmdLine, command);
		}
		else {
			Runnable realCommand = createCommandOld(command);

			if (realCommand == null) {
				return buildCommandLineWithAllCommands();
			}

			cmdLine.addSubcommand(command, realCommand);
		}

		// has to be done after we add the subcommands
		cmdLine.setTrimQuotes(true);

		setHandlers(cmdLine);

		cmdLine.registerConverter(ExistingListName.class, s -> new ExistingListNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(NewTaskListName.class, s -> new NewTaskListNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(ExistingGroupName.class, s -> new ExistingGroupNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(NewTaskGroupName.class, s -> new NewTaskGroupNameTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(ExistingID.class, s -> new ExistingIDTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(NewID.class, s -> new NewIDTypeConverter(tasks).convert(s));
		cmdLine.registerConverter(ExistingProject.class, s -> new ExistingProjectNameTypeConverter(projects).convert(s));
		cmdLine.registerConverter(NewProject.class, s -> new NewProjectNameTypeConverter(projects).convert(s));

		return cmdLine;
	}

	public void execute(PrintStream output, String command) {
		System.setOut(output);

		ParsedLine parse = defaultParser.parse(command, 0, Parser.ParseContext.UNSPECIFIED);
		String[] strings = parse.words().toArray(new String[0]);

		if (strings.length == 1 && strings[0].isEmpty()) {
			strings[0] = "help";
		}
		CommandLine commandLine = buildCommandLine(strings[0]); // rebuild
		commandLine.execute(strings);
	}

	public boolean isValidCommand(String command) {
		ParsedLine parse = defaultParser.parse(command, 0, Parser.ParseContext.UNSPECIFIED);
		String[] strings = parse.words().toArray(new String[0]);

		CommandLine commandLine = buildCommandLine(strings[0]); // rebuild

		try {
			commandLine.parseArgs(strings);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}

	public String getPrompt() {
		String prompt = "";

		if (tasks.getGroupForList(tasks.getCurrentList()).equals(tasks.getCurrentGroup())) {
			prompt += tasks.getCurrentList();
		}
		else {
			prompt += tasks.getCurrentGroup().getFullPath();
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

	public Map<String, String> getAliases() {
		return aliases;
	}

	@Override
	public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) throws Exception {
		System.out.println(ex.getMessage());
		System.out.println();

		if (!(ex instanceof TaskException) || localSettings.isDebugEnabled()) {
			return defaultHandler.handleExecutionException(ex, commandLine, parseResult);
		}
		return 0;
	}
}
