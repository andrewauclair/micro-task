// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.*;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.fusesource.hawtjni.runtime.ArgFlag;
import org.fusesource.hawtjni.runtime.JniArg;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static com.andrewauclair.microtask.ConsoleTable.Alignment.LEFT;
import static com.andrewauclair.microtask.ConsoleTable.Alignment.RIGHT;
import static org.fusesource.jansi.internal.Kernel32.GetStdHandle;
import static org.fusesource.jansi.internal.Kernel32.STD_OUTPUT_HANDLE;

public class StatusConsole {
	private final String CONSOLE_TITLE = "micro task status";
	private final boolean directoryDisplay;

	private String currentGroup;
	private String currentList;
	private String currentCommand = "times --today";
	private final DataLoader loader;
	private final Socket client;
	private final Commands commands;
	private final Terminal terminal;
	private final LineReader lineReader;
//	private final Status status;

	public enum DisplayType {
		ACTIVE_TASK("active-task", 0),
		ACTIVE_LIST("active-list", 1),
		ACTIVE_GROUP("active-group", 2),
		ACTIVE_PROJECT("active-project", 3),
		ACTIVE_FEATURE("active-feature", 4),
		ACTIVE_MILESTONE("active-milestone", 5),
		ACTIVE_TAGS("active-tags", 6),
		ACTIVE_CONTEXT("active-context", 7), // special value that displays the filed value, if any, options: list, group, project, feature, milestone
		CURRENT_LIST("current-list", 8);
//		CURRENT_GROUP(),
//		ACTIVE_TASK_TIME_NOW(),
//		ACTIVE_TASK_TIME_TODAY(),
//		ACTIVE_TASK_TIME_WEEK(),
//		ACTIVE_TASK_TIME_ALL(),
//		CURRENT_LIST_TIME_WEEK(),
//		CURRENT_LIST_TIME_ALL(),

		private final String name;
		private final int value;

		DisplayType(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public static DisplayType valueof(String name) {
			for (final DisplayType value : values()) {
				if (value.name.equals(name)) {
					return value;
				}
			}
			return null;
		}

		public static DisplayType valueOf(int value) {
			for (final DisplayType displayType : values()) {
				if (displayType.value == value) {
					return displayType;
				}
			}
			return null;
		}
	}

	public enum TransferType {
		COMMAND(0),
		CURRENT_GROUP(1),
		CURRENT_LIST(2),
		FOCUS(3),
		EXIT(4),
		END_TRANSFER(5);

		private final int value;

		TransferType(int value) {

			this.value = value;
		}

		public static TransferType valueOf(int value) {
			for (final TransferType transferType : values()) {
				if (transferType.value == value) {
					return transferType;
				}
			}
			return null;
		}
	}

	private final Tasks tasks;
	private final Projects projects;

	private final OSInterfaceImpl osInterface = new OSInterfaceImpl() {
		@Override
		public DataOutputStream createOutputStream(String fileName) {
			throw new RuntimeException("The status console can't create files. It is read only");
		}

		@Override
		public boolean canCreateFiles() {
			return false;
		}

		@Override
		public void gitCommit(String message) {
		}
	};

	public StatusConsole(boolean directoryDisplay) throws Exception {
		this.directoryDisplay = directoryDisplay;
		client = new Socket("localhost", 5678);

		hidecursor();

		System.out.println("Connected");

		LocalSettings localSettings = new LocalSettings(osInterface);

		tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		projects = new Projects(tasks, osInterface);
		tasks.setProjects(projects);

		commands = new Commands(tasks, projects, new GitLabReleases(), localSettings, osInterface);

		loader = new DataLoader(tasks, new TaskReader(osInterface), localSettings, projects, osInterface);

		terminal = TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.streams(System.in, System.out)
				.build();

		lineReader = LineReaderBuilder.builder()
				.terminal(terminal)
				.variable(LineReader.BELL_STYLE, "none")
				.build();

		osInterface.setTerminal(terminal);

		System.setIn(terminal.input());
		System.setOut(new PrintStream(terminal.output()));

		if (!directoryDisplay) {
			updateStatus(terminal);

			Timer timer = new Timer();

			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					updateStatus(terminal);
				}
			};

			timer.schedule(timerTask, 1000, 1000);
		}

		currentGroup = tasks.getCurrentGroup().getFullPath();
		currentList = tasks.getCurrentList().absoluteName();

		final Kernel32 kernel32 = Kernel32.INSTANCE;

