// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class TaskWriter {
	private final OSInterface osInterface;

	public TaskWriter(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public boolean writeTask(Task task, String fileName) {
		try (DataOutputStream outputStream = osInterface.createOutputStream(fileName)) {
			outputStream.write(task.task.getBytes());
			writeNL(outputStream);
			outputStream.write(task.state.toString().getBytes());
			writeNL(outputStream);
			outputStream.write(String.valueOf(task.isRecurring()).getBytes());

			writeNL(outputStream);
			writeNL(outputStream);
			writeTime(outputStream, "add", task.getAddTime().start);

			List<TaskTimes> taskTimes = task.getStartStopTimes();

			for (TaskTimes time : taskTimes) {
				writeNL(outputStream);
				writeTime(outputStream, "start", time.start);

				if (!time.project.isEmpty() || !time.feature.isEmpty()) {
					writeNL(outputStream);
					outputStream.write(time.project.getBytes());
					writeNL(outputStream);
					outputStream.write(time.feature.getBytes());
				}

				if (time.stop != TaskTimes.TIME_NOT_SET) {
					writeNL(outputStream);
					writeTime(outputStream, "stop", time.stop);
				}
			}

			if (task.getFinishTime().isPresent()) {
				writeNL(outputStream);
				writeTime(outputStream, "finish", task.getFinishTime().get().start);
			}
			writeNL(outputStream);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeNL(OutputStream outputStream) throws IOException {
		outputStream.write(Utils.NL.getBytes());
	}

	private void writeTime(DataOutputStream outputStream, String name, long time) throws IOException {
		outputStream.write(name.getBytes());
		outputStream.write(" ".getBytes());
		outputStream.write(String.valueOf(time).getBytes());
	}
}
