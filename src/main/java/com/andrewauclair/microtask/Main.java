// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.VersionCommand;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.OSInterfaceImpl;
import com.andrewauclair.microtask.os.StatusConsole;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.group.TaskGroupFileWriter;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.TaskListFileWriter;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.jline.builtins.Builtins;
import org.jline.builtins.Completers;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Comparator.comparingLong;
import static picocli.CommandLine.Command;


public final class Main {
	private static final String CONSOLE_TITLE = "micro task main";

	private static Commands commands;
	private LineReader lineReader;

	public void newTerminal(Terminal terminal) {
		Builtins builtins = new Builtins(Paths.get(""), null, null);
		builtins.rename(org.jline.builtins.Builtins.Command.TTOP, "top");
		builtins.alias("zle", "widget");
		builtins.alias("bindkey", "keymap");
		Completers.SystemCompleter systemCompleter = builtins.compileCompleters();

		CommandLine cmd = commands.buildCommandLineWithAllCommands();
		PicocliCommands picocliCommands = new PicocliCommands(Paths.get(""), cmd);
		systemCompleter.add(picocliCommands.compileCompleters());
		systemCompleter.compile();

		boolean loadHistory = false;

		if (lineReader != null) {
			try {
				lineReader.getHistory().save();
				loadHistory = true;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		lineReader = buildLineReader(systemCompleter, terminal);

		if (loadHistory) {
			try {
				lineReader.getHistory().load();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		builtins.setLineReader(lineReader);
		bindCtrlBackspace(lineReader);
		bindCtrlV(lineReader);
	}

	private static final char BACKSPACE_KEY = '\u0008';
	private static final AtomicBoolean runningCommand = new AtomicBoolean(false);

	private static final OSInterfaceImpl osInterface = new OSInterfaceImpl();

	private final Tasks tasks;

	private Main() throws Exception {
		final Kernel32 kernel32 = Kernel32.INSTANCE;

		kernel32.SetConsoleTitle(CONSOLE_TITLE);

		osInterface.createTerminal();

		LocalSettings localSettings = new LocalSettings(osInterface);
		osInterface.setLocalSettings(localSettings);

		tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		commands = new Commands(tasks, new GitLabReleases(), localSettings, osInterface);

		osInterface.openStatusLink();

		boolean loadSuccessful = tasks.load(new TaskLoader(tasks, new TaskReader(osInterface), localSettings, osInterface), commands);

		if (requiresTaskUpdate()) {
			commands.execute(System.out, "update --tasks");
		}

		osInterface.setMain(this);
		osInterface.createTerminal();

		if (loadSuccessful) {
			lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();
		}

		if (tasks.getActiveTaskID() != Tasks.NO_ACTIVE_TASK) {
			// set active list to the list of the active task
			tasks.setActiveList(tasks.getActiveTaskList());

			// set active group to the group of the active task
			tasks.setActiveGroup(new ExistingTaskGroupName(tasks, tasks.getGroupForList(tasks.getActiveTaskList()).getFullPath()));
		}

		sendCurrentStatus();

		VersionCommand.printLogo(osInterface);

		while (true) {
			try {
				String command = lineReader.readLine(commands.getPrompt());

				if (command.equals("proj-feat-assign")) {
					manualProjectFeatureAssign(tasks);
				}

				if (command.equals("export")) {
					exportData(tasks);
				}
				else {
					runningCommand.set(true);
					commands.execute(System.out, command);
					runningCommand.set(false);
				}

				sendCurrentStatus();
			}
			catch (UserInterruptException ignored) {
			}
			catch (EndOfFileException e) {
				return;
			}
		}
	}

	private void sendCurrentStatus() {
		osInterface.sendStatusMessage(StatusConsole.TransferType.CurrentGroup, tasks.getActiveGroup().getFullPath());
		osInterface.sendStatusMessage(StatusConsole.TransferType.CurrentList, tasks.getActiveList().absoluteName());
	}

	public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("status")) {
			StatusConsole statusConsole = new StatusConsole();

			statusConsole.run();

			System.exit(0);
		}
		else {
			new Main();
		}
	}

	private static LineReader buildLineReader(Completers.SystemCompleter systemCompleter, Terminal terminal) {
		return LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(systemCompleter)
				.parser(new DefaultParser())
				.variable(LineReader.LIST_MAX, 50)
				.variable(LineReader.BELL_STYLE, "none")
				.variable(LineReader.HISTORY_FILE, "history.txt")
				.build();
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

	private void bindCtrlV(LineReader lineReader) {
		KeyMap<Binding> main = lineReader.getKeyMaps().get(LineReader.MAIN);

		main.bind((Widget) () -> {
			String clipboardContents = getClipboardContents();

			if (!clipboardContents.isEmpty()) {
				lineReader.getBuffer().write(clipboardContents);
			}

			return true;
		}, KeyMap.ctrl('v'));
	}

	/**
	 * Get the String residing on the clipboard.
	 *
	 * @return any text found on the Clipboard; if none found, return an
	 * empty String.
	 */
	public String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText =
				(contents != null) &&
						contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException | IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;
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
				newTimes.add(new TaskTimes(time.start, time.stop, new TaskFinder(tasks).getProjectForTask(new ExistingID(tasks, task.id)), new TaskFinder(tasks).getFeatureForTask(new ExistingID(tasks, task.id))));
			}

			Task newTask = new Task(task.id, task.task, task.state, newTimes, task.isRecurring());

			writer.writeTask(newTask, "git-data/tasks/" + tasks.findListForTask(new ExistingID(tasks, task.id)).getFullPath() + "/" + task.id + ".txt");
		}
		System.exit(0);
	}

