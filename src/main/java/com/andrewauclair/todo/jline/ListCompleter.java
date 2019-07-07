// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListCompleter implements Completer {
	private final Tasks tasks;
	
	public ListCompleter(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		Objects.requireNonNull(reader);
		Objects.requireNonNull(line);
		
		List<Candidate> names = tasks.getListNames().stream()
				.map(Candidate::new)
				.collect(Collectors.toList());
		
		candidates.addAll(names);
	}
}
