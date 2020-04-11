// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;

import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_Update_Test extends CommandsBaseTestCase {
	private final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.90.0.50", 8080));

	@Test
	void execute_update_command() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1", Proxy.NO_PROXY)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease("version-1", Proxy.NO_PROXY)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

		ByteArrayInputStream in = Mockito.mock(ByteArrayInputStream.class);
		System.setIn(in);

		Mockito.when(in.read()).then(invocationOnMock -> {
			assertOutput(
					"Updated to version 'version-1'",
					"",
					"This is a",
					"multiline",
					"changelog",
					"",
					"Press any key to shutdown. Please restart with the new version."
			);
			return 0;
		});

		commands.execute(printStream, "update --release version-1");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease("version-1", Proxy.NO_PROXY);
		//noinspection ResultOfMethodCallIgnored, don't need to do anything with the result because this is just a Mockito.verify
		inOrder.verify(in).read();
		inOrder.verify(osInterface).exit();

		assertOutput(
				"Updated to version 'version-1'",
				"",
				"This is a",
				"multiline",
				"changelog",
				"",
				"Press any key to shutdown. Please restart with the new version.",
				""
		);
	}

	@Test
	void update_prints_not_found_when_version_is_unknown() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1", Proxy.NO_PROXY)).thenReturn(false);

		commands.execute(printStream, "update --release version-1");

		assertOutput(
				"Version 'version-1' not found on GitLab",
				""
		);
	}

	@Test
	void update_prints_failure_if_updateToRelease_throws_exception() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1", Proxy.NO_PROXY)).thenThrow(IOException.class);

		commands.execute(printStream, "update --release version-1");

		assertOutput(
				"Failed to update to version 'version-1'",
				""
		);
	}

	@Test
	void update_to_release_with_proxy_settings() throws IOException {
		Mockito.when(gitLabReleases.updateToRelease("version-1", proxy)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease("version-1", proxy)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

		ByteArrayInputStream in = Mockito.mock(ByteArrayInputStream.class);
		System.setIn(in);

		Mockito.when(in.read()).then(invocationOnMock -> {
			assertOutput(
					"Updated to version 'version-1'",
					"",
					"This is a",
					"multiline",
					"changelog",
					"",
					"Press any key to shutdown. Please restart with the new version."
			);
			return 0;
		});

		commands.execute(printStream, "update --release version-1 --proxy-ip 10.90.0.50 --proxy-port 8080");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease("version-1", proxy);
		//noinspection ResultOfMethodCallIgnored, don't need to do anything with the result because this is just a Mockito.verify
		inOrder.verify(in).read();
		inOrder.verify(osInterface).exit();

		assertOutput(
				"Updated to version 'version-1'",
				"",
				"This is a",
				"multiline",
				"changelog",
				"",
				"Press any key to shutdown. Please restart with the new version.",
				""
		);
	}

	@Test
	void proxy_port_must_be_provided() {
		commands.execute(printStream, "update --release version-1 --proxy-ip 10.90.0.50");

		assertOutput(
				"Error: Missing required argument(s): --proxy-port=<proxy_port>",
				""
		);
	}

	@Test
	void proxy_ip_must_be_provided() {
		commands.execute(printStream, "update --release version-1 --proxy-port 8080");

		assertOutput(
				"Error: Missing required argument(s): --proxy-ip=<proxy_ip>",
				""
		);
	}

	@Test
	void print_out_releases() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList("version-1", "version-2", "version-3"));

		commands.execute(printStream, "update --releases");

		assertOutput(
				"Releases found on GitLab",
				"",
				"version-1",
				"version-2",
				ANSI_FG_GREEN + "version-3 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void print_out_releases_with_proxy() throws IOException {
		Mockito.when(gitLabReleases.getVersions(proxy)).thenReturn(Arrays.asList("version-1", "version-2", "version-3"));

		commands.execute(printStream, "update -r --proxy-ip 10.90.0.50 --proxy-port 8080");

		assertOutput(
				"Releases found on GitLab",
				"",
				"version-1",
				"version-2",
				ANSI_FG_GREEN + "version-3 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void lists_only_the_newest_5_versions_and_highlights_newest() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");

		commands.execute(printStream, "update -r");

		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5 " + ANSI_FG_GREEN + "-- current" + ConsoleColors.ANSI_RESET,
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void failure_to_retrieve_releases_from_gitlab() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenThrow(IOException.class);

		commands.execute(printStream, "update --releases");

		assertOutput(
				"Failed to get releases from GitLab",
				""
		);
	}

	@Test
	void update_to_latest_release() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList("version-1", "version-2", "version-3"));
		Mockito.when(gitLabReleases.updateToRelease("version-3", Proxy.NO_PROXY)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease("version-3", Proxy.NO_PROXY)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

		ByteArrayInputStream in = Mockito.mock(ByteArrayInputStream.class);
		System.setIn(in);

		Mockito.when(in.read()).then(invocationOnMock -> {
			assertOutput(
					"Updated to version 'version-3'",
					"",
					"This is a",
					"multiline",
					"changelog",
					"",
					"Press any key to shutdown. Please restart with the new version."
			);
			return 0;
		});

		commands.execute(printStream, "update --latest");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease("version-3", Proxy.NO_PROXY);
		//noinspection ResultOfMethodCallIgnored, don't need to do anything with the result because this is just a Mockito.verify
		inOrder.verify(in).read();
		inOrder.verify(osInterface).exit();

		assertOutput(
				"Updated to version 'version-3'",
				"",
				"This is a",
				"multiline",
				"changelog",
				"",
				"Press any key to shutdown. Please restart with the new version.",
				""
		);
	}

	@Test
	void update_to_latest_with_proxy_settings() throws IOException {
		Mockito.when(gitLabReleases.getVersions(proxy)).thenReturn(Arrays.asList("version-1", "version-2", "version-3"));
		Mockito.when(gitLabReleases.updateToRelease("version-3", proxy)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease("version-3", proxy)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

		ByteArrayInputStream in = Mockito.mock(ByteArrayInputStream.class);
		System.setIn(in);

		Mockito.when(in.read()).then(invocationOnMock -> {
			assertOutput(
					"Updated to version 'version-3'",
					"",
					"This is a",
					"multiline",
					"changelog",
					"",
					"Press any key to shutdown. Please restart with the new version."
			);
			return 0;
		});

		commands.execute(printStream, "update --latest --proxy-ip 10.90.0.50 --proxy-port 8080");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease("version-3", proxy);
		//noinspection ResultOfMethodCallIgnored, don't need to do anything with the result because this is just a Mockito.verify
		inOrder.verify(in).read();
		inOrder.verify(osInterface).exit();

		assertOutput(
				"Updated to version 'version-3'",
				"",
				"This is a",
				"multiline",
				"changelog",
				"",
				"Press any key to shutdown. Please restart with the new version.",
				""
		);
	}

	@Test
	void highlights_current_release() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");

		commands.execute(printStream, "update -r");

		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5 " + ANSI_FG_GREEN + "-- current" + ConsoleColors.ANSI_RESET,
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void current_and_latest_are_same_release() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.7");

		commands.execute(printStream, "update -r");

		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 " + ANSI_FG_GREEN + "-- current & latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void highlights_current_release_if_current_release_is_not_one_of_newest_5_releases() throws IOException {
		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.1");

		commands.execute(printStream, "update -r");

		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.1 " + ANSI_FG_GREEN + "-- current" + ConsoleColors.ANSI_RESET,
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void ignores_exception_for_getting_version() throws IOException {
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);

		Mockito.when(gitLabReleases.getVersions(Proxy.NO_PROXY)).thenReturn(Arrays.asList(
				"19.1.1",
				"19.1.2",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				"19.1.7"
		));

		commands.execute(printStream, "update -r");

		assertOutput(
				"Releases found on GitLab",
				"",
				"2 older releases",
				"",
				"19.1.3",
				"19.1.4",
				"19.1.5",
				"19.1.6",
				ANSI_FG_GREEN + "19.1.7 -- latest" + ConsoleColors.ANSI_RESET,
				""
		);
	}

	@Test
	void invalid_command() {
		commands.execute(printStream, "update");

		assertOutput(
				"Invalid command.",
				""
		);
	}

	@Test
	void update_command_help() {
		commands.execute(printStream, "update --help");

		assertOutput(
				"Usage:  update [--proxy-ip=<proxy_ip> --proxy-port=<proxy_port>] [-hlr]",
				"               [--from-remote] [--tasks] [--to-remote] [--release=<release>]",
				"      --from-remote",
				"  -h, --help                Show this help message.",
				"  -l, --latest",
				"      --proxy-ip=<proxy_ip>",
				"      --proxy-port=<proxy_port>",
				"",
				"  -r, --releases",
				"      --release=<release>",
				"      --tasks",
				"      --to-remote"
		);
	}
}
