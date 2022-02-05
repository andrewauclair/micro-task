// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.os.GitLabReleases;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

class Commands_Update_Test extends CommandsBaseTestCase {
	private final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.90.0.50", 8080));

	@Test
	void execute_update_command() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(
				Collections.singletonList(
						new GitLabReleases.ReleasePipeline("version-1", 5)
				)
		);
		Mockito.when(gitLabReleases.updateToRelease(
				new GitLabReleases.ReleasePipeline("version-1", 5),
				Proxy.NO_PROXY
		)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease(
				new GitLabReleases.ReleasePipeline("version-1", 5),
				Proxy.NO_PROXY,
				false
		)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

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

		commands.execute(printStream, "update app --release version-1");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease(new GitLabReleases.ReleasePipeline("version-1", 5), Proxy.NO_PROXY);
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
		Mockito.when(gitLabReleases.updateToRelease(new GitLabReleases.ReleasePipeline("version-1", 5), Proxy.NO_PROXY)).thenReturn(false);

		commands.execute(printStream, "update app --release version-1");

		assertOutput(
				"Version 'version-1' not found on GitLab",
				""
		);
	}

	@Test
	void update_prints_failure_if_updateToRelease_throws_exception() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(
				Collections.singletonList(
						new GitLabReleases.ReleasePipeline("version-1", 5)
				)
		);
		Mockito.when(gitLabReleases.updateToRelease(new GitLabReleases.ReleasePipeline("version-1", 5), Proxy.NO_PROXY)).thenThrow(IOException.class);

		commands.execute(printStream, "update app --release version-1");

