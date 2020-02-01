// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

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

			List<TaskTimes> times = task.getTimes();

			if (times.size() > 0) {
				writeNL(outputStream);

				writeNL(outputStream);
				writeTime(outputStream, "add", times.get(0).start);
				
				if (times.size() > 1) {
					List<TaskTimes> taskTimes = times.subList(1, times.size());
					Iterator<TaskTimes> iterator = taskTimes.iterator();
					
					while (iterator.hasNext()) {
						TaskTimes time = iterator.next();
						
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
						
						if (!iterator.hasNext() && task.state == TaskState.Finished) {
							writeNL(outputStream);
							writeTime(outputStream, "finish", time.stop);
						}
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void writeTime(DataOutputStream outputStream, String name, long time) throws IOException {
		outputStream.write(name.getBytes());
		outputStream.write(" ".getBytes());
		outputStream.write(String.valueOf(time).getBytes());
	}
	
	private void writeNL(OutputStream outputStream) throws IOException {
		outputStream.write(Utils.NL.getBytes());
	}
}
