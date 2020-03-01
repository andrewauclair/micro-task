// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

import java.lang.reflect.Constructor;

public final class PicocliFactory implements CommandLine.IFactory {
	private final Commands commands;
	private final Tasks tasks;

	public PicocliFactory(Commands commands, Tasks tasks) {
		this.commands = commands;
		this.tasks = tasks;
	}

	@Override
	public <K> K create(Class<K> cls) throws Exception {
		if (cls == ListCompleter.class) {
			return cls.cast(new ListCompleter(tasks));
		}
		else if (cls == GroupCompleter.class) {
			return cls.cast(new GroupCompleter(tasks));
		}
//		else {
		Constructor<?>[] constructors = cls.getConstructors();
//		return cls.getConstructor(Tasks.class, Commands.class).newInstance(tasks, commands);
//		}
		return CommandLine.defaultFactory().create(cls);
	}
}
