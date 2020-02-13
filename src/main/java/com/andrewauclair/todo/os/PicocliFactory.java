package com.andrewauclair.todo.os;

import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

public class PicocliFactory implements CommandLine.IFactory {
	private final Commands commands;
	private final Tasks tasks;

	public PicocliFactory(Commands commands, Tasks tasks) {

		this.commands = commands;
		this.tasks = tasks;
	}

	@Override
	public <K> K create(Class<K> cls) throws Exception {
		if (cls == ListCompleter.class) {
			return (K) new ListCompleter(tasks, true);
		}
		else if (cls == GroupCompleter.class) {
			return (K) new GroupCompleter(tasks, true);
		}
		return CommandLine.defaultFactory().create(cls);
	}
}
