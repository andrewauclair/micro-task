// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.command.TimesCommand;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.os.OSInterfaceImpl;
import com.andrewauclair.todo.task.*;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.jline.builtins.Completers;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Status;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
	private static final char BACKSPACE_KEY = '\u0008';
	
	public static void main(String[] args) throws IOException {
		OSInterfaceImpl osInterface = new OSInterfaceImpl();
		Tasks tasks = new Tasks(getStartingID(osInterface), new TaskWriter(osInterface), System.out, osInterface);
		Commands commands = new Commands(tasks, new GitLabReleases(), osInterface);
		
		osInterface.setCommands(commands);
		
		File git_data = new File("git-data");
		
		boolean exists = git_data.exists();
		
		if (!exists) {
			boolean mkdir = git_data.mkdir();
			
			System.out.println(mkdir);
			
			osInterface.runGitCommand("git init", false);
			osInterface.runGitCommand("git config user.email \"git@todo.app\"", false);
			osInterface.runGitCommand("git config user.name \"TODO App\"", false);
		}
		
		boolean exception = false;
		
		try {
			TaskLoader loader = new TaskLoader(tasks, new TaskReader(osInterface), osInterface);
			loader.load();
		}
		catch (Exception e) {
			System.out.println(ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED + "Failed to read tasks." + ConsoleColors.ANSI_RESET);
			e.printStackTrace();
			
			System.out.println();
			System.out.println("Last file: " + osInterface.getLastInputFile());
			
			exception = true;
		}
		
		reloadAliases(osInterface, commands);
		
		Terminal terminal = TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.build();
		
		System.setIn(terminal.input());
		System.setOut(new PrintStream(terminal.output()));
		
		Completers.TreeCompleter treeCompleter = new Completers.TreeCompleter(commands.getAutoCompleteNodes());
		
		LineReader lineReader = LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(treeCompleter)
				.variable(LineReader.BELL_STYLE, "none")
				.build();
		
		Status status = Status.getStatus(terminal);
		status.setBorder(true);
		
		updateStatus(tasks, status, terminal, osInterface);
		
		bindCtrlBackspace(lineReader);
		
		if (!exception) {
			lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();
		}
		
		Size terminalSize = terminal.getSize();
		
		if (tasks.getActiveTaskID() != Tasks.NO_ACTIVE_TASK) {
			// set active list to the list of the active task
			tasks.setActiveList(tasks.getActiveTaskList());
			
			// set active group to the group of the active task
			tasks.switchGroup(tasks.getGroupForList(tasks.getActiveTaskList()).getFullPath());
		}
		
		Timer timer = new Timer();
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				updateStatus(tasks, status, terminal, osInterface);
			}
		};
		
		boolean hasActiveTask = tasks.hasActiveTask();
		
		if (hasActiveTask) {
			timer.schedule(timerTask, 0, 30000);
		}
		
		while (true) {
			try {
				String command = lineReader.readLine(commands.getPrompt());
				
				if (command.equals("proj-feat-assign")) {
					manualProjectFeatureAssign(tasks, osInterface);
				}
				
				if (command.equals("export")) {
					exportData(tasks, osInterface);
				}
				else if (command.equals("test-data")) {
					generateTestData(tasks);
				}
				else {
					commands.execute(System.out, command);
				}
				
				if (tasks.hasActiveTask()) {
					timerTask.cancel();
					timerTask = new TimerTask() {
						@Override
						public void run() {
							updateStatus(tasks, status, terminal, osInterface);
						}
					};
					timer.schedule(timerTask, 0, 30000);
				}
				else {
					timerTask.cancel();
				}
				hasActiveTask = tasks.hasActiveTask();
				
				updateStatus(tasks, status, terminal, osInterface);
			}
			catch (UserInterruptException ignored) {
			}
			catch (EndOfFileException e) {
				return;
			}
		}
	}
	
	private static void updateStatus(Tasks tasks, Status status, Terminal terminal, OSInterface osInterface) {
		synchronized (tasks) {
			List<AttributedString> statusLines = new ArrayList<>();
			
			int width = terminal.getSize().getColumns();
			
			if (tasks.hasActiveTask()) {
				String description = tasks.getActiveTask().description();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				TimesCommand.printTotalTime(new PrintStream(stream), tasks.getActiveTask().getElapsedTime(osInterface), false);
				String time = new String(stream.toByteArray(), StandardCharsets.UTF_8);
				
				if (width < description.length() + time.length()) {
					int length = width - time.length() - 3;
					
					description = description.substring(0, length - 3);
					description += "...'";
				}
				description += String.join("", Collections.nCopies(width - description.length() - time.length(), " "));
				description += time;
				
				statusLines.add(new AttributedString(description));
				statusLines.add(new AttributedString("Active Task Group: " + tasks.getGroupForList(tasks.getActiveTaskList()).getFullPath() + "		Active Task List: " + tasks.getActiveTaskList()));
				statusLines.add(new AttributedString("Current Group: " + tasks.getActiveGroup().getFullPath() + "  Current List: " + tasks.getActiveList()));
			}
			else {
				statusLines.add(new AttributedString("No active task"));
				statusLines.add(new AttributedString(""));
				statusLines.add(new AttributedString("Current Group: " + tasks.getActiveGroup().getFullPath() + "  Current List: " + tasks.getActiveList()));
			}
			
			List<AttributedString> finalLines = new ArrayList<>();
			
			// fill lines with white space to overwrite anything already in the terminal
			for (AttributedString statusLine : statusLines) {
				int length = statusLine.length();
				
				finalLines.add(new AttributedString(statusLine.toString() + String.join("", Collections.nCopies(width - length, " "))));
			}
			
			status.update(finalLines);
		}
	}
	
	private static void bindCtrlBackspace(LineReader lineReader) {
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
	
	private static long getStartingID(OSInterface osInterface) {
		try (InputStream inputStream = osInterface.createInputStream("git-data/next-id.txt")) {
			Scanner scanner = new Scanner(inputStream);
			return scanner.nextLong();
		}
		catch (IOException ignored) {
		}
		return 1;
	}
	
	// TODO Add this somewhere that's tested
	private static void reloadAliases(OSInterface osInterface, Commands commands) {
		if (!new File("git-data/aliases.txt").exists()) {
			return;
		}
		
		try {
			Scanner scanner = new Scanner(osInterface.createInputStream("git-data/aliases.txt"));
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				String[] split = line.split("=");
				
				String name = split[0];
				String command = split[1].substring(1, split[1].length() - 1);
				
				commands.addAlias(name, command);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// temporary function that we will use to assign a project and feature to all existing task times at work
	// I'll remove this once I can verify that everything is working well for projects and features
	private static void manualProjectFeatureAssign(Tasks tasks, OSInterface osInterface) {
		TaskWriter writer = new TaskWriter(osInterface);
		
		for (Task task : tasks.getAllTasks()) {
			List<TaskTimes> oldTimes = task.getStartStopTimes();
			List<TaskTimes> newTimes = new ArrayList<>();
			
			newTimes.add(task.getTimes().get(0));
			
			for (TaskTimes time : oldTimes) {
				newTimes.add(new TaskTimes(time.start, time.stop, tasks.getProjectForTask(task.id), tasks.getFeatureForTask(task.id)));
			}
			
			Task newTask = new Task(task.id, task.task, task.state, newTimes, task.isRecurring());
			
			writer.writeTask(newTask, "git-data/tasks/" + tasks.findListForTask(task.id).getFullPath() + "/" + task.id + ".txt");
		}
		System.exit(0);
	}
	
	private static void generateTestData(Tasks tasks) {
		// create 100 groups with 10000 tasks spread randomly between them
		
		// just testing load times for now
		
		// next we'll randomly generate times for the 10000 tasks
		
		for (int i = 0; i < 100; i++) {
			tasks.createGroup("group-" + (i + 1));
			tasks.addList("group-" + (i + 1) + "/list-" + (i + 1));
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
	private static void exportData(Tasks tasks, OSInterface osInterface) {
		exportGroup(tasks.getRootGroup(), "/", 1, 1, osInterface);
	}
	
	private static void exportGroup(TaskGroup group, String path, int groupNum, int listNum, OSInterface osInterface) {
		for (TaskContainer child : group.getChildren()) {
			if (child instanceof TaskGroup) {
				String name = "group-" + groupNum;
				groupNum++;
				exportGroup((TaskGroup) child, path + name + "/", groupNum, listNum, osInterface);
			}
			else if (child instanceof TaskList) {
				String name = "list-" + listNum;
				listNum++;
				
				exportList((TaskList) child, path + name, osInterface);
			}
		}
	}
	
	private static void exportList(TaskList list, String path, OSInterface osInterface) {
		TaskWriter writer = new TaskWriter(osInterface);
		for (Task task : list.getTasks()) {
			Task strippedTask = new TaskBuilder(task).rename("Task " + task.id);
			
			writer.writeTask(strippedTask, "git-data-export/tasks" + path + "/" + task.id + ".txt");
		}
	}
}
