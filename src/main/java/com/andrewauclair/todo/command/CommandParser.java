// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandParser {
	private final Map<String, CommandOption> options = new HashMap<>();
	
	public CommandParser(List<CommandOption> options) {
		for (CommandOption option : options) {
			this.options.put(option.getName(), option);
			
			if (option.getShortName() != CommandOption.NO_SHORTNAME) {
				this.options.put(String.valueOf(option.getShortName()), option);
			}
		}
	}
	
	CommandParseResult parse(String args) {
		CommandParseResult result = new CommandParseResult();
		
		String[] s = args.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for (int i = 1; i < s.length; i++) {
			String option = s[i];
			
			if (option.startsWith("-") && !option.startsWith("--")) {
				option = option.substring(1);
				
				int[] vals = option.chars().toArray();
				
				for (int val : vals) {
					char c = (char) val;
					
					CommandOption commandOption = options.get(String.valueOf(c));
					
					if (commandOption == null) {
						throw new TaskException("Unknown option '" + c + "'");
					}
					
					if (commandOption.getArgumentCount() > 0) {
						String arg1 = s[i + 1];
						
						if (arg1.startsWith("\"")) {
							arg1 = arg1.substring(1, arg1.length() - 1);
						}
						result.args.put(commandOption.getName(), new CommandArgument(commandOption.getName(), arg1));
						i++;
					}
					else {
						result.args.put(commandOption.getName(), new CommandArgument(commandOption.getName()));
					}
				}
			}
			else if (option.startsWith("--")) {
				i = parseOption(result, s, i, option.substring(2));
			}
		}
		
		return result;
	}
	
	private int parseOption(CommandParseResult result, String[] s, int i, String option) {
		CommandOption commandOption = options.get(option);
		
		if (commandOption == null) {
			throw new TaskException("Unknown option '" + option + "'");
		}
		
		if (commandOption.getArgumentCount() > 0) {
			String arg1 = s[i + 1];
			
			if (arg1.startsWith("\"")) {
				arg1 = arg1.substring(1, arg1.length() - 1);
			}
			result.args.put(commandOption.getName(), new CommandArgument(commandOption.getName(), arg1));
			i++;
		}
		else {
			result.args.put(commandOption.getName(), new CommandArgument(commandOption.getName()));
		}
		return i;
	}
	
	static class CommandParseResult {
		private final Map<String, CommandArgument> args = new HashMap<>();
		
		int getArgCount() {
			return args.keySet().size();
		}
		
		boolean hasArgument(String name) {
			return args.containsKey(name);
		}
		
		boolean getBoolArgument(String name) {
			return Boolean.parseBoolean(getStrArgument(name));
		}
		
		String getStrArgument(String name) {
			return args.get(name).getValue();
		}
		
		int getIntArgument(String name) {
			return Integer.parseInt(getStrArgument(name));
		}
		
		long getLongArgument(String name) {
			return Long.parseLong(getStrArgument(name));
		}
	}
}
