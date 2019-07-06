// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.jline;

import com.andrewauclair.todo.Utils;
import org.jline.reader.Candidate;

// Helper class to make JLine 3 Candidates easier to test, they don't have an equals or a toString so they're useless in the logs
class TestCandidate {
	private final Candidate candidate;

	TestCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	@Override
	public String toString() {
		return candidate.value() + Utils.NL +
				candidate.displ() + Utils.NL +
				candidate.group() + Utils.NL +
				candidate.descr() + Utils.NL +
				candidate.suffix() + Utils.NL +
				candidate.key() + Utils.NL +
				candidate.complete();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Candidate) {
			return candidate.compareTo((Candidate) obj) == 0;
		}
		else if (obj instanceof TestCandidate) {
			int compare = candidate.compareTo(((TestCandidate) obj).candidate);
			return compare == 0;
		}
		return false;
	}
}
