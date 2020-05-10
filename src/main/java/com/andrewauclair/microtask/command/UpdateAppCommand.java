// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.ConsoleTable;
import com.andrewauclair.microtask.os.GitLabReleases;
import com.andrewauclair.microtask.os.OSInterface;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.ParseException;
import java.util.List;

import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

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
		@Option(names = {"--list-releases"}, description = "Display the available releases on GitLab.")
		private boolean list_releases;

		@Option(names = {"-l", "--latest"}, description = "Update to the latest release.")
		private boolean latest;

		@Option(names = {"--release"}, description = "Update to a specific release.")
		private String release;

		@Option(names = {"--list-snapshots"}, description = "List non-tag pipelines with artifacts available.")
		private boolean list_snapshots;

		@Option(names = {"--snapshot"}, description = "Update to a specific snapshot release.")
		private String snapshot;
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

//		List<String> versions;
		List<GitLabReleases.ReleasePipeline> releases;

		try {
//			versions = gitLabReleases.getVersions(proxy);
			releases = gitLabReleases.getReleases(proxy, args.list_snapshots || args.snapshot != null);
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to get releases from GitLab");
			System.out.println();
			return;
		}

		if (args.list_releases) {
			System.out.println("Releases found on GitLab");
			System.out.println();

			ConsoleTable table = new ConsoleTable(osInterface);
			table.setHeaders("Release Name", "");
			table.enableAlternatingColors();
//			table.setRowLimit(MAX_DISPLAYED_VERSIONS, false);

			String currentVersion = "";

			try {
				currentVersion = osInterface.getVersion();
			}
			catch (IOException ignored) {
			}

			int count = 0;

			for (final GitLabReleases.ReleasePipeline release : releases) {
				boolean isCurrentVersion = release.versionName.equals(currentVersion);
				boolean isLatest = release.equals(releases.get(0));

				String status = "";
				if (isLatest && isCurrentVersion) {
					status = "-- current & latest";
				}
				else if (isLatest) {
					status = "-- latest";
				}
				else if (isCurrentVersion) {
					status = "-- current";
				}

				if (isCurrentVersion) {
					table.addRow(ANSI_BG_GREEN, release.versionName, status);
				}
				else if (count < MAX_DISPLAYED_VERSIONS) {
					table.addRow(release.versionName, status);
				}

				count++;
			}

			table.print();
		}
		else if (args.latest) {
			if (releases.size() > 0) {
				updatedToNewRelease = updateToVersion(releases.get(0), proxy, false);
			}
		}
		else if (args.list_snapshots) {
			System.out.println("Snapshots found on GitLab");
			System.out.println();

			ConsoleTable table = new ConsoleTable(osInterface);
			table.setHeaders("Release Name");
			table.enableAlternatingColors();

			for (final GitLabReleases.ReleasePipeline release : releases) {
				table.addRow(release.versionName);
			}

			table.print();
		}
		else if (args.snapshot != null) {
			GitLabReleases.ReleasePipeline pipeline = null;
			for (final GitLabReleases.ReleasePipeline release : releases) {
				if (release.versionName.equals(args.snapshot)) {
					pipeline = release;
					break;
				}
			}
			if (pipeline == null) {
				System.out.println("Snapshot version '" + args.snapshot + "' not found on GitLab");
			}
			else {
				updatedToNewRelease = updateToVersion(pipeline, proxy, true);
			}
		}
		else {
			GitLabReleases.ReleasePipeline pipeline = null;
			for (final GitLabReleases.ReleasePipeline release : releases) {
				if (release.versionName.equals(args.release)) {
					pipeline = release;
					break;
				}
			}
			if (pipeline == null) {
				System.out.println("Version '" + args.release + "' not found on GitLab");
			}
			else {
				updatedToNewRelease = updateToVersion(pipeline, proxy, false);
			}
		}

		System.out.println();

		if (updatedToNewRelease) {
			osInterface.exit();
		}
	}

	private boolean updateToVersion(GitLabReleases.ReleasePipeline version, Proxy proxy, boolean snapshot) {
		try {
			boolean updated = gitLabReleases.updateToRelease(version, proxy);

			if (updated) {
				System.out.print("Updated to");
				if (snapshot) {
					System.out.print(" snapshot");
				}
				System.out.println(" version '" + version.versionName + "'");
				System.out.println();
				System.out.println(gitLabReleases.changelogForRelease(version, proxy, snapshot));
				System.out.println();
				System.out.println("Press any key to shutdown. Please restart with the new version.");

				// force a restart, but wait for the user to respond first
				//noinspection ResultOfMethodCallIgnored
				System.in.read();

				return true;
			}
			else {
				System.out.println("Version '" + version.versionName + "' not found on GitLab");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to update to version '" + version.versionName + "'");
		}

		return false;
	}
}
