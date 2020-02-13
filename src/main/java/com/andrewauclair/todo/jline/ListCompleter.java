// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.task.Tasks;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListCompleter extends ArrayList<String> {
	public ListCompleter(Tasks tasks, boolean includeCurrentList) {
		List<String> names = tasks.getListNames().stream()
				.filter(list -> includeCurrentList || !list.equals(tasks.getActiveList()))
				.collect(Collectors.toList());

		addAll(names);
	}
}
