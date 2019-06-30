// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import com.sun.jna.Function;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		OSInterface osInterface = new OSInterface();
		Tasks tasks = new Tasks(getStartingID(osInterface), new TaskWriter(System.out, osInterface), System.out, osInterface);
		Commands commands = new Commands(tasks, System.out);
		
		setConsoleMode();
		
		getConsoleSize();
		
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
		
		String command;
		Scanner scanner = new Scanner(System.in);
		do {
			System.out.print(commands.getPrompt());
			command = scanner.nextLine();
			
			if (command.equals("clear")) {
				clearScreen();
			}
			else if (!command.equals("exit")) {
				try {
					commands.execute(command);
				}
				catch (RuntimeException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		while (!command.equals("exit"));
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
	
	private static void clearScreen() {
		//Clears Screen in java
		try {
			if (System.getProperty("os.name").contains("Windows")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			}
			else {
				Runtime.getRuntime().exec("clear");
			}
		}
		catch (IOException | InterruptedException ignored) {
		}
	}
	
	// TODO This doesn't work on Windows 7. I don't plan on supporting it, but I should at least have a way to turn off the color outputs if we can't turn on virtual terminal mode
	private static void setConsoleMode() {
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Set output mode to handle virtual terminal sequences
			Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
			DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
			HANDLE hOut = (HANDLE) GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});
			
			DWORDByReference p_dwMode = new DWORDByReference(new DWORD(0));
			Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
			GetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, p_dwMode});
			
			int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
			DWORD dwMode = p_dwMode.getValue();
			dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
			Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
			BOOL result = (BOOL) SetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, dwMode});
			
			if (!result.booleanValue()) {
				// failed to set virtual terminal mode
				Function GetLastError = Function.getFunction("kernel32", "GetLastError");
				int error = (Integer) GetLastError.invoke(DWORD.class, new Object[]{});
				
				System.out.println("Failed to enable Virtual Terminal Mode, Error: " + error);
			}
		}
	}
	
	private static void getConsoleSize() throws InterruptedException {
		// GetConsoleScreenBufferInfo
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Set output mode to handle virtual terminal sequences
			Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
			DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
			HANDLE hOut = (HANDLE) GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});
			
			_CONSOLE_SCREEN_BUFFER_INFO bufferInfo = new _CONSOLE_SCREEN_BUFFER_INFO();
			Function GetConsoleScreenBufferInfo = Function.getFunction("kernel32", "GetConsoleScreenBufferInfo");
			BOOL result = (BOOL) GetConsoleScreenBufferInfo.invoke(BOOL.class, new Object[]{hOut, bufferInfo});
			
			if (!result.booleanValue()) {
				// failed to get console screen buffer info
				Function GetLastError = Function.getFunction("kernel32", "GetLastError");
				int error = (Integer) GetLastError.invoke(DWORD.class, new Object[]{});
				
				System.out.println("Failed to get console screen buffer info, Error: " + error);
			}
			else {
				// TODO Print out the size here
				System.out.println(bufferInfo.toString());
				
				char escCode = 0x1B;
				int row = 10;
				int column = 10;
				System.out.print(String.format("%c[%d;%df", escCode, row, column));
				
				for (int y = 0; y < bufferInfo.srWindow.Right; y++) {
					for (int x = 0; x < bufferInfo.srWindow.Bottom; x++) {
						System.out.print(String.format("%c[%d;%df%c", escCode, x, y, 'X'));
//						Thread.sleep(10);
					}
				}
				
				
			}
		}
	}
	
	public static class _CONSOLE_SCREEN_BUFFER_INFO extends Structure {
		public COORD dwSize;
		public COORD dwCursorPosition;
		public WinDef.WORD wAttributes;
		public SMALL_RECT srWindow;
		public COORD dwMaximumWindowSize;
		
		protected List getFieldOrder() {
			return Arrays.asList("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize");
		}
		
		@Override
		public String toString() {
			return "_CONSOLE_SCREEN_BUFFER_INFO{" +
					"dwSize=" + dwSize +
					", dwCursorPosition=" + dwCursorPosition +
					", wAttributes=" + wAttributes +
					", srWindow=" + srWindow +
					", dwMaximumWindowSize=" + dwMaximumWindowSize +
					'}';
		}
	}
	
	public static class COORD extends Structure {
		public short x;
		public short y;
		
		protected List getFieldOrder() {
			return Arrays.asList("x", "y");
		}
		
		@Override
		public String toString() {
			return "COORD{" +
					"x=" + x +
					", y=" + y +
					'}';
		}
	}
	
	public static class SMALL_RECT extends Structure {
		public short Left;
		public short Top;
		public short Right;
		public short Bottom;
		
		protected List getFieldOrder() {
			return Arrays.asList("Left", "Top", "Right", "Bottom");
		}
		
		@Override
		public String toString() {
			return "SMALL_RECT{" +
					"Left=" + Left +
					", Top=" + Top +
					", Right=" + Right +
					", Bottom=" + Bottom +
					'}';
		}
	}
}
