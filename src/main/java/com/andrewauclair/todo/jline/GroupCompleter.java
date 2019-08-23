package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.task.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GroupCompleter implements Completer {
	private final Tasks tasks;
	private final boolean includeCurrentGroup;

	public GroupCompleter(Tasks tasks, boolean includeCurrentGroup) {
		this.tasks = tasks;
		this.includeCurrentGroup = includeCurrentGroup;
	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		Objects.requireNonNull(reader);
		Objects.requireNonNull(line);

		List<Candidate> names = tasks.getGroupNames().stream()
				.filter(list -> includeCurrentGroup || !list.equals(tasks.getActiveGroup().getFullPath()))
				.map(Candidate::new)
				.collect(Collectors.toList());

		candidates.addAll(names);
	}
}
