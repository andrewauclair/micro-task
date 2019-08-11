// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.task.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListCompleter implements Completer {
	private final Tasks tasks;
	private final boolean includeCurrentList;

	public ListCompleter(Tasks tasks, boolean includeCurrentList) {
		this.tasks = tasks;
		this.includeCurrentList = includeCurrentList;
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		Objects.requireNonNull(reader);
		Objects.requireNonNull(line);
		
		List<Candidate> names = tasks.getListNames().stream()
				.filter(list -> includeCurrentList || !list.equals(tasks.getCurrentList()))
				.map(Candidate::new)
				.collect(Collectors.toList());
		
		candidates.addAll(names);
	}
}
