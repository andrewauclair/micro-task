// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.os.OSInterfaceImpl;
import com.andrewauclair.todo.task.*;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.jline.builtins.Builtins;
import org.jline.builtins.Completers;
import org.jline.builtins.Widgets;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Status;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.HelpCommand;

public final class Main {
	private static final char BACKSPACE_KEY = '\u0008';
	private static final AtomicBoolean runningCommand = new AtomicBoolean(false);

	private static final OSInterfaceImpl osInterface = new OSInterfaceImpl();

	private final Tasks tasks;

	private Main() throws Exception {
		tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		Commands commands = new Commands(tasks, new GitLabReleases(), osInterface);

		File git_data = new File("git-data");

		boolean exists = git_data.exists();

		if (!exists) {
			boolean mkdir = git_data.mkdir();

			System.out.println(mkdir);

			osInterface.runGitCommand("git init", false);
			osInterface.runGitCommand("git config user.email \"git@todo.app\"", false);
			osInterface.runGitCommand("git config user.name \"TODO App\"", false);
		}

		boolean loadSuccessful = tasks.load(new TaskLoader(tasks, new TaskReader(osInterface), osInterface), commands);

		if (requiresTaskUpdate()) {
			commands.execute(System.out, "update --tasks");
		}

		Builtins builtins = new Builtins(Paths.get(""), null, null);
		builtins.rename(org.jline.builtins.Builtins.Command.TTOP, "top");
		builtins.alias("zle", "widget");
		builtins.alias("bindkey", "keymap");
		Completers.SystemCompleter systemCompleter = builtins.compileCompleters();
		// set up picocli commands
//		CliCommands cliCommands = new CliCommands();
		CommandLine cmd = commands.buildCommandLineWithAllCommands();
		PicocliCommands picocliCommands = new PicocliCommands(Paths.get(""), cmd);
		systemCompleter.add(picocliCommands.compileCompleters());
		systemCompleter.compile();

		Terminal terminal = TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.streams(System.in, System.out)
				.build();

		osInterface.setTerminal(terminal);

		System.setIn(terminal.input());
		System.setOut(new PrintStream(terminal.output()));

		LineReader lineReader = buildLineReader(systemCompleter, terminal);

		builtins.setLineReader(lineReader);
		bindCtrlBackspace(lineReader);

		DescriptionGenerator descriptionGenerator = new DescriptionGenerator(builtins, picocliCommands);
		new Widgets.TailTipWidgets(lineReader, descriptionGenerator::commandDescription, 5, Widgets.TailTipWidgets.TipType.COMPLETER);

		Status status = Status.getStatus(terminal);
		status.setBorder(true);


		if (loadSuccessful) {
			lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();
		}

		if (tasks.getActiveTaskID() != Tasks.NO_ACTIVE_TASK) {
			// set active list to the list of the active task
			tasks.setActiveList(tasks.getActiveTaskList());

			// set active group to the group of the active task
			tasks.switchGroup(tasks.getGroupForList(tasks.getActiveTaskList()).getFullPath());
		}

		updateStatus(status, terminal);

		Timer timer = new Timer();

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (!runningCommand.get()) {
					updateStatus(status, terminal);
				}
			}
		};

		boolean hasActiveTask = tasks.hasActiveTask();

		if (hasActiveTask) {
			timer.schedule(timerTask, 1000, 30000);
		}

		while (true) {
			try {
				String command = lineReader.readLine(commands.getPrompt());

				if (command.equals("proj-feat-assign")) {
					manualProjectFeatureAssign(tasks);
				}

				if (command.equals("export")) {
					exportData(tasks);
				}
				else if (command.equals("test-data")) {
					generateTestData(tasks);
				}
				else {
					runningCommand.set(true);
					commands.execute(System.out, command);
					runningCommand.set(false);
				}

				if (tasks.hasActiveTask()) {
					timerTask.cancel();
					timerTask = new TimerTask() {
						@Override
						public void run() {
							if (!runningCommand.get()) {
								updateStatus(status, terminal);
							}
						}
					};
					timer.schedule(timerTask, 1000, 30000);
				}
				else {
					timerTask.cancel();
				}
				hasActiveTask = tasks.hasActiveTask();

				updateStatus(status, terminal);

				// set up picocli commands
//				cliCommands = new CliCommands();
//				cmd = commands.buildCommandLine();
//				picocliCommands = new CustomPicocliCommands(Paths.get(""), cmd);
//
//				systemCompleter = builtins.compileCompleters();
//				systemCompleter.add(picocliCommands.compileCompleters());
//				systemCompleter.compile();
//
//				lineReader = buildLineReader(systemCompleter, terminal);
//
//				builtins.setLineReader(lineReader);
//				cliCommands.setReader(lineReader);
//				bindCtrlBackspace(lineReader);
			}
			catch (UserInterruptException ignored) {
			}
			catch (EndOfFileException e) {
				return;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}

	private static LineReader buildLineReader(Completers.SystemCompleter systemCompleter, Terminal terminal) {
		return LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(systemCompleter)
				.parser(new DefaultParser())
				.variable(LineReader.LIST_MAX, 50)
				.variable(LineReader.BELL_STYLE, "none")
				.build();
	}

	public long getElapsedTime(Task task) {
		long total = 0;
		for (TaskTimes time : task.getStartStopTimes()) {
			if (time.stop != TaskTimes.TIME_NOT_SET) {
				total += time.stop - time.start;
			}
			else {
				total += osInterface.currentSeconds() - time.start;
			}
		}
		return total;
	}

	private void updateStatus(Status status, Terminal terminal) {
//		if (false ) {// used when I want to run the app from IntelliJ
		synchronized (tasks) {
			int width = terminal.getSize().getColumns();

			List<AttributedString> as = new ArrayList<>();

			if (tasks.hasActiveTask()) {
				String description = tasks.getActiveTask().description();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				new PrintStream(stream).print(Utils.formatTime(getElapsedTime(tasks.getActiveTask()), Utils.HighestTime.None));
				String time = new String(stream.toByteArray(), StandardCharsets.UTF_8);

				if (width < description.length() + time.length()) {
					int length = width - time.length() - 3;

					description = description.substring(0, length - 3);
					description += "...'";
				}
				description += String.join("", Collections.nCopies(width - description.length() - time.length(), " "));
				description += time;

				as.add(new AttributedString(padString(terminal, description)));
				as.add(new AttributedString(padString(terminal, "Active Task Group: " + tasks.getGroupForList(tasks.getActiveTaskList()).getFullPath() + "    Active Task List: " + tasks.getActiveTaskList())));
				as.add(new AttributedString(padString(terminal, "Current Group: " + tasks.getActiveGroup().getFullPath() + "  Current List: " + tasks.getActiveList())));
			}
			else {
				as.add(new AttributedString(padString(terminal, "No active task")));
				as.add(new AttributedString(padString(terminal, "")));
				as.add(new AttributedString(padString(terminal, "Current Group: " + tasks.getActiveGroup().getFullPath() + "  Current List: " + tasks.getActiveList())));
			}

			status.update(as);
		}
//		}
	}

	private String padString(Terminal terminal, String str) {
		int width = terminal.getSize().getColumns();
		return str + String.join("", Collections.nCopies(width - str.length(), " "));
	}

	private void bindCtrlBackspace(LineReader lineReader) {
		KeyMap<Binding> main = lineReader.getKeyMaps().get(LineReader.MAIN);

		Widget widget = lineReader.getBuiltinWidgets().get(LineReader.BACKWARD_KILL_WORD);

		main.bind((Widget) () -> {
			short keyState = User32.INSTANCE.GetAsyncKeyState(WinUser.VK_LCONTROL);

			if ((keyState & 0x8000) == 0) {
				return lineReader.getBuffer().backspace();
			}
			else {
				return widget.apply();
			}
		}, KeyMap.ctrl(BACKSPACE_KEY));
	}

	// temporary function that we will use to assign a project and feature to all existing task times at work
	// I'll remove this once I can verify that everything is working well for projects and features
	private void manualProjectFeatureAssign(Tasks tasks) {
		TaskWriter writer = new TaskWriter(Main.osInterface);

		for (Task task : tasks.getAllTasks()) {
			List<TaskTimes> oldTimes = task.getStartStopTimes();
			List<TaskTimes> newTimes = new ArrayList<>();

			newTimes.add(task.getAllTimes().get(0));

			for (TaskTimes time : oldTimes) {
				newTimes.add(new TaskTimes(time.start, time.stop, tasks.getProjectForTask(task.id), tasks.getFeatureForTask(task.id)));
			}

			Task newTask = new Task(task.id, task.task, task.state, newTimes, task.isRecurring());

			writer.writeTask(newTask, "git-data/tasks/" + tasks.findListForTask(task.id).getFullPath() + "/" + task.id + ".txt");
		}
		System.exit(0);
	}

	private void generateTestData(Tasks tasks) {
		// create 100 groups with 10000 tasks spread randomly between them

		// just testing load times for now

		// next we'll randomly generate times for the 10000 tasks

		for (int i = 0; i < 100; i++) {
			tasks.createGroup("group-" + (i + 1));
			tasks.addList("group-" + (i + 1) + "/list-" + (i + 1), true);
		}

		Random random = new Random();

		for (int i = 0; i < 10000; i++) {
			int group = random.nextInt(100);

			tasks.addTask("Test " + (i + 1), "group-" + (group + 1) + "/list-" + (group + 1));
		}
	}

	// Export data with generic names, this will remove any possible proprietary data
	// Tasks will be exported as X - 'Task X'
	// Groups will be exported as 'group-x'
	// Lists will be exported as 'list-x'
	private void exportData(Tasks tasks) {
		exportGroup(tasks, tasks.getRootGroup(), "/", 1, 1, Main.osInterface);

		try (OutputStream outputStream = ((OSInterface) Main.osInterface).createOutputStream("git-data-export/next-id.txt")) {
			outputStream.write(String.valueOf(tasks.nextID()).getBytes());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exportGroup(Tasks tasks, TaskGroup group, String path, int groupNum, int listNum, OSInterface osInterface) {
		tasks.writeGroupInfoFile(group, "git-data-export");

		for (TaskContainer child : group.getChildren()) {
			if (child instanceof TaskGroup) {
				String name = "group-" + groupNum;
				groupNum++;
				exportGroup(tasks, (TaskGroup) child, path + name + "/", groupNum, listNum, osInterface);
			}
			else if (child instanceof TaskList) {
				String name = "list-" + listNum;
				listNum++;

				exportList(tasks, (TaskList) child, path + name, osInterface);
			}
		}
	}

	private void exportList(Tasks tasks, TaskList list, String path, OSInterface osInterface) {
		tasks.writeListInfoFile(list, "git-data-export");

		TaskWriter writer = new TaskWriter(osInterface);
		for (Task task : list.getTasks()) {
			Task strippedTask = new TaskBuilder(task).rename("Task " + task.id);

			writer.writeTask(strippedTask, "git-data-export/tasks" + path + "/" + task.id + ".txt");
		}
	}

	private boolean requiresTaskUpdate() {
		String currentVersion = "";

		try {
			currentVersion = ((OSInterface) Main.osInterface).getVersion();
		}
		catch (IOException ignored) {
		}

		try (InputStream inputStream = ((OSInterface) Main.osInterface).createInputStream("git-data/task-data-version.txt")) {
			Scanner scanner = new Scanner(inputStream);

			String dataVersion = scanner.nextLine();

			return !currentVersion.equals(dataVersion);
		}
		catch (Exception ignored) {
		}
		// if the file doesn't exist, then yes, we need to update
		return true;
	}

	/**
	 * Top-level command that just prints help.
	 */
	@Command(name = "",
			description = {
					"TODO Task tracking application. " +
							"Hit @|magenta <TAB>|@ to see available commands.",
					""},
			subcommands = {HelpCommand.class})
	public static final class CliCommands implements Runnable {
		PrintWriter out;

		public CliCommands() {
		}

		public void run() {
			out.println(new CommandLine(this).getUsageMessage());
		}
	}

	/**
	 * Provide command descriptions for JLine TailTipWidgets
	 * to be displayed in the status bar.
	 */
	private static final class DescriptionGenerator {
		final Builtins builtins;
		final PicocliCommands picocli;

		DescriptionGenerator(Builtins builtins, PicocliCommands picocli) {
			this.builtins = builtins;
			this.picocli = picocli;
		}

		Widgets.CmdDesc commandDescription(Widgets.CmdLine line) {
			Widgets.CmdDesc out = null;

			if (line.getDescriptionType() == Widgets.CmdLine.DescriptionType.COMMAND) {
				String cmd = Parser.getCommand(line.getArgs().get(0));

				if (builtins.hasCommand(cmd) || picocli.hasCommand(cmd)) {
					out = builtins.commandDescription(cmd);
				}
			}

			return out;
		}
	}
}
