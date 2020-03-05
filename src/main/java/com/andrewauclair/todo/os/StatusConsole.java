// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.task.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Status;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

public class StatusConsole {
	private String currentGroup;
	private String currentList;
	private String currentCommand = "times --today";
	private final TaskLoader loader;
	private final Socket client;
	private final Commands commands;
	private final Terminal terminal;
	private final LineReader lineReader;
	private final Status status;

	public enum TransferType {
		Command,
		CurrentGroup,
		CurrentList,
		Focus,
		Exit
	}

	private final Tasks tasks;

	private final OSInterfaceImpl osInterface = new OSInterfaceImpl() {
		@Override
		public DataOutputStream createOutputStream(String fileName) {
			throw new RuntimeException("The status console can't create files. It is read only");
		}
	};

	public StatusConsole() throws Exception {
		client = new Socket("localhost", 5678);

		System.out.println("Connected");

		tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		commands = new Commands(tasks, new GitLabReleases(), osInterface);

		loader = new TaskLoader(tasks, new TaskReader(osInterface), osInterface);

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

		status = Status.getStatus(terminal);
		status.setBorder(true);

		updateStatus(status, terminal);

		Timer timer = new Timer();

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				updateStatus(status, terminal);
			}
		};

		timer.schedule(timerTask, 1000, 1000);

		currentGroup = tasks.getActiveGroup().getFullPath();
		currentList = tasks.getActiveList();

		final Kernel32 kernel32 = Kernel32.INSTANCE;

		kernel32.SetConsoleTitle("TODO App Status Console");
	}

	public void run() {
		int c;
		try (DataInputStream in = new DataInputStream(client.getInputStream())) {
			while ((c = in.read()) != -1) {
				synchronized (tasks) {
					tasks.load(loader, commands);

					lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();

					if (c == TransferType.Command.ordinal()) {
						currentCommand = in.readUTF();
					}
					else if (c == TransferType.CurrentGroup.ordinal()) {
						currentGroup = in.readUTF();
					}
					else if (c == TransferType.CurrentList.ordinal()) {
						currentList = in.readUTF();
					}
					else if (c == TransferType.Focus.ordinal()) {
						bringWindowToFront();
					}
					else if (c == TransferType.Exit.ordinal()) {
						client.close();
						break;
					}

					tasks.switchGroup(currentGroup);
					tasks.setActiveList(currentList);

					try {
						commands.execute(System.out, currentCommand);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				updateStatus(status, terminal);
			}
		}
		catch (IOException ignored) {
		}
	}

	private void updateStatus(Status status, Terminal terminal) {
		synchronized (tasks) {
			int width = terminal.getSize().getColumns();

			List<AttributedString> as = new ArrayList<>();

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

				line2 += String.join("", Collections.nCopies(width - line2.length() - timeToday.length(), " "));
				line3 += String.join("", Collections.nCopies(width - line3.length() - allTime.length(), " "));

				line2 += timeToday;
				line3 += allTime;

				as.add(new AttributedString(padString(terminal, description)));
				as.add(new AttributedString(padString(terminal, line2)));
				as.add(new AttributedString(padString(terminal, line3)));
			}
			else {
				as.add(new AttributedString(padString(terminal, "No active task")));
				as.add(new AttributedString(padString(terminal, "")));
				as.add(new AttributedString(padString(terminal, "Current Group: " + currentGroup + "  Current List: " + currentList)));
			}

			status.update(as);
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

		for (TaskTimes time : task.getStartStopTimes()) {
			if (time.start >= midnightStart && time.stop < midnightStop && time.start < midnightStop) {
				totalTime += time.getDuration(osInterface);
			}
		}

		return totalTime;
	}

	private long getTimeCurrent(Task task) {
		TaskTimes taskTimes = task.getStartStopTimes().get(task.getStartStopTimes().size() - 1);

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

	private String padString(Terminal terminal, String str) {
		int width = terminal.getSize().getColumns();
		return padString(str, width);
	}

	private String padString(String str, int width) {
		return String.join("", Collections.nCopies(width - str.length(), " "));
	}

	private void bringWindowToFront() {
		WinDef.HWND hWnd = User32.INSTANCE.FindWindow(null, "TODO App Status Console");
		if (hWnd == null) {
			return;
		}
		User32.INSTANCE.SetForegroundWindow(hWnd);
	}
}
