// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.task.*;
import com.sun.jna.platform.win32.*;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Status;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
		CurrentList
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
//				.completer(systemCompleter)
//				.parser(new DefaultParser())
//				.variable(LineReader.LIST_MAX, 50)
				.variable(LineReader.BELL_STYLE, "none")
//				.variable(LineReader.HISTORY_FILE, "history.txt")
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

	public void run() throws IOException {
		DataInputStream in = new DataInputStream(client.getInputStream());

		int c;
		try {
			while ((c = in.read()) != -1) {
				synchronized (tasks) {
					tasks.load(loader, commands);

//					osInterface.clearScreen();
					lineReader.getBuiltinWidgets().get(LineReader.CLEAR_SCREEN).apply();
//					clearScreen();

					if (c == TransferType.Command.ordinal()) {
						currentCommand = in.readUTF();
					}
					else if (c == TransferType.CurrentGroup.ordinal()) {
						currentGroup = in.readUTF();
					}
					else if (c == TransferType.CurrentList.ordinal()) {
						currentList = in.readUTF();
					}

					tasks.switchGroup(currentGroup);
					tasks.setActiveList(currentList);

					try {
						commands.execute(System.out, currentCommand);
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					bringWindowToFront();
				}
				updateStatus(status, terminal);
			}
		}
		catch (IOException ignored) {
		}

		long state = getElapsedTime(new Task(1, "State", TaskState.Inactive, Collections.emptyList()));
	}

	private void updateStatus(Status status, Terminal terminal) {
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
				as.add(new AttributedString(padString(terminal, "Active Task List: " + tasks.getActiveTaskList())));
				as.add(new AttributedString(padString(terminal, "Current Group: " + currentGroup + "  Current List: " + currentList)));
			}
			else {
				as.add(new AttributedString(padString(terminal, "No active task")));
				as.add(new AttributedString(padString(terminal, "")));
				as.add(new AttributedString(padString(terminal, "Current Group: " + currentGroup + "  Current List: " + currentList)));
			}

			status.update(as);
		}
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
		return str + String.join("", Collections.nCopies(width - str.length(), " "));
	}

	private void bringWindowToFront() {
		WinDef.HWND hWnd = User32.INSTANCE.FindWindow(null, "TODO App Status Console");
		if (hWnd == null) {
			return;
		}
		User32.INSTANCE.SetForegroundWindow(hWnd);
	}
}
