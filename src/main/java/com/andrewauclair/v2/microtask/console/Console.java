// Copyright (C) 2022 Andrew Auclair - All Rights Reserved

package com.andrewauclair.v2.microtask.console;

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.DockingRegion;
import com.andrewauclair.microtask.os.ConsoleColors;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.*;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.*;

public abstract class Console extends JPanel implements Dockable {
	JTextPane outputArea = new JTextPane();

	private final JTextField inputField = new JTextField();
	private final String name;

	private static final Color defaultForegroundColor = new Color(7, 54, 66);
	private static final Color defaultBackgroundColor = new Color(238, 232, 213);

	private Color foregroundColor = defaultForegroundColor;
	private Color backgroundColor = defaultBackgroundColor;

	private final Map<ConsoleColors.ConsoleForegroundColor, Color> foregroundColors = new HashMap<>() {{
		put(ANSI_FG_BLACK, Color.BLACK.darker());
		put(ANSI_FG_RED, Color.RED.darker());
		put(ANSI_FG_GREEN, Color.GREEN.darker());
		put(ANSI_FG_YELLOW, Color.YELLOW.darker());
		put(ANSI_FG_BLUE, new Color(253, 246, 227));
		put(ANSI_FG_PURPLE, new Color(108, 113, 196));
		put(ANSI_FG_CYAN, Color.CYAN.darker());
		put(ANSI_FG_WHITE, new Color(7, 54, 66));
		put(ANSI_FG_BRIGHT_BLACK, Color.BLACK);
		put(ANSI_FG_BRIGHT_RED, Color.RED);
		put(ANSI_FG_BRIGHT_GREEN, Color.GREEN);
		put(ANSI_FG_BRIGHT_YELLOW, Color.YELLOW);
		put(ANSI_FG_BRIGHT_BLUE, Color.BLUE);
		put(ANSI_FG_BRIGHT_PURPLE, new Color(255, 0, 255));
		put(ANSI_FG_BRIGHT_CYAN, Color.CYAN);
		put(ANSI_FG_BRIGHT_WHITE, Color.WHITE);
	}};

	private final Map<ConsoleColors.ConsoleBackgroundColor, Color> backgroundColors = new HashMap<>() {{
		put(ANSI_BG_BLACK, Color.BLACK.darker());
		put(ANSI_BG_RED, Color.RED.darker());
		put(ANSI_BG_GREEN, Color.GREEN.darker());
		put(ANSI_BG_YELLOW, Color.YELLOW.darker());
		put(ANSI_BG_BLUE, new Color(253, 246, 227));
		put(ANSI_BG_PURPLE, new Color(108, 113, 196));
		put(ANSI_BG_CYAN, Color.CYAN.darker());
		put(ANSI_BG_WHITE, new Color(7, 54, 66));
		put(ANSI_BG_BRIGHT_BLACK, Color.BLACK);
		put(ANSI_BG_BRIGHT_RED, Color.RED);
		put(ANSI_BG_BRIGHT_GREEN, Color.GREEN);
		put(ANSI_BG_BRIGHT_YELLOW, Color.YELLOW);
		put(ANSI_BG_BRIGHT_BLUE, Color.BLUE);
		put(ANSI_BG_BRIGHT_PURPLE, new Color(255, 0, 255));
		put(ANSI_BG_BRIGHT_CYAN, Color.CYAN);
		put(ANSI_BG_BRIGHT_WHITE, Color.WHITE);
	}};

	public Console(String name, boolean readOnly) {
		this.name = name;

		Docking.registerDockable(this);

		create(readOnly);
	}

	public void create(boolean readOnly) {
		outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
		inputField.setFont(new Font("Consolas", Font.PLAIN, 14));

		//Make outputArea read-only
		outputArea.setEditable(false);

		//Set component backgrounds to BLACK and text color to WHITE to make it look more like a console
		outputArea.setBackground(defaultBackgroundColor);
		inputField.setBackground(defaultBackgroundColor);
		outputArea.setForeground(defaultForegroundColor);
		inputField.setForeground(defaultForegroundColor);

		//Add components
		setLayout(new BorderLayout());
		add(new JScrollPane(outputArea),BorderLayout.CENTER);

		if (!readOnly) {
			add(inputField, BorderLayout.SOUTH);
		}

		//This listener listens for ENTER key on the inputField.
		inputField.addActionListener(e -> {
			String text = inputField.getText();
			inputField.setText("");

			handleInput(text);
		});
	}

	protected void appendOutput(String str) {
		if (str.contains("\u001B")) {
			String[] split = str.split("\u001B");

			for (String s : split) {
				if (s.isEmpty()) {
					continue;
				}
				else if (!s.startsWith("[")) {
					appendText(s);
					continue;
				}
				String color = "\u001B" + s.substring(0, s.indexOf('m') + 1);

				if (color.equals("\u001B[0m")) {
					foregroundColor = defaultForegroundColor;
					backgroundColor = defaultBackgroundColor;

					appendText(s.substring(s.indexOf('m') + 1));
				}
				else {
					for (final ConsoleColors.ConsoleForegroundColor value : ConsoleColors.ConsoleForegroundColor.values()) {
						if (value.toString().equals(color)) {
							foregroundColor = foregroundColors.get(value);
							break;
						}
					}

					for (final ConsoleColors.ConsoleBackgroundColor value : ConsoleColors.ConsoleBackgroundColor.values()) {
						if (value.toString().equals(color)) {
							backgroundColor = backgroundColors.get(value);
							break;
						}
					}

					appendText(s.substring(s.indexOf('m') + 1));
				}

			}
		}
		else {
			appendText(str);
		}
	}

	private void appendText(String str) {
		outputArea.setEditable(true);

		// break the string up by the color codes
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, foregroundColor);

		aset = sc.addAttribute(aset, StyleConstants.Background, backgroundColor);

		int len = outputArea.getDocument().getLength();
		outputArea.setCaretPosition(len);
		outputArea.setCharacterAttributes(aset, false);
		outputArea.replaceSelection(str);

		outputArea.setEditable(false);
	}

	public void clear() {
		try {
			outputArea.getDocument().remove(0, outputArea.getDocument().getLength());
		}
		catch (BadLocationException ignored) {
		}
	}

	protected abstract void handleInput(String input);

	@Override
	public String persistentID() {
		return name;
	}

	@Override
	public int type() {
		return 0;
	}

	@Override
	public String tabText() {
		return name;
	}

	@Override
	public Icon tabIcon() {
		return null;
	}

	@Override
	public boolean floatingAllowed() {
		return false;
	}

	@Override
	public boolean limitToRoot() {
		return false;
	}

	@Override
	public List<DockingRegion> disallowedRegions() {
		return Collections.emptyList();
	}

	@Override
	public boolean allowClose() {
		return false;
	}

	@Override
	public boolean allowPinning() {
		return false;
	}

	@Override
	public boolean allowMinMax() {
		return false;
	}

	@Override
	public boolean hasMoreOptions() {
		return false;
	}
}