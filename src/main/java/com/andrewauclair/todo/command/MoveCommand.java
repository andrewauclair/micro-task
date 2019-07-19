package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.jline.ListCompleter;
import org.jline.builtins.Completers;
import org.jline.reader.impl.completer.NullCompleter;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class MoveCommand extends Command {
	private final Tasks tasks;

	public MoveCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");

		Task task = tasks.moveTask(Long.parseLong(s[1]), s[2]);

		output.println("Moved task " + task.id + " to list '" + s[2] + "'");
		output.println();
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("move",
						node(new NullCompleter(),
								node(new ListCompleter(tasks, false)
								)
						)
				)
		);
	}
}
