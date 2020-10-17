// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class TaskWriter {
	private final OSInterface osInterface;

	public TaskWriter(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public boolean writeTask(Task task, String fileName) {
		try {
			writeTask(task, osInterface.createOutputStream(fileName));
			return true;
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return false;
	}

	public void writeTask(Task task, OutputStream outputStream) {
		PrintStream printStream = new PrintStream(outputStream);

		printStream.println(task.task);
		printStream.println(task.state);
		printStream.println(task.isRecurring());
		printStream.println();

		writeTime(printStream, "add", task.getAddTime().start);

		List<TaskTimes> taskTimes = task.getStartStopTimes();

		for (TaskTimes time : taskTimes) {
			writeTime(printStream, "start", time.start);

			printStream.println(time.project);
			printStream.println(time.feature);

			if (time.stop != TaskTimes.TIME_NOT_SET) {
				writeTime(printStream, "stop", time.stop);
			}
		}

		if (task.getFinishTime().isPresent()) {
			writeTime(printStream, "finish", task.getFinishTime().get().start);
		}
		printStream.println("END");
	}

	private void writeTime(PrintStream outputStream, String name, long time) {
		outputStream.print(name);
		outputStream.print(" ");
		outputStream.println(time);
	}
}
