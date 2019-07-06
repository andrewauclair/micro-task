// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RenameCompleter implements Completer {
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

			Optional<Task> task = tasks.getTask(taskID);

			task.ifPresent(value -> candidates.add(new Candidate(taskID + " \"" + value.task + "\"")));
		}
		catch (NumberFormatException ignored) {
		}
	}
}
