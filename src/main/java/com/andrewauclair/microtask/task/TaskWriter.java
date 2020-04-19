// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.Utils;
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
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream(fileName))) {
			outputStream.println(task.task);
			outputStream.println(task.state);
			outputStream.println(task.isRecurring());
			outputStream.println();

			writeTime(outputStream, "add", task.getAddTime().start);

			List<TaskTimes> taskTimes = task.getStartStopTimes();

			for (TaskTimes time : taskTimes) {
				writeTime(outputStream, "start", time.start);

				outputStream.println(time.project);
				outputStream.println(time.feature);

				if (time.stop != TaskTimes.TIME_NOT_SET) {
					writeTime(outputStream, "stop", time.stop);
				}
			}

			if (task.getFinishTime().isPresent()) {
				writeTime(outputStream, "finish", task.getFinishTime().get().start);
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	private void writeTime(PrintStream outputStream, String name, long time) throws IOException {
		outputStream.print(name);
		outputStream.print(" ");
		outputStream.println(time);
	}
}
