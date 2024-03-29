// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import org.jline.builtins.Options;
import org.jline.console.ArgDesc;
import org.jline.console.CmdDesc;
import org.jline.console.CommandRegistry;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.utils.AttributedString;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Compiles SystemCompleter for command completion and implements a method commandDescription() that provides command descriptions
 * for JLine TailTipWidgets to be displayed in terminal status bar.
 * SystemCompleter implements the JLine 3 {@link Completer} interface. SystemCompleter generates completion
 * candidates for the specified command line based on the {@link CommandLine.Model.CommandSpec} that this {@code PicocliCommands} was constructed with.
 *
 * @since 4.1.2
 */
public class CustomPicocliCommands implements CommandRegistry {
	private final Supplier<Path> workDir;
	private final CommandLine cmd;
	private final Set<String> commands;
	private final Map<String, String> aliasCommand = new HashMap<>();

	public CustomPicocliCommands(Path workDir, CommandLine cmd) {
		this(() -> workDir, cmd);
	}

	public CustomPicocliCommands(Supplier<Path> workDir, CommandLine cmd) {
		this.workDir = workDir;
		this.cmd = cmd;
		commands = cmd.getCommandSpec().subcommands().keySet();
		for (String c : commands) {
			for (String a : cmd.getSubcommands().get(c).getCommandSpec().aliases()) {
				aliasCommand.put(a, c);
			}
		}
	}

	/**
	 * @param command
	 * @return true if PicocliCommands contains command
	 */
	public boolean hasCommand(String command) {
		return commands.contains(command) || aliasCommand.containsKey(command);
	}


	public SystemCompleter compileCompleters() {
		SystemCompleter out = new SystemCompleter();
		List<String> all = new ArrayList<>();
		all.addAll(commands);
		all.addAll(aliasCommand.keySet());
		out.add(all, new PicocliCompleter());
		return out;
	}

	@Override
	public CmdDesc commandDescription(List<String> args) {
		return new CmdDesc(true);
	}

	private class PicocliCompleter implements Completer {

		public PicocliCompleter() {
		}

		@Override
		public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
			assert commandLine != null;
			assert candidates != null;
			String word = commandLine.word();
			List<String> words = commandLine.words();

			CommandLine sub = getSubcommand(commandLine, words);

			if (sub == null) {
				return;
			}

			// next positional parameter index
			int paramIndex = calculateNextPositionalParamIndex(sub, commandLine, findSubcommandStartIndex(sub, commandLine, words), words);

			if (word.startsWith("-")) {
				String buffer = word.substring(0, commandLine.wordCursor());
				int eq = buffer.indexOf('=');
				for (CommandLine.Model.OptionSpec option : sub.getCommandSpec().options()) {
					if (option.arity().max() == 0 && eq < 0) {
						addCandidates(candidates, Arrays.asList(option.names()));
					}
					else {
						if (eq > 0) {
							String opt = buffer.substring(0, eq);
							if (Arrays.asList(option.names()).contains(opt) && option.completionCandidates() != null) {
								addCandidates(candidates, option.completionCandidates(), buffer.substring(0, eq + 1), "", true);
							}
						}
						else {
							addCandidates(candidates, Arrays.asList(option.names()), "", "=", false);
						}
					}
				}
			}
			else {
				if (commandLine.wordIndex() - 1 >= 0 && words.get(commandLine.wordIndex() - 1).startsWith("-")) {
					for (CommandLine.Model.OptionSpec option : sub.getCommandSpec().options()) {
						if (option.completionCandidates() != null) {
							addCandidates(candidates, option.completionCandidates(), "", "", true);
						}
					}
				}
				else {
					// add subcommands as completion options
					addCandidates(candidates, sub.getSubcommands().keySet());

					// add aliases as completion options
					for (CommandLine s : sub.getSubcommands().values()) {
						addCandidates(candidates, Arrays.asList(s.getCommandSpec().aliases()));
					}

					// add options as completion options
					for (CommandLine.Model.OptionSpec option : sub.getCommandSpec().options()) {
						if (option.isPositional()) {
							if (option.completionCandidates() != null) {
								addCandidates(candidates, option.completionCandidates(), "", "", true);
							}
						}
						else {
							addCandidates(candidates, Arrays.asList(option.names()));

						}
					}

					for (final CommandLine.Model.PositionalParamSpec positionalParameter : sub.getCommandSpec().positionalParameters()) {
						if (positionalParameter.completionCandidates() != null) {
							addCandidates(candidates, positionalParameter.completionCandidates(), "", "", true);
						}
					}
				}
			}
		}