		assertOutput(
				"Failed to update to version 'version-1'",
				""
		);
	}

	@Test
	void update_to_release_with_proxy_settings() throws IOException {
		Mockito.when(gitLabReleases.getReleases(proxy, false)).thenReturn(
				Collections.singletonList(
						new GitLabReleases.ReleasePipeline("version-1", 5)
				)
		);
		Mockito.when(gitLabReleases.updateToRelease(new GitLabReleases.ReleasePipeline("version-1", 5), proxy)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease(new GitLabReleases.ReleasePipeline("version-1", 5), proxy, false)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

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

		commands.execute(printStream, "update app --release version-1 --proxy-ip 10.90.0.50 --proxy-port 8080");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease(new GitLabReleases.ReleasePipeline("version-1", 5), proxy);
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
		commands.execute(printStream, "update app --release version-1 --proxy-ip 10.90.0.50");

		assertOutput(
				"Error: Missing required argument(s): --proxy-port=<proxy_port>",
				""
		);
	}

	@Test
	void proxy_ip_must_be_provided() {
		commands.execute(printStream, "update app --release version-1 --proxy-port 8080");

		assertOutput(
				"Error: Missing required argument(s): --proxy-ip=<proxy_ip>",
				""
		);
	}

	@Test
	void print_out_releases() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(
				Arrays.asList(
						new GitLabReleases.ReleasePipeline("version-3", 3),
						new GitLabReleases.ReleasePipeline("version-2", 2),
						new GitLabReleases.ReleasePipeline("version-1", 1)
				));

		commands.execute(printStream, "update app --list-releases");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "         ",
				ANSI_BG_GRAY + "version-3     -- latest" + ANSI_RESET,
				"version-2              ",
				ANSI_BG_GRAY + "version-1              " + ANSI_RESET,
				""
		);
	}

	@Test
	void print_out_releases_with_proxy() throws IOException {
		Mockito.when(gitLabReleases.getReleases(proxy, false)).thenReturn(
				Arrays.asList(
						new GitLabReleases.ReleasePipeline("version-3", 5),
						new GitLabReleases.ReleasePipeline("version-2", 5),
						new GitLabReleases.ReleasePipeline("version-1", 5)
				)
		);

		commands.execute(printStream, "update app --list-releases --proxy-ip 10.90.0.50 --proxy-port 8080");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "         ",
				ANSI_BG_GRAY + "version-3     -- latest" + ANSI_RESET,
				"version-2              ",
				ANSI_BG_GRAY + "version-1              " + ANSI_RESET,
				""
		);
	}

	@Test
	void lists_only_the_newest_5_versions_and_highlights_newest() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(
				Arrays.asList(
						new GitLabReleases.ReleasePipeline("19.1.7", 5),
						new GitLabReleases.ReleasePipeline("19.1.6", 5),
						new GitLabReleases.ReleasePipeline("19.1.5", 5),
						new GitLabReleases.ReleasePipeline("19.1.4", 5),
						new GitLabReleases.ReleasePipeline("19.1.3", 5),
						new GitLabReleases.ReleasePipeline("19.1.2", 5),
						new GitLabReleases.ReleasePipeline("19.1.1", 5)
				)
		);
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");

		commands.execute(printStream, "update app --list-releases");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "          ",
				ANSI_BG_GRAY + "19.1.7        -- latest " + ANSI_RESET,
				"19.1.6                  ",
				ANSI_BG_GREEN + "19.1.5        -- current" + ANSI_RESET,
				"19.1.4                  ",
				ANSI_BG_GRAY + "19.1.3                  " + ANSI_RESET,
				""
		);
	}

	@Test
	void failure_to_retrieve_releases_from_gitlab() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenThrow(IOException.class);

		commands.execute(printStream, "update app --list-releases");

		assertOutput(
				"Failed to get releases from GitLab",
				""
		);
	}

	@Test
	void update_to_latest_release() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(
				Arrays.asList(
						new GitLabReleases.ReleasePipeline("version-3", 5),
						new GitLabReleases.ReleasePipeline("version-2", 5),
						new GitLabReleases.ReleasePipeline("version-1", 5)
				)
		);
		Mockito.when(gitLabReleases.updateToRelease(new GitLabReleases.ReleasePipeline("version-3", 5), Proxy.NO_PROXY)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease(new GitLabReleases.ReleasePipeline("version-3", 5), Proxy.NO_PROXY, false)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

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

		commands.execute(printStream, "update app --latest");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease(new GitLabReleases.ReleasePipeline("version-3", 5), Proxy.NO_PROXY);
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
		Mockito.when(gitLabReleases.getReleases(proxy, false)).thenReturn(
				Arrays.asList(
						new GitLabReleases.ReleasePipeline("version-3", 5),
						new GitLabReleases.ReleasePipeline("version-2", 5),
						new GitLabReleases.ReleasePipeline("version-1", 5)
				)
		);
		Mockito.when(gitLabReleases.updateToRelease(new GitLabReleases.ReleasePipeline("version-3", 5), proxy)).thenReturn(true);
		Mockito.when(gitLabReleases.changelogForRelease(new GitLabReleases.ReleasePipeline("version-3", 5), proxy, false)).thenReturn("This is a" + Utils.NL + "multiline" + Utils.NL + "changelog");

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

		commands.execute(printStream, "update app --latest --proxy-ip 10.90.0.50 --proxy-port 8080");

		InOrder inOrder = Mockito.inOrder(gitLabReleases, in, osInterface);

		inOrder.verify(gitLabReleases).updateToRelease(new GitLabReleases.ReleasePipeline("version-3", 5), proxy);
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
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(Arrays.asList(
				new GitLabReleases.ReleasePipeline("19.1.7", 7),
				new GitLabReleases.ReleasePipeline("19.1.6", 6),
				new GitLabReleases.ReleasePipeline("19.1.5", 5),
				new GitLabReleases.ReleasePipeline("19.1.4", 4),
				new GitLabReleases.ReleasePipeline("19.1.3", 3),
				new GitLabReleases.ReleasePipeline("19.1.2", 2),
				new GitLabReleases.ReleasePipeline("19.1.1", 1)
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");

		commands.execute(printStream, "update app --list-releases");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "          ",
				ANSI_BG_GRAY + "19.1.7        -- latest " + ANSI_RESET,
				"19.1.6                  ",
				ANSI_BG_GREEN + "19.1.5        -- current" + ANSI_RESET,
				"19.1.4                  ",
				ANSI_BG_GRAY + "19.1.3                  " + ANSI_RESET,
				""
		);
	}

	@Test
	void current_and_latest_are_same_release() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(Arrays.asList(
				new GitLabReleases.ReleasePipeline("19.1.7", 1),
				new GitLabReleases.ReleasePipeline("19.1.6", 2),
				new GitLabReleases.ReleasePipeline("19.1.5", 3),
				new GitLabReleases.ReleasePipeline("19.1.4", 4),
				new GitLabReleases.ReleasePipeline("19.1.3", 5),
				new GitLabReleases.ReleasePipeline("19.1.2", 6),
				new GitLabReleases.ReleasePipeline("19.1.1", 7)
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.7");

		commands.execute(printStream, "update app --list-releases");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "                   ",
				ANSI_BG_GREEN + "19.1.7        -- current & latest" + ANSI_RESET,
				"19.1.6                           ",
				ANSI_BG_GRAY + "19.1.5                           " + ANSI_RESET,
				"19.1.4                           ",
				ANSI_BG_GRAY + "19.1.3                           " + ANSI_RESET,
				""
		);
	}

	@Test
	void highlights_current_release_if_current_release_is_not_one_of_newest_5_releases() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(Arrays.asList(
				new GitLabReleases.ReleasePipeline("19.1.7", 7),
				new GitLabReleases.ReleasePipeline("19.1.6", 6),
				new GitLabReleases.ReleasePipeline("19.1.5", 5),
				new GitLabReleases.ReleasePipeline("19.1.4", 4),
				new GitLabReleases.ReleasePipeline("19.1.3", 3),
				new GitLabReleases.ReleasePipeline("19.1.2", 2),
				new GitLabReleases.ReleasePipeline("19.1.1", 1)
		));
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.1");

		commands.execute(printStream, "update app --list-releases");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "          ",
				ANSI_BG_GRAY + "19.1.7        -- latest " + ANSI_RESET,
				"19.1.6                  ",
				ANSI_BG_GRAY + "19.1.5                  " + ANSI_RESET,
				"19.1.4                  ",
				ANSI_BG_GRAY + "19.1.3                  " + ANSI_RESET,
				ANSI_BG_GREEN + "19.1.1        -- current" + ANSI_RESET,
				""
		);
	}

	@Test
	void ignores_exception_for_getting_version() throws IOException {
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);

		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, false)).thenReturn(Arrays.asList(
				new GitLabReleases.ReleasePipeline("19.1.7", 7),
				new GitLabReleases.ReleasePipeline("19.1.6", 6),
				new GitLabReleases.ReleasePipeline("19.1.5", 5),
				new GitLabReleases.ReleasePipeline("19.1.4", 4),
				new GitLabReleases.ReleasePipeline("19.1.3", 3),
				new GitLabReleases.ReleasePipeline("19.1.2", 2),
				new GitLabReleases.ReleasePipeline("19.1.1", 1)
		));

		commands.execute(printStream, "update app --list-releases");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Releases found on GitLab",
				"",
				u + "Release Name" + r + "  " + u + r + "         ",
				ANSI_BG_GRAY + "19.1.7        -- latest" + ANSI_RESET,
				"19.1.6                 ",
				ANSI_BG_GRAY + "19.1.5                 " + ANSI_RESET,
				"19.1.4                 ",
				ANSI_BG_GRAY + "19.1.3                 " + ANSI_RESET,
				""
		);
	}

	@Test
	void list_snapshot_releases() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, true)).thenReturn(
				Arrays.asList(
						new GitLabReleases.ReleasePipeline("19.1.7", 7),
						new GitLabReleases.ReleasePipeline("19.1.6", 6),
						new GitLabReleases.ReleasePipeline("19.1.5", 5),
						new GitLabReleases.ReleasePipeline("19.1.4", 4),
						new GitLabReleases.ReleasePipeline("19.1.3", 3),
						new GitLabReleases.ReleasePipeline("19.1.2", 2),
						new GitLabReleases.ReleasePipeline("19.1.1", 1)
				)
		);

		commands.execute(printStream, "update app --list-snapshots");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Snapshots found on GitLab",
				"",
				u + "Release Name" + r,
				ANSI_BG_GRAY + "19.1.7      " + ANSI_RESET,
				"19.1.6      ",
				ANSI_BG_GRAY + "19.1.5      " + ANSI_RESET,
				"19.1.4      ",
				ANSI_BG_GRAY + "19.1.3      " + ANSI_RESET,
				"19.1.2      ",
				ANSI_BG_GRAY + "19.1.1      " + ANSI_RESET,
				""
		);
	}

	@Test
	void list_snapshot_releases_with_proxy() throws IOException {
		Mockito.when(gitLabReleases.getReleases(proxy, true)).thenReturn(Arrays.asList(
				new GitLabReleases.ReleasePipeline("19.1.7", 7),
				new GitLabReleases.ReleasePipeline("19.1.6", 6),
				new GitLabReleases.ReleasePipeline("19.1.5", 5),
				new GitLabReleases.ReleasePipeline("19.1.4", 4),
				new GitLabReleases.ReleasePipeline("19.1.3", 3),
				new GitLabReleases.ReleasePipeline("19.1.2", 2),
				new GitLabReleases.ReleasePipeline("19.1.1", 1)
		));

		commands.execute(printStream, "update app --list-snapshots --proxy-ip 10.90.0.50 --proxy-port 8080");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Snapshots found on GitLab",
				"",
				u + "Release Name" + r,
				ANSI_BG_GRAY + "19.1.7      " + ANSI_RESET,
				"19.1.6      ",
				ANSI_BG_GRAY + "19.1.5      " + ANSI_RESET,
				"19.1.4      ",
				ANSI_BG_GRAY + "19.1.3      " + ANSI_RESET,
				"19.1.2      ",
				ANSI_BG_GRAY + "19.1.1      " + ANSI_RESET,
				""
		);
	}

	@Test
	void update_to_snapshot_version() throws IOException {
		Mockito.when(gitLabReleases.getReleases(Proxy.NO_PROXY, true)).thenReturn(
				Collections.singletonList(
						new GitLabReleases.ReleasePipeline("test", 5)
				)
		);
		commands.execute(printStream, "update app --snapshot test");

		Mockito.verify(gitLabReleases).updateToRelease(new GitLabReleases.ReleasePipeline("test", 5), Proxy.NO_PROXY);
	}

	@Test
	void update_to_snapshot_version_with_proxy() throws IOException {
		Mockito.when(gitLabReleases.getReleases(proxy, true)).thenReturn(
				Collections.singletonList(
						new GitLabReleases.ReleasePipeline("test", 5)
				)
		);
		commands.execute(printStream, "update app --snapshot test --proxy-ip 10.90.0.50 --proxy-port 8080");

		Mockito.verify(gitLabReleases).updateToRelease(new GitLabReleases.ReleasePipeline("test", 5), proxy);
	}

	@Test
	void invalid_command() {
		commands.execute(printStream, "update");

		assertOutput(
				"Usage:  update [-h] [COMMAND]",
				"Update the application or remote repo.",
				"  -h, --help   Show this help message.",
				"Commands:",
				"  app   Update the application.",
				"  repo  Push/pull changes to/from remote repo."
		);
	}

	@Test
	void update_command_help() {
		commands.execute(printStream, "update --help");

		assertOutput(
				"Usage:  update [-h] [COMMAND]",
				"Update the application or remote repo.",
				"  -h, --help   Show this help message.",
				"Commands:",
				"  app   Update the application.",
				"  repo  Push/pull changes to/from remote repo."
		);
	}
}
