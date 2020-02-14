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

public class GroupCompleter extends ArrayList<String> {
	public GroupCompleter(Tasks tasks) {
		super(tasks.getGroupNames());
	}
}
