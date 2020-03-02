// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.task.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Status;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StatusConsole {

	private final Tasks tasks;

	private final OSInterface osInterface = new OSInterfaceImpl();

	public StatusConsole() throws Exception {
		Socket client = new Socket("localhost", 5678);

		System.out.println("Connected");

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));



		tasks = new Tasks(new TaskWriter(osInterface), System.out, osInterface);
		Commands commands = new Commands(tasks, new GitLabReleases(), osInterface);

		TaskLoader loader = new TaskLoader(tasks, new TaskReader(osInterface), osInterface);



		Terminal terminal = TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.streams(System.in, System.out)
				.build();

		System.setIn(terminal.input());
		System.setOut(new PrintStream(terminal.output()));

		Status status = Status.getStatus(terminal);
		status.setBorder(true);


		updateStatus(status, terminal);

		Timer timer = new Timer();

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
//				if (!runningCommand.get()) {
								updateStatus(status, terminal);
//				}
			}
		};

//		boolean hasActiveTask = tasks.hasActiveTask();

//		if (hasActiveTask) {
			timer.schedule(timerTask, 1000, 1000);
//		}


		int c = 0;
		while ((c = inFromServer.read()) != -1) {
			synchronized (tasks) {
				tasks.load(loader, commands);

				osInterface.clearScreen();

//				if (tasks.hasActiveTask()) {
//					System.out.println("Active task: " + tasks.getActiveTask().description());
//					System.out.println("Active List: " + tasks.getActiveList());
//					System.out.println("Active Group: " + tasks.getActiveGroup().getFullPath());
//				}
//				else {
//					System.out.println("No active task.");
//				}

				commands.execute(System.out, "list");
			}
		}
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

	private String padString(Terminal terminal, String str) {
		int width = terminal.getSize().getColumns();
		return str + String.join("", Collections.nCopies(width - str.length(), " "));
	}
}