		kernel32.SetConsoleTitle(CONSOLE_TITLE);

		run();
	}

	public void run() {
		int c;
		try (DataInputStream in = new DataInputStream(client.getInputStream())) {
			while ((c = in.read()) != -1) {
				synchronized (tasks) {
					tasks.load(loader, commands);
					commands.loadAliases();
					lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();

					TransferType transferType = TransferType.valueOf(c);

					while (transferType != TransferType.END_TRANSFER) {
						switch (Objects.requireNonNull(transferType)) {
							case COMMAND -> currentCommand = in.readUTF();
							case CURRENT_GROUP -> currentGroup = in.readUTF();
							case CURRENT_LIST -> currentList = in.readUTF();
							case FOCUS -> bringWindowToFront();
							case EXIT -> {
								client.close();
								return;
							}
						}
						transferType = TransferType.valueOf(in.read());
					}

					tasks.setCurrentGroup(new ExistingGroupName(tasks, currentGroup));
					tasks.setCurrentList(new ExistingListName(tasks, currentList));
				}
				if (directoryDisplay) {
					displayDirectory();
				}
				else {
					updateStatus(terminal);
				}
			}
		}
		catch (IOException ignored) {
		}
	}

	private void displayDirectory() {
		int height = osInterface.getTerminalHeight();
		int lines = 3;

		System.out.println("Current group: " + tasks.getCurrentGroup().getFullPath());
		System.out.println();

		ConsoleTable table = new ConsoleTable(osInterface);
		table.setHeaders("Total", "Finished", "Active", "Recurring", "List / Group");
		table.setColumnAlignment(RIGHT, RIGHT, RIGHT, RIGHT, LEFT);

		for (final TaskContainer child : tasks.getCurrentGroup().getChildren()) {
			if (child.getState() == TaskContainerState.Finished) {
				continue;
			}

			List<Task> tasks = child.getTasks();

			int total = tasks.size();
			long finished = tasks.stream()
					.filter(task -> task.state == TaskState.Finished)
					.count();
			long active = total - finished;
			long recurring = tasks.stream()
					.filter(task -> task.recurring)
					.count();

			String name = child instanceof TaskGroup ? child.getName() + "/" : child.getName();
			table.addRow(String.valueOf(total), String.valueOf(finished), String.valueOf(active), String.valueOf(recurring), name);

			lines++;
		}

		table.print();

		for (int i = 0; i < height - lines - 1; i++) {
			System.out.println();
		}
	}

	private void updateStatus(Terminal terminal) {
		synchronized (tasks) {
			int height = osInterface.getTerminalHeight();
			int lines = 3;

			terminal.puts(InfoCmp.Capability.cursor_address, 0, 0);

			int width = osInterface.getTerminalWidth();

			List<String> as = new ArrayList<>();

			if (tasks.hasActiveTask()) {
				String description = tasks.getActiveTask().description();

				String currentTime = getTimeString(getTimeCurrent(tasks.getActiveTask()));
				String timeToday = getTimeString(getTimeToday(tasks.getActiveTask()));
				String allTime = getTimeString(getElapsedTime(tasks.getActiveTask()));

				if (width < description.length() + currentTime.length()) {
					int length = width - currentTime.length() - 3;

					description = description.substring(0, length - 3);
					description += "...'";
				}
				description += String.join("", Collections.nCopies(width - description.length() - currentTime.length(), " "));
				description += currentTime;

				String line2 = "Active Task List: " + tasks.getActiveTaskList();
				String line3 = "Current Group: " + currentGroup + "  Current List: " + currentList;

				if (width - line2.length() - timeToday.length() < 0) {
					line2 = "";
				}

				if (width - line2.length() - timeToday.length() < 0) {
					line2 += timeToday;
				}
				else {
					line2 += String.join("", Collections.nCopies(width - line2.length() - timeToday.length(), " "));
				}

				if (width - line3.length() - allTime.length() < 0) {
					line3 = "Current Group: " + currentGroup;
				}
				if (width - line3.length() - allTime.length() < 0) {
					line3 += allTime.length();
				}
				else {
					line3 += String.join("", Collections.nCopies(width - line3.length() - allTime.length(), " "));
				}

				line2 += timeToday;
				line3 += allTime;

				as.add(padString(terminal, description));
				as.add(padString(terminal, line2));
				as.add(padString(terminal, line3));
			}
			else {
				as.add(padString(terminal, "No active task"));
				as.add(padString(terminal, ""));
				as.add(padString(terminal, "Current Group: " + currentGroup + "  Current List: " + currentList));
			}

			for (final String a : as) {
				System.out.println(a);
			}

			for (int i = 0; i < height - lines - 1; i++) {
				System.out.println();
			}
		}
	}

	private String getTimeString(long time) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		new PrintStream(stream).print(Utils.formatTime(time, Utils.HighestTime.None));
		return new String(stream.toByteArray(), StandardCharsets.UTF_8);
	}

	private long applyDateRange(Task task, ZoneId zoneId, LocalDateTime midnight, LocalDateTime nextMidnight) {
		long midnightStart = midnight.atZone(zoneId).toEpochSecond();
		long midnightStop = nextMidnight.atZone(zoneId).toEpochSecond();

		long totalTime = 0;

		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		for (TaskTimes time : task.startStopTimes) {
			if (time.start >= midnightStart && time.stop < midnightStop && time.start < midnightStop) {
				totalTime += time.getDuration(osInterface);
			}
		}

		return totalTime;
	}

	private long getTimeCurrent(Task task) {
		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		TaskTimes taskTimes = task.startStopTimes.get(task.startStopTimes.size() - 1);

		return osInterface.currentSeconds() - taskTimes.start;
	}

	private long getTimeToday(Task task) {
		Instant instant = Instant.ofEpochSecond(osInterface.currentSeconds());

		LocalDate day = LocalDate.ofInstant(instant, osInterface.getZoneId());

		LocalDate of = LocalDate.of(day.getYear(), day.getMonth().getValue(), day.getDayOfMonth());

		ZoneId zoneId = osInterface.getZoneId();
		instant = of.atStartOfDay(zoneId).toInstant();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);
		LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
		LocalDateTime nextMidnight = midnight.plusDays(1);

		return applyDateRange(task, zoneId, midnight, nextMidnight);
	}

	private long getElapsedTime(Task task) {
		long total = 0;
		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		for (TaskTimes time : task.startStopTimes) {
			if (time.stop != TaskTimes.TIME_NOT_SET) {
				total += time.stop - time.start;
			}
			else {
				total += osInterface.currentSeconds() - time.start;
			}
		}
		return total;
	}

	private String padString(Terminal terminal, String str) {
		int width = terminal.getSize().getColumns();

		if (width == 0) {
			width = 80;
		}
		return padString(str, width);
	}

	private String padString(String str, int width) {
		if (width - str.length() < 0) {
			return str;
		}
		return str + String.join("", Collections.nCopies(width - str.length(), " "));
	}

	private void bringWindowToFront() {
		WinDef.HWND hWnd = User32.INSTANCE.FindWindow(null, CONSOLE_TITLE);
		if (hWnd == null) {
			return;
		}
		User32.INSTANCE.SetForegroundWindow(hWnd);
	}

	// typedef struct _CONSOLE_CURSOR_INFO {
	//   DWORD dwSize;
	//   BOOL  bVisible;
	// } CONSOLE_CURSOR_INFO, *PCONSOLE_CURSOR_INFO;
	public static class CONSOLE_CURSOR_INFO extends Structure {
		public int dwSize;
		public boolean bVisible;

		public static class ByReference extends CONSOLE_CURSOR_INFO implements
		                                                                                                     Structure.ByReference {
		}

		private static String[] fieldOrder = { "dwSize", "bVisible" };

		@Override
		protected java.util.List<String> getFieldOrder() {
			return java.util.Arrays.asList(fieldOrder);
		}
	}

	public interface Kernel32 extends StdCallLibrary, WinNT, Wincon {

		/** The instance. */
		Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

		boolean SetConsoleCursorInfo(@JniArg(cast = "HANDLE",flags = {ArgFlag.POINTER_ARG}) long handle, CONSOLE_CURSOR_INFO.ByReference info);
	}

	void hidecursor()
	{
		final StatusConsole.Kernel32 kernel32 = StatusConsole.Kernel32.INSTANCE;

		long handle = GetStdHandle(STD_OUTPUT_HANDLE);
		CONSOLE_CURSOR_INFO.ByReference info = new CONSOLE_CURSOR_INFO.ByReference();
		info.dwSize = 100;
		info.bVisible = false;
		boolean set = kernel32.SetConsoleCursorInfo(handle, info);

		System.out.println("Hide cursor: " + set);
	}
}
