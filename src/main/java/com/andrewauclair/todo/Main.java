// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.jline.ActiveListCompleter;
import com.andrewauclair.todo.jline.ActiveTaskCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.jline.RenameCompleter;
import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
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
import java.util.Properties;
import java.util.Scanner;

public class Main {
	private static final char BACKSPACE_KEY = '\u0008';
	
	public static void main(String[] args) throws IOException {
		OSInterface osInterface = new OSInterface();
		Tasks tasks = new Tasks(getStartingID(osInterface), new TaskWriter(System.out, osInterface), System.out, osInterface);
		Commands commands = new Commands(tasks, System.out);
		
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
		
		readTasks(osInterface, tasks);
		
		Terminal terminal = TerminalBuilder.builder()
				.jna(true)
				.nativeSignals(true)
				.build();
		
		Completers.TreeCompleter treeCompleter = new Completers.TreeCompleter(
				Completers.TreeCompleter.node("active"),
				Completers.TreeCompleter.node("clear"),
				Completers.TreeCompleter.node("exit"),
				Completers.TreeCompleter.node("list",
						Completers.TreeCompleter.node("--tasks",
								Completers.TreeCompleter.node("--list",
										Completers.TreeCompleter.node(new ListCompleter(tasks))
								)
						),
						Completers.TreeCompleter.node("--lists")
				),
				Completers.TreeCompleter.node("debug"),
				Completers.TreeCompleter.node("add"),
				Completers.TreeCompleter.node("start"),
				Completers.TreeCompleter.node("stop"),
				Completers.TreeCompleter.node("finish"),
				
				Completers.TreeCompleter.node("times",
						Completers.TreeCompleter.node("--task",
								Completers.TreeCompleter.node(new ActiveTaskCompleter(tasks))
						),
						Completers.TreeCompleter.node("--list",
								Completers.TreeCompleter.node(new ActiveListCompleter(tasks))
						),
						Completers.TreeCompleter.node("--today")
				),
				
				Completers.TreeCompleter.node("create-list"),
				Completers.TreeCompleter.node("switch-list",
						Completers.TreeCompleter.node(new ListCompleter(tasks)
						)
				),
				Completers.TreeCompleter.node("search"),
				
				Completers.TreeCompleter.node("rename",
						Completers.TreeCompleter.node(new RenameCompleter(tasks))
				),

				Completers.TreeCompleter.node("version")
		);
		
		LineReader lineReader = LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(treeCompleter)
				.variable(LineReader.BELL_STYLE, "none")
				.build();
		
		bindCtrlBackspace(lineReader);
		
		osInterface.clearScreen();
		
		System.out.println(terminal.getSize());
		
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("version.properties");
		Properties props = new Properties();
		props.load(resourceAsStream);
		
		String version = (String) props.get("version");
		
		while (true) {
			try {
				String command = lineReader.readLine(commands.getPrompt());

				// TODO Move this to Commands and the version loading to os interface as a String getVersion() method
				if (command.equals("version")) {
					System.out.println(version);
					System.out.println();
				}
				else if (command.equals("update --releases") || command.equals("update -r")) {
					GitLabReleases.printReleases();
				}
				else if (command.equals("update --latest") || command.equals("update -l")) {
					GitLabReleases.updateToRelease("");
				}
				else if (command.startsWith("update")) {
					GitLabReleases.updateToRelease(command.split(" ")[1]);
				}
				else {
					commands.execute(command);
				}
			}
			catch (UserInterruptException ignored) {
			}
			catch (EndOfFileException e) {
				return;
			}
		}
	}
	
	private static void readTasks(OSInterface osInterface, Tasks tasks) throws IOException {
		TaskReader reader = new TaskReader(osInterface);
		
		File[] files = new File("git-data/tasks").listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					String listName = file.getName();
					tasks.addList(listName);
					tasks.setCurrentList(listName);
					
					File[] listTasks = file.listFiles();
					
					if (listTasks != null) {
						for (File listTask : listTasks) {
							if (listTask.getName().endsWith(".txt")) {
								Task task = reader.readTask("git-data/tasks/" + listName + "/" + listTask.getName());
								
								tasks.addTask(task);
							}
						}
					}
				}
			}
		}
	}
	
	private static void bindCtrlBackspace(LineReader lineReader) {
		KeyMap<Binding> main = lineReader.getKeyMaps().get(LineReader.MAIN);
		
		Widget widget = lineReader.getBuiltinWidgets().get(LineReader.BACKWARD_KILL_WORD);
		
		main.bind((Widget) () -> {
			short keyState = User32.INSTANCE.GetAsyncKeyState(WinUser.VK_LCONTROL);
			
			if (keyState == 0) {
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
