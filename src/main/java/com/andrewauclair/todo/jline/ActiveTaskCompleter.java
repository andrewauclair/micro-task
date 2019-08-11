// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.task.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Objects;

public class ActiveTaskCompleter implements Completer {
	private final Tasks tasks;

	public ActiveTaskCompleter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		Objects.requireNonNull(reader);
		Objects.requireNonNull(line);

		if (tasks.hasActiveTask()) {
			candidates.add(new Candidate(String.valueOf(tasks.getActiveTaskID())));
		}
	}
}
