// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor;
import com.andrewauclair.microtask.os.OSInterface;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;

@Command(name = "version")
public final class VersionCommand implements Runnable {
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	VersionCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		printLogo(osInterface);
	}

	public static void printLogo(OSInterface osInterface) {
		ConsoleForegroundColor micro = ConsoleForegroundColor.ANSI_FG_PURPLE;
		ConsoleForegroundColor task = ConsoleForegroundColor.ANSI_FG_CYAN;

		String version;
		try {
			version = osInterface.getVersion();
		}
		catch (IOException e) {
			version = "Unknown";
		}

		System.out.println(micro + "               _                    " + task + "   __                __  " + ANSI_RESET);
		System.out.println(micro + "   ____ ___   (_)_____ _____ ____   " + task + "  / /_ ____ _ _____ / /__ " + System.getProperty("java.runtime.version") + ANSI_RESET);
		System.out.println(micro + "  / __ `__ \\ / // ___// ___// __ \\  " + task + " / __// __ `// ___// //_/" + ANSI_RESET);
		System.out.println(micro + " / / / / / // // /__ / /   / /_/ /  " + task + "/ /_ / /_/ /(__  )/ ,<   " + ANSI_RESET + ANSI_BOLD + version + ANSI_RESET);
		System.out.println(micro + "/_/ /_/ /_//_/ \\___//_/    \\____/   " + task + "\\__/ \\__,_//____//_/|_|  " + ANSI_RESET);
		System.out.println(micro + "                                    " + task + "                         " + ANSI_RESET);
		System.out.println(ANSI_BOLD + "Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved" + ANSI_RESET);
		System.out.println();
	}
}
