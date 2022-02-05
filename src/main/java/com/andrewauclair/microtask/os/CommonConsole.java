// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.picocli.CustomPicocliCommands;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.jline.builtins.Completers;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Widget;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import picocli.CommandLine;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Paths;

public class CommonConsole {
	private static final char BACKSPACE_KEY = '\u0008';

	LineReader configureTerminal(Commands commands, Terminal terminal) {
		CommandLine cmd = commands.buildCommandLineWithAllCommands();

		CustomPicocliCommands picocliCommands = new CustomPicocliCommands(Paths.get(""), cmd);

		Completers.SystemCompleter systemCompleter = new Completers.SystemCompleter();
		systemCompleter.add(picocliCommands.compileCompleters());
		systemCompleter.compile();

		LineReader lineReader = LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(systemCompleter)
				.parser(new DefaultParser())
				.variable(LineReader.LIST_MAX, 50)
				.variable(LineReader.BELL_STYLE, "none")
				.variable(LineReader.HISTORY_FILE, OSInterfaceImpl.working_directory + "/history.txt")
				.build();

		loadHistory(lineReader);

		bindCtrlBackspace(lineReader);
		bindCtrlV(lineReader);

		return lineReader;
	}

	private void loadHistory(LineReader lineReader) {
		try {
			lineReader.getHistory().load();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void bindCtrlBackspace(LineReader lineReader) {
		KeyMap<Binding> main = lineReader.getKeyMaps().get(LineReader.MAIN);

		Widget widget = lineReader.getBuiltinWidgets().get(LineReader.BACKWARD_KILL_WORD);

		main.bind((Widget) () -> {
			short left_ctrl = User32.INSTANCE.GetAsyncKeyState(WinUser.VK_LCONTROL);
			short right_ctrl = User32.INSTANCE.GetAsyncKeyState(WinUser.VK_RCONTROL);

			if ((left_ctrl & 0x8000) == 0 && (right_ctrl & 0x8000) == 0) {
				return lineReader.getBuffer().backspace();
			}
			else {
				return widget.apply();
			}
		}, KeyMap.ctrl(BACKSPACE_KEY));
	}

	private void bindCtrlV(LineReader lineReader) {
		KeyMap<Binding> main = lineReader.getKeyMaps().get(LineReader.MAIN);

		main.bind((Widget) () -> {
			String clipboardContents = getClipboardContents();

			if (!clipboardContents.isEmpty()) {
				lineReader.getBuffer().write(clipboardContents);
			}

			return true;
		}, KeyMap.ctrl('v'));
	}

	/**
	 * Get the String residing on the clipboard.
	 *
	 * @return any text found on the Clipboard; if none found, return an
	 * empty String.
	 */
	private String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText =
				(contents != null) &&
						contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException | IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;
	}
}
