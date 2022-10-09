package com.andrewauclair.v2.microtask.console;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.os.OSInterfaceImpl;

import javax.swing.Timer;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StatusConsole extends Console implements ComponentListener {
	private final Commands commands;
	private OSInterfaceImpl osInterface;
	private final String command;

	private final Timer timer;
	private final Timer updateOnResize = new Timer(500, e-> update());

	public StatusConsole(Commands commands, OSInterfaceImpl osInterface, String name, String command, boolean updateOnInterval, int interval) {
		super(name, true);
		this.commands = commands;
		this.osInterface = osInterface;

		this.command = command;

		if (updateOnInterval) {
			timer = new Timer(interval * 1000, e -> update());
			timer.setRepeats(true);
			timer.start();
		}
		else {
			timer = null;
		}

		outputArea.addComponentListener(this);
	}

	public void remove() {
		if (timer != null) {
			timer.stop();
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

		FontMetrics fontMetrics = outputArea.getFontMetrics(outputArea.getFont());

		Rectangle2D bounds = fontMetrics.getStringBounds("W", getGraphics());

		double fullWidth = scrollPane.getWidth() - scrollPane.getInsets().left - scrollPane.getInsets().right;
		double fullHeight = scrollPane.getHeight() - scrollPane.getInsets().top - scrollPane.getInsets().bottom;

		osInterface.width = (int) (fullWidth / bounds.getWidth());
		osInterface.height = (int) (fullHeight / bounds.getHeight());

		commands.execute(System.out, command);

		String str = stream.toString();

		appendOutput(str);
	}

	@Override
	protected void handleInput(String input) {
		// no-op, status consoles don't receive input
	}

	@Override
	public boolean allowClose() {
		return true;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		updateOnResize.restart();
	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}
}
