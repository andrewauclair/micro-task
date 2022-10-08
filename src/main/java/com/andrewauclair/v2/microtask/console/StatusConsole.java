package com.andrewauclair.v2.microtask.console;

import com.andrewauclair.microtask.command.Commands;

import javax.swing.Timer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StatusConsole extends Console {
	private Commands commands;
	private String command;

	private final Timer timer;

	public StatusConsole(Commands commands, String name, String command, boolean updateOnInterval, int interval) {
		super(name, true);
		this.commands = commands;

		this.command = command;

		if (updateOnInterval) {
			timer = new Timer(interval * 1000, e -> {
				update();
			});
			timer.setRepeats(true);
			timer.start();
		}
		else {
			timer = null;
		}
	}

	public void update() {
		clear();

		executeCommand(command);
	}

	private void executeCommand(String command) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(stream);
		System.setOut(print);
		System.setErr(print);
		commands.execute(System.out, command);

		String str = stream.toString();

		appendOutput(str);
	}

	@Override
	protected void handleInput(String input) {
		// no-op, status consoles don't receive input
	}
}
