// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
	@Test
	void parse_basic_arguments() {
		CommandParser parser = new CommandParser(
				Arrays.asList(
						new CommandOption("arg1", CommandOption.NO_SHORTNAME, true),
						new CommandOption("arg2", CommandOption.NO_SHORTNAME, true),
						new CommandOption("g", CommandOption.NO_SHORTNAME, true)
				)
		);
		
		CommandParser.CommandParseResult result = parser.parse("test --arg1 --arg2 -g");
		
		assertTrue(result.hasArgument("arg1"));
		assertTrue(result.hasArgument("arg2"));
		assertTrue(result.hasArgument("g"));
	}
	
	@Test
	void parse_strings_in_arguments() {
		CommandParser parser = new CommandParser(
				Arrays.asList(
						new CommandOption("arg1", CommandOption.NO_SHORTNAME, true),
						new CommandOption("arg2", CommandOption.NO_SHORTNAME, Collections.singletonList("String"))
				)
		);
		
		CommandParser.CommandParseResult result = parser.parse("test --arg1 --arg2 \"Test String Here\"");
		
		assertTrue(result.hasArgument("arg1"));
		assertTrue(result.hasArgument("arg2"));
		assertEquals("Test String Here", result.getStrArgument("arg2"));
	}
	
	@Test
	void provide_parser_with_expected_format() {
		// (option) (option) (option param)
		List<CommandOption> options = Arrays.asList(
				new CommandOption("arg1", CommandOption.NO_SHORTNAME, Collections.emptyList()),
				new CommandOption("great", 'g', Collections.singletonList("String"))
		);
		
		CommandParser parser = new CommandParser(options);
		
		CommandParser.CommandParseResult result = parser.parse("test --arg1 -g \"Test\"");
		
		assertTrue(result.hasArgument("arg1"));
		assertTrue(result.hasArgument("great"));
		assertEquals("Test", result.getStrArgument("great"));
	}
	
	@Test
	void parse_multiple_flag_values() {
		List<CommandOption> options = Arrays.asList(
				new CommandOption("alpha", 'a', true),
				new CommandOption("bravo", 'b', true),
				new CommandOption("charlie", 'c', true)
		);
		
		CommandParser parser = new CommandParser(options);
		
		CommandParser.CommandParseResult args = parser.parse("test -abc");
		
		assertTrue(args.hasArgument("alpha"));
		assertTrue(args.hasArgument("bravo"));
		assertTrue(args.hasArgument("charlie"));
	}
	
	@Test
	void parser_throws_exception_if_option_is_unknown_full_name() {
		List<CommandOption> options = Arrays.asList(
				new CommandOption("alpha", 'a', true),
				new CommandOption("bravo", 'b', true)
		);
		
		CommandParser parser = new CommandParser(options);
		
		TaskException taskException = assertThrows(TaskException.class, () -> parser.parse("test --charlie"));
		
		assertEquals("Unknown option 'charlie'", taskException.getMessage());
	}
	
	@Test
	void parser_throws_exception_if_option_is_unknown_short_name() {
		List<CommandOption> options = Arrays.asList(
				new CommandOption("alpha", 'a', true),
				new CommandOption("bravo", 'b', true)
		);
		
		CommandParser parser = new CommandParser(options);
		
		TaskException taskException = assertThrows(TaskException.class, () -> parser.parse("test -abc"));
		
		assertEquals("Unknown option 'c'", taskException.getMessage());
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
		assertEquals("CommandOption{name='test', shortName=t, arguments=[1, 2], usesName=true}", new CommandOption("test", 't', Arrays.asList("1", "2")).toString());
	}
	
	@Test
	void command_argument_toString() {
		assertEquals("CommandArgument{name='name', value='value'}", new CommandArgument("name", "value").toString());
	}
}
