// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

class TaskWriter {
	private final FileCreator fileCreator;
	
	TaskWriter(FileCreator fileCreator) {
		this.fileCreator = fileCreator;
	}
	
	boolean writeTask(Task task, String fileName) {
		try (OutputStream outputStream = fileCreator.createOutputStream(fileName)) {
			outputStream.write(task.task.getBytes());
			writeNL(outputStream);
			outputStream.write(task.state.toString().getBytes());
			
			List<TaskTimes.Times> times = task.times.asList();
			
			if (times.size() > 0) {
				writeNL(outputStream);
				
				for (TaskTimes.Times time : times) {
					writeNL(outputStream);
					outputStream.write("start ".getBytes());
					outputStream.write(String.valueOf(time.start).getBytes());
					
					if (time.stop != TaskTimes.Times.TIME_NOT_SET) {
						writeNL(outputStream);
						outputStream.write("stop ".getBytes());
						outputStream.write(String.valueOf(time.stop).getBytes());
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
	
	private void writeNL(OutputStream outputStream) throws IOException {
		outputStream.write(System.lineSeparator().getBytes());
	}
}