	// Export data with generic names, this will remove any possible proprietary data
	// Tasks will be exported as X - 'Task X'
	// Groups will be exported as 'group-x'
	// Lists will be exported as 'list-x'
	private void exportData(Tasks tasks) {
		exportGroup(tasks, tasks.getRootGroup(), "/", 1, 1, Main.osInterface);

		try (PrintStream outputStream = new PrintStream(Main.osInterface.createOutputStream("git-data-export/next-id.txt"))) {
			outputStream.print(tasks.nextID());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exportGroup(Tasks tasks, TaskGroup group, String path, int groupNum, int listNum, OSInterface osInterface) {
		new TaskGroupFileWriter(group, osInterface)
				.inFolder("git-data-export")
				.write();

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
//		tasks.writeListInfoFile(list, "git-data-export");

		new TaskListFileWriter(list, osInterface)
				.inFolder("git-data-export")
				.write();

		TaskWriter writer = new TaskWriter(osInterface);

		for (Task task : list.getTasks()) {
			Task strippedTask = new TaskBuilder(task)
					.withName("Task " + task.id)
					.build();

			writer.writeTask(strippedTask, "git-data-export/tasks" + path + "/" + task.id + ".txt");
		}
	}

	// TODO Find a way to test this, build it into the task loader as the last step
	private boolean requiresTaskUpdate() {
		String currentVersion = "";

		try {
			currentVersion = Main.osInterface.getVersion();
		}
		catch (IOException ignored) {
		}

		try (InputStream inputStream = Main.osInterface.createInputStream("git-data/task-data-version.txt")) {
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
					"",
					"micro task, a CLI based task tracking application."},
			synopsisSubcommandLabel = "COMMAND",
			subcommands = {MicroTaskHelpCommand.class}
	)
	public static final class CliCommands implements Runnable {//}, CommandLine.IHelpCommandInitializable2 {
//		PrintWriter out;

		public CliCommands() {
		}

		public void run() {
			System.out.println("Help output!");
			System.out.println(new CommandLine(this).getUsageMessage());
		}

//		@Override
//		public void init(CommandLine helpCommandLine, CommandLine.Help.ColorScheme colorScheme, PrintWriter outWriter, PrintWriter errWriter) {
//			outWriter.write("Test output");
//		}
	}

	@Command(name = "help", helpCommand = true)
	public static final class MicroTaskHelpCommand implements Runnable {//}, CommandLine.IHelpCommandInitializable2 {

//		@Override
//		public void init(CommandLine helpCommandLine, CommandLine.Help.ColorScheme colorScheme, PrintWriter outWriter, PrintWriter errWriter) {
//			helpCommandLine.usage(outWriter, colorScheme);
//		}

		@Override
		public void run() {
			VersionCommand.printLogo(osInterface);
			System.out.println();

			commands.buildCommandLineWithAllCommands().usage(System.out);

			System.out.println();
			System.out.println("Aliases:");
			System.out.println();

			Map<String, String> aliases = commands.getAliases();

			Optional<String> max = aliases.keySet().stream()
					.max(comparingLong(String::length));

			for (final String alias : aliases.keySet()) {
				System.out.print("  ");
				System.out.println(String.format("%-" + max.get().length() + "s  %s", alias, aliases.get(alias)));
			}
			System.out.println();
		}
	}
}
