// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.os.OSInterface;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Command(name = "app", description = "Update the application.")
public class UpdateAppCommand implements Runnable {
	private static final int MAX_DISPLAYED_VERSIONS = 5;

	private final GitLabReleases gitLabReleases;
	private final OSInterface osInterface;

	private static final class ProxySettings {
		@Option(names = {"--proxy-ip"}, required = true, description = "Proxy IP address to use for connecting to GitLab.")
		private InetAddress proxy_ip;

		@Option(names = {"--proxy-port"}, required = true, description = "Proxy port to use for connecting to GitLab.")
		private int proxy_port;
	}

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	private static class Args {
		@Option(names = {"-r", "--releases"}, description = "Display the available releases on GitLab.")
		private boolean releases;

		@Option(names = {"-l", "--latest"}, description = "Update to the latest release.")
		private boolean latest;

		@Option(names = {"--release"}, description = "Update to a specific release.")
		private String release;
	}

	@ArgGroup()
	private Args args;

	@ArgGroup(exclusive = false)
	private ProxySettings proxy;

	public UpdateAppCommand(GitLabReleases gitLabReleases, OSInterface osInterface) {
		this.gitLabReleases = gitLabReleases;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		Proxy proxy = Proxy.NO_PROXY;

		boolean updatedToNewRelease = false;

		if (this.proxy != null) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxy.proxy_ip, this.proxy.proxy_port));
		}

		List<String> versions;

		try {
			versions = gitLabReleases.getVersions(proxy);
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to get releases from GitLab");
			System.out.println();
			return;
		}

		if (args.releases) {
			System.out.println("Releases found on GitLab");
			System.out.println();

			int toDisplay = Math.min(versions.size(), MAX_DISPLAYED_VERSIONS);

			String currentVersion = "";

			try {
				currentVersion = osInterface.getVersion();
			}
			catch (IOException ignored) {
			}

			if (versions.size() > 5) {
				System.out.println((versions.size() - 5) + " older releases");
				System.out.println();

				if (!versions.subList(versions.size() - 5, versions.size()).contains(currentVersion) && !currentVersion.isEmpty()) {
					System.out.print(currentVersion);
					System.out.print(" ");
					System.out.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
					System.out.print("-- current");
					System.out.println(ConsoleColors.ANSI_RESET);
				}
			}

			for (int i = versions.size() - toDisplay; i < versions.size(); i++) {
				if (i + 1 == versions.size()) {
					System.out.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
				}
				System.out.print(versions.get(i));

				if (versions.get(i).equals(currentVersion)) {
					System.out.print(" ");
					System.out.print(ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN);
					System.out.print("-- current");

					if (i + 1 == versions.size()) {
						System.out.print(" & latest");
					}
					System.out.print(ConsoleColors.ANSI_RESET);
				}
				else if (i + 1 == versions.size()) {
					System.out.print(" -- latest");
					System.out.print(ConsoleColors.ANSI_RESET);
				}

				System.out.println();
			}
		}
		else if (args.latest) {
			updatedToNewRelease = updateToVersion(versions.get(versions.size() - 1), proxy);
		}
		else {
			updatedToNewRelease = updateToVersion(args.release, proxy);
		}

		System.out.println();

		if (updatedToNewRelease) {
			osInterface.exit();
		}
	}

	private boolean updateToVersion(String version, Proxy proxy) {
		try {
			boolean updated = gitLabReleases.updateToRelease(version, proxy);

			if (updated) {
				System.out.println("Updated to version '" + version + "'");
				System.out.println();
				System.out.println(gitLabReleases.changelogForRelease(version, proxy));
				System.out.println();
				System.out.println("Press any key to shutdown. Please restart with the new version.");

				// force a restart, but wait for the user to respond first
				//noinspection ResultOfMethodCallIgnored
				System.in.read();

				return true;
			}
			else {
				System.out.println("Version '" + version + "' not found on GitLab");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to update to version '" + version + "'");
		}

		return false;
	}
}
