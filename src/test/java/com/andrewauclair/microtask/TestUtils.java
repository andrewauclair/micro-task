// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.build.TaskBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {
	public static void assertOutput(OutputStream stream, String... lines) {
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < lines.length; i++) {
			output.append(lines[i]);
			if (i + 1 < lines.length) {
				output.append(Utils.NL);
			}
		}

		assertThat(stream.toString()).isEqualTo(output.toString());
	}

	public static InputStream createInputStream(String... lines) {
		String content = String.join(Utils.NL, lines);

		return new ByteArrayInputStream(content.getBytes());
	}

	public static TaskBuilder newTask(NewID id, IDValidator idValidator, String name, TaskState state, long addTime) {
		return new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);
	}

	public static TaskBuilder existingTask(ExistingID id, String name, TaskState state, long addTime) {
		return new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);
	}

	public static TaskBuilder newTaskBuilder(NewID id, IDValidator idValidator, String name, TaskState state, long addTime) {
		return new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);
	}

	public static TaskBuilder existingTaskBuilder(ExistingID id, String name, TaskState state, long addTime) {
		return new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);
	}

	public static TaskBuilder newTask(NewID id, IDValidator idValidator, String name, TaskState state, long addTime, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder;
	}

	public static Task existingTask(ExistingID id, String name, TaskState state, long addTime, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder.build();
	}

	public static TaskBuilder existingTaskBuilder(ExistingID id, String name, TaskState state, long addTime, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder;
	}

	public static TaskBuilder newTask(NewID id, IDValidator idValidator, String name, TaskState state, long addTime, long finishTime, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withFinishTime(finishTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder;
	}

	public static TaskBuilder existingTask(ExistingID id, String name, TaskState state, long addTime, long finishTime, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withFinishTime(finishTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder;
	}

	public static TaskBuilder existingTaskBuilder(ExistingID id, String name, TaskState state, long addTime, long finishTime, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withFinishTime(finishTime)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder;
	}

	public static Task newTask(NewID id, IDValidator idValidator, String name, TaskState state, long addTime, boolean recurring, List<TaskTimes> startStopTimes) {
		TaskBuilder builder = new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withRecurring(recurring)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);

		startStopTimes.forEach(builder::withStartStopTime);

		return builder.build();
	}

	public static TaskBuilder newTask(NewID id, IDValidator idValidator, String name, TaskState state, long addTime, boolean recurring) {
		return new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withRecurring(recurring)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);
	}

	public static TaskBuilder existingTask(ExistingID id, String name, TaskState state, long addTime, boolean recurring) {
		return new TaskBuilder(id)
				.withTask(name)
				.withState(state)
				.withAddTime(addTime)
				.withRecurring(recurring)
				.withDueTime(addTime + Tasks.DEFAULT_DUE_TIME);
	}
}
