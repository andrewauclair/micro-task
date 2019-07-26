// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandParserTest {
	@Test
	void parse_basic_arguments() {
		CommandParser parser = new CommandParser(
				Arrays.asList(
						new CommandOption("arg1", CommandOption.NO_SHORTNAME),
						new CommandOption("arg2", CommandOption.NO_SHORTNAME),
						new CommandOption("g", CommandOption.NO_SHORTNAME)
				)
		);
		
		List<CommandArgument> args = parser.parse("test --arg1 --arg2 -g");
		
		assertThat(args).containsOnly(
				new CommandArgument("arg1"),
				new CommandArgument("arg2"),
				new CommandArgument("g")
		);
	}
	
	@Test
	void parse_strings_in_arguments() {
		CommandParser parser = new CommandParser(
				Arrays.asList(
						new CommandOption("arg1", CommandOption.NO_SHORTNAME),
						new CommandOption("arg2", CommandOption.NO_SHORTNAME, Collections.singletonList("String"))//, Collections.singletonList(new CommandArgument("String")))
				)
		);
		
		List<CommandArgument> args = parser.parse("test --arg1 --arg2 \"Test String Here\"");
		
		assertThat(args).containsOnly(
				new CommandArgument("arg1"),
				new CommandArgument("arg2", "Test String Here")
		);
	}
	
	@Test
	void provide_parser_with_expected_format() {
		// (option) (option) (option param)
		List<CommandOption> options = Arrays.asList(
				new CommandOption("arg1", CommandOption.NO_SHORTNAME, Collections.emptyList()),
				new CommandOption("great", 'g', Collections.singletonList("String"))
		);
		
		CommandParser parser = new CommandParser(options);
		
		List<CommandArgument> args = parser.parse("test --arg1 -g \"Test\"");
		
		assertThat(args).containsOnly(
				new CommandArgument("arg1"),
				new CommandArgument("great", "Test")
		);
	}
	
	@Test
	void parse_multiple_flag_values() {
		List<CommandOption> options = Arrays.asList(
				new CommandOption("alpha", 'a'),
				new CommandOption("bravo", 'b'),
				new CommandOption("charlie", 'c')
		);
		
		CommandParser parser = new CommandParser(options);
		
		List<CommandArgument> args = parser.parse("test -abc");
		
		assertThat(args).containsOnly(
				new CommandArgument("alpha"),
				new CommandArgument("bravo"),
				new CommandArgument("charlie")
		);
	}
	
	@Test
	void parser_throws_exception_if_option_is_unknown_full_name() {
		List<CommandOption> options = Arrays.asList(
				new CommandOption("alpha", 'a'),
				new CommandOption("bravo", 'b')
		);
		
		CommandParser parser = new CommandParser(options);
		
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> parser.parse("test --charlie"));
		
		assertEquals("Unknown option 'charlie'", runtimeException.getMessage());
	}
	
	@Test
	void parser_throws_exception_if_option_is_unknown_short_name() {
		List<CommandOption> options = Arrays.asList(
				new CommandOption("alpha", 'a'),
				new CommandOption("bravo", 'b')
		);
		
		CommandParser parser = new CommandParser(options);
		
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> parser.parse("test -abc"));
		
		assertEquals("Unknown option 'c'", runtimeException.getMessage());
	}
	
	@Test
	void command_option_equals() {
		EqualsVerifier.forClass(CommandOption.class).verify();
	}
	
	@Test
	void command_argument_equals() {
		EqualsVerifier.forClass(CommandArgument.class).verify();
	}
	
	@Test
	void command_option_toString() {
		assertEquals("CommandOption{name='test', shortName=t, arguments=[1, 2]}", new CommandOption("test", 't', Arrays.asList("1", "2")).toString());
	}
	
	@Test
	void command_argument_toString() {
		assertEquals("CommandArgument{name='name', value='value'}", new CommandArgument("name", "value").toString());
	}
}
