// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.StatusConsole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Status_Test extends CommandsBaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-c", "--command"})
	void status_command_sends_a_command_to_the_status_console() {
		commands.execute(printStream, "status -c \"list --tasks\"");

		Mockito.verify(osInterface).sendStatusMessage(StatusConsole.TransferType.Command, "list --tasks");
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

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void status_command_help(String parameter) {
		commands.execute(printStream, "status " + parameter);

		assertOutput(
				"Usage:  status [-h] -c=<command>",
				"  -c, --command=<command>",
				"  -h, --help                Show this help message."
		);
	}
}
