// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.jline;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Objects;

public final class RenameCompleter implements Completer {
	private final Tasks tasks;

	public RenameCompleter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		Objects.requireNonNull(reader);
		Objects.requireNonNull(line);

		try {
			long taskID = Long.parseLong(line.word());

			Task task = tasks.getTask(taskID);

			candidates.add(new Candidate(taskID + " -n \"" + task.task + "\""));
		}
		catch (NumberFormatException ignored) {
		}
	}
}
