// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.v2.microtask.console;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.OSInterfaceImpl;
import com.andrewauclair.v2.MainFrame;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MainConsole extends Console {
	private final MainFrame mainFrame;
	private final Commands commands;
	private OSInterfaceImpl osInterface;

	public MainConsole(MainFrame mainFrame, Commands commands, OSInterfaceImpl osInterface) {
		super("main", false);
		this.mainFrame = mainFrame;

		this.commands = commands;
		this.osInterface = osInterface;
	}

	public void executeCommand(String command) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(stream);
		System.setOut(print);
		System.setErr(print);
		commands.execute(System.out, command);

		String str = stream.toString();

		appendOutput(str);

		mainFrame.executedCommand();
	}

	@Override
	protected void handleInput(String input) {
		executeCommand(input);
	}

	@Override
	public boolean allowClose() {
		return false;
	}
}
