// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.StatusConsole;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Status_Test extends CommandsBaseTestCase {
	@Test
	void status_command_sends_a_command_to_the_status_console() {
		commands.execute(printStream, "status --command \"tasks --all\"");

		Mockito.verify(osInterface).sendStatusMessage(StatusConsole.TransferType.COMMAND, "tasks --all");
	}

	@Test
	void status_command_checks_if_command_is_valid() {
		Mockito.reset(osInterface);

		commands.execute(printStream, "status -c \"times --unknown-option\"");

		Mockito.verifyNoInteractions(osInterface);

		assertThat(commands.getAliases()).isEmpty();

		assertOutput(
				"Unknown option: '--unknown-option'",
				"",
				"Command 'times --unknown-option' is invalid.",
				""
		);
	}

	@Test
	void status_command_help() {
		commands.execute(printStream, "status --help");

		assertOutput(
				"Usage:  status [-h] -c=<command>",
				"Send a command to the status console.",
				"  -c, --command=<command>   Command to send to the status console.",
				"  -h, --help                Show this help message."
		);
	}
}
