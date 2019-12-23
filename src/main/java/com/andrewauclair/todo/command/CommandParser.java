// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;

import java.util.*;

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

//					i = parseOption(result, s, i, option);
					if (commandOption.getArgumentCount() > 0) {
						String arg1 = s[i + 1];
						
						if (arg1.startsWith("\"")) {
							arg1 = arg1.substring(1, arg1.length() - 1);
						}
						result.args.put(commandOption.getName(), Collections.singletonList(new CommandArgument(commandOption.getName(), arg1)));
						i++;
					}
					else {
						result.args.put(commandOption.getName(), Collections.singletonList(new CommandArgument(commandOption.getName())));
					}
				}
			}
			else if (option.startsWith("--")) {
				i = parseOption(result, s, i, option.substring(2));
			}
			else {
				boolean found = false;
				
				// TODO This isn't the greatest setup, these are called positional arguments and we should be able to define their position
				// probably the first argument, if it doesn't use the name
				for (CommandOption commandOption : options.values()) {
					if (!commandOption.usesName() && !result.args.keySet().contains(commandOption.getName())) {
						result.args.put(commandOption.getName(), Collections.singletonList(new CommandArgument(commandOption.getName(), option)));
						found = true;
						break;
					}
				}
				
				if (!found) {
					throw new TaskException("Unknown value '" + option + "'.");
				}
			}
		}
		
		return result;
	}
	
	private int parseOption(CommandParseResult result, String[] s, int i, String option) {
		CommandOption commandOption = options.get(option);
		
		if (commandOption == null) {
			throw new TaskException("Unknown option '" + option + "'");
		}
		
		int argCount = commandOption.getArgumentCount();
		
		List<CommandArgument> arguments = new ArrayList<>();
		
		while (argCount > 0) {
			String arg1 = s[i + 1];
			
			if (arg1.startsWith("\"")) {
				arg1 = arg1.substring(1, arg1.length() - 1);
			}
			
			arguments.add(new CommandArgument(commandOption.getName(), arg1));
//			result.args.put(commandOption.getName(), new CommandArgument(commandOption.getName(), arg1));
			i++;
			
			argCount--;
		}
		
		if (commandOption.getArgumentCount() == 0) {
			result.args.put(commandOption.getName(), Collections.singletonList(new CommandArgument(commandOption.getName())));
		}
		else {
			result.args.put(commandOption.getName(), arguments);
		}
		
		return i;
	}
	
	static class CommandParseResult {
		private final Map<String, List<CommandArgument>> args = new HashMap<>();
		
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
			return args.get(name).get(0).getValue();
		}
		
		long getLongArgument(String name, int position) {
			return Long.parseLong(getStrArgument(name, position));
		}
		
		int getIntArgument(String name) {
			return Integer.parseInt(getStrArgument(name));
		}
		
		long getLongArgument(String name) {
			return Long.parseLong(getStrArgument(name));
		}
		
		String getStrArgument(String name, int position) {
			return args.get(name).get(position).getValue();
		}
	}
}
