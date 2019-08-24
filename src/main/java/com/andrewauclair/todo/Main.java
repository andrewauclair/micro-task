// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.os.OSInterfaceImpl;
import com.andrewauclair.todo.task.TaskLoader;
import com.andrewauclair.todo.task.TaskReader;
import com.andrewauclair.todo.task.TaskWriter;
import com.andrewauclair.todo.task.Tasks;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.jline.builtins.Completers;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Main {
	private static final char BACKSPACE_KEY = '\u0008';
	
	public static void main(String[] args) throws IOException {
		OSInterfaceImpl osInterface = new OSInterfaceImpl();
		Tasks tasks = new Tasks(getStartingID(osInterface), new TaskWriter(System.out, osInterface), System.out, osInterface);
		Commands commands = new Commands(tasks, new GitLabReleases(), osInterface);
		
		osInterface.setCommands(commands);
		
		File git_data = new File("git-data");
		
		boolean exists = git_data.exists();
		
		if (!exists) {
			boolean mkdir = git_data.mkdir();
			
			System.out.println(mkdir);
			
			osInterface.runGitCommand("git init");
			osInterface.runGitCommand("git config user.email \"mightymalakai33@gmail.com\"");
			osInterface.runGitCommand("git config user.name \"Andrew Auclair\"");
		}

		boolean exception = false;

		try {
			TaskLoader loader = new TaskLoader(tasks, new TaskReader(osInterface), osInterface);
			loader.load();
		}
		catch (Exception e) {
			System.out.println(ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED + "Failed to read tasks." + ConsoleColors.ANSI_RESET);
			e.printStackTrace();

			exception = true;
		}

		Terminal terminal = TerminalBuilder.builder()
				.jna(true)
				.nativeSignals(true)
				.build();

		Completers.TreeCompleter treeCompleter = new Completers.TreeCompleter(commands.getAutoCompleteNodes());
		
		LineReader lineReader = LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(treeCompleter)
				.variable(LineReader.BELL_STYLE, "none")
				.build();
		
		bindCtrlBackspace(lineReader);

		if (!exception) {
			osInterface.clearScreen();
		}

//		System.out.println(terminal.getSize());

		if (tasks.getActiveTaskID() != Tasks.NO_ACTIVE_TASK) {
			// set active list to the list of the active task
			tasks.setActiveList(tasks.getActiveTaskList());

			// set active group to the group of the active task
			tasks.switchGroup(tasks.groupNameFromList(tasks.getActiveTaskList()));
		}

		while (true) {
			try {
				String command = lineReader.readLine(commands.getPrompt());

				commands.execute(System.out, command);
			}
			catch (UserInterruptException ignored) {
			}
			catch (EndOfFileException e) {
				return;
			}
		}
	}
	
	private static void bindCtrlBackspace(LineReader lineReader) {
		KeyMap<Binding> main = lineReader.getKeyMaps().get(LineReader.MAIN);
		
		Widget widget = lineReader.getBuiltinWidgets().get(LineReader.BACKWARD_KILL_WORD);
		
		main.bind((Widget) () -> {
			short keyState = User32.INSTANCE.GetAsyncKeyState(WinUser.VK_LCONTROL);
			
			if ((keyState & 0x8000) == 0) {
				return lineReader.getBuffer().backspace();
			}
			else {
				return widget.apply();
			}
		}, KeyMap.ctrl(BACKSPACE_KEY));
	}
	
	private static long getStartingID(OSInterface osInterface) {
		try (InputStream inputStream = osInterface.createInputStream("git-data/next-id.txt")) {
			Scanner scanner = new Scanner(inputStream);
			return scanner.nextLong();
		}
		catch (IOException ignored) {
		}
		return 1;
	}
}
