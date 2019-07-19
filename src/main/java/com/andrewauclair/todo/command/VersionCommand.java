package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class VersionCommand extends Command {
	private final Tasks tasks;

	public VersionCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		try {
			output.println(tasks.osInterface.getVersion());
		}
		catch (IOException e) {
			output.println("Unknown");
		}
		output.println();
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("version"));
	}
}