		private CommandLine getSubcommand(ParsedLine commandLine, List<String> words) {
			CommandLine sub = cmd;
			for (int i = 0; i < commandLine.wordIndex(); i++) {
				if (!words.get(i).startsWith("-")) {
					CommandLine newSub = findSubcommandLine(sub, words.get(i));
					// might be a positional parameter
					if (newSub == null) {
						break;
					}
					sub = newSub;
				}
			}
			return sub;
		}

		private int findSubcommandStartIndex(CommandLine cmd, ParsedLine commandLine, List<String> words) {
			for (int i = 0; i < commandLine.wordIndex(); i++) {
				if (words.get(i).equals(cmd.getCommandName())) {
					return i;
				}
			}
			return -1;
		}

		private int calculateNextPositionalParamIndex(CommandLine cmd, ParsedLine commandLine, int startIndex, List<String> words) {
			int index = 0;

			CommandLine.Model.CommandSpec spec = cmd.getCommandSpec();

			for (int i = startIndex + 1; i < commandLine.wordIndex(); i++) {
				for (final CommandLine.Model.OptionSpec option : spec.options()) {
					if (option.isPositional()) {

					}
					else {

					}
				}
			}
			return index;
		}

		private void addCandidates(List<Candidate> candidates, Iterable<String> cands) {
			addCandidates(candidates, cands, "", "", true);
		}

		private void addCandidates(List<Candidate> candidates, Iterable<String> cands, String preFix, String postFix, boolean complete) {
			for (String s : cands) {
				candidates.add(new Candidate(AttributedString.stripAnsi(preFix + s + postFix), s, null, null, null, null, complete));
			}
		}

		private CommandLine findSubcommandLine(CommandLine cmdline, String command) {
			for (CommandLine s : cmdline.getSubcommands().values()) {
				if (s.getCommandName().equals(command) || Arrays.asList(s.getCommandSpec().aliases()).contains(command)) {
					return s;
				}
			}
			return null;
		}
	}

	/**
	 * @param command
	 * @return command description for JLine TailTipWidgets to be displayed in terminal status bar.
	 */
	public CmdDesc commandDescription(String command) {
		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();
		CommandLine.Help cmdhelp = new picocli.CommandLine.Help(spec);
		List<AttributedString> main = new ArrayList<>();
		Map<String, List<AttributedString>> options = new HashMap<>();
		String synopsis = AttributedString.stripAnsi(spec.usageMessage().sectionMap().get("synopsis").render(cmdhelp).toString());
		main.add(Options.HelpException.highlightSyntax(synopsis.trim(), Options.HelpException.defaultStyle()));
		// using JLine help highlight because the statement below does not work well...
		//        main.add(new AttributedString(spec.usageMessage().sectionMap().get("synopsis").render(cmdhelp).toString()));
		for (CommandLine.Model.OptionSpec o : spec.options()) {
			String key = Arrays.stream(o.names()).collect(Collectors.joining(" "));
			List<AttributedString> val = new ArrayList<>();
			for (String d : o.description()) {
				val.add(new AttributedString(d));
			}
			if (o.arity().max() > 0) {
				key += "=" + o.paramLabel();
			}
			options.put(key, val);
		}
		return new CmdDesc(main, ArgDesc.doArgNames(Arrays.asList("")), options);
	}

	@Override
	public List<String> commandInfo(String command) {
		List<String> out = new ArrayList<>();
		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();
		CommandLine.Help cmdhelp = new picocli.CommandLine.Help(spec);
		String description = AttributedString.stripAnsi(spec.usageMessage().sectionMap().get("description").render(cmdhelp).toString());
		out.addAll(Arrays.asList(description.split("\\r?\\n")));
		return out;
	}

	@Override
	public Object invoke(CommandSession session, String command, Object... args) throws Exception {
		List<String> arguments = new ArrayList<>();
		arguments.add(command);

		for (final Object arg : args) {
			arguments.add(arg.toString());
		}
		cmd.execute(arguments.toArray(new String[0]));
		return null;
	}

	@Override
	public Set<String> commandNames() {
		return commands;
	}

	@Override
	public Map<String, String> commandAliases() {
		return aliasCommand;
	}
}

