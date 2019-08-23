// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

public class TaskWriter {
	private final PrintStream output;
	private final OSInterface osInterface;

	public TaskWriter(PrintStream output, OSInterface osInterface) {
		this.output = output;
		this.osInterface = osInterface;
	}

	public boolean writeTask(Task task, String fileName) {
		try (DataOutputStream outputStream = osInterface.createOutputStream(fileName)) {
			outputStream.write(task.task.getBytes());
			writeNL(outputStream);
			outputStream.write(task.state.toString().getBytes());
			writeNL(outputStream);
			outputStream.write(String.valueOf(task.getIssue()).getBytes());
			writeNL(outputStream);
			outputStream.write(task.getProject().getBytes());
			
			List<TaskTimes> times = task.getTimes();

			if (times.size() > 0) {
				writeNL(outputStream);

				writeNL(outputStream);
				outputStream.write("add ".getBytes());
				outputStream.write(String.valueOf(times.get(0).start).getBytes());

				if (times.size() > 1) {
					for (TaskTimes time : times.subList(1, times.size())) {
						writeNL(outputStream);
						outputStream.write("start ".getBytes());
						outputStream.write(String.valueOf(time.start).getBytes());

						if (time.stop != TaskTimes.TIME_NOT_SET) {
							writeNL(outputStream);
							outputStream.write("stop ".getBytes());
							outputStream.write(String.valueOf(time.stop).getBytes());
						}
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace(output);
			return false;
		}
		return true;
	}

	private void writeNL(OutputStream outputStream) throws IOException {
		outputStream.write(Utils.NL.getBytes());
	}
}
