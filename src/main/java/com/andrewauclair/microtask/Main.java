// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.MainConsole;
import com.andrewauclair.microtask.os.OSInterfaceImpl;
import com.andrewauclair.microtask.os.StatusConsole;

import java.util.Arrays;
import java.util.List;

public final class Main {
	public static MainConsole mainConsole;
	public static StatusConsole statusConsole;

	public static void main(String[] args) throws Exception {
//		System.out.println(Arrays.toString(args));

		if (args.length > 0 && args[0].equals("status")) {
			OSInterfaceImpl.disableGit = true; // don't use git on the status console

			boolean directory = false;
			if (args.length > 1 && args[1].equals("directory")) {
				// this status console is for displaying the directory structure of the current group or the active group, if there is one
				directory = true;
			}
			else {
				// displaying a custom display in the form of <x>:<y>:<width> <type> where:
				// x - x position in the console
				// y - y position in the console
				// width - max width to consume
				// type - the type of data to show, active-task or 0 for example
				List<String> strs = Arrays.asList(args);
//				strs.remove(0); // status


			}

			statusConsole = new StatusConsole(directory);

			System.exit(0);
		}
		else {
			if (args.length > 0 && args[0].equals("--disable-git")) {
				OSInterfaceImpl.disableGit = true;
			}
			else if (args.length > 1 && args[0].equals("working-dir")) {
				OSInterfaceImpl.working_directory = args[1];
			}
			mainConsole = new MainConsole();
		}
	}
}
