// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class LongCompleter implements Completer {
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		try {
			Long.parseLong(line.word());

			candidates.add(new Candidate(line.word()));
		}
		catch (NumberFormatException ignored) {
		}
	}
}
