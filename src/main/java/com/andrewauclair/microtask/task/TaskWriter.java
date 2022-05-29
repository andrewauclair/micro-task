// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
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
		try (DataOutputStream stream = osInterface.createOutputStream(fileName)) {
			writeTask(task, stream);
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
		printStream.println(task.recurring);
		printStream.println("due " + task.dueTime);
		printStream.println();

		if (!task.tags.isEmpty()) {
			task.tags.forEach(tag -> printStream.println("tag " + tag));
			printStream.println();
		}

		writeTime(printStream, "add", task.addTime);

		List<TaskTimes> taskTimes = task.startStopTimes;

		for (TaskTimes time : taskTimes) {
			writeTime(printStream, "start", time.start);

			printStream.println(time.project);
			printStream.println(time.feature);

			if (time.stop != TaskTimes.TIME_NOT_SET) {
				writeTime(printStream, "stop", time.stop);
			}
		}

		if (task.finishTime != TaskTimes.TIME_NOT_SET) {
			writeTime(printStream, "finish", task.finishTime);
		}
		printStream.println("END");
	}

	private void writeTime(PrintStream outputStream, String name, long time) {
		outputStream.print(name);
		outputStream.print(" ");
		outputStream.println(time);
	}
}
