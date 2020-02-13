package com.andrewauclair.todo.jline;

import org.jline.builtins.Completers;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class CustomPicocliCommands extends PicocliCommands {
	private final Supplier<Path> workDir;
	private final CommandLine cmd;
	private final List<String> commands;
	private final Map<String, String> aliasCommand = new HashMap<>();

	public CustomPicocliCommands(Path workDir, CommandLine cmd) {
		this(() -> workDir, cmd);
	}

	public CustomPicocliCommands(Supplier<Path> workDir, CommandLine cmd) {
		super(workDir, cmd);
		this.workDir = workDir;
		this.cmd = cmd;
		commands = new ArrayList<>(cmd.getCommandSpec().subcommands().keySet());
		for (String c : commands) {
			for (String a : cmd.getSubcommands().get(c).getCommandSpec().aliases()) {
				aliasCommand.put(a, c);
			}
		}
	}

	/**
	 * @return SystemCompleter for command completion
	 */
	@Override
	public Completers.SystemCompleter compileCompleters() {
		Completers.SystemCompleter out = new Completers.SystemCompleter();
		out.addAliases(aliasCommand);
		for (String s : commands) {
			CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(s).getCommandSpec();
			List<String> options = new ArrayList<>();
			Map<String, List<String>> optionValues = new HashMap<>();
			for (CommandLine.Model.OptionSpec o : spec.options()) {
				List<String> values = new ArrayList<>();
				if (o.completionCandidates() != null) {
					o.completionCandidates().forEach(v -> values.add(v));
				}
				if (o.arity().max() == 0) {
					options.addAll(Arrays.asList(o.names()));
				}
				else {
					for (String n : o.names()) {
						optionValues.put(n, values);
					}
				}
			}



			// TODO positional parameter completion
			// JLine OptionCompleter need to be improved with option descriptions and option value completion,
			// now it completes only strings.
			if (options.isEmpty() && optionValues.isEmpty()) {
				out.add(s, new ArgumentCompleter(new StringsCompleter(s), NullCompleter.INSTANCE));
			}
			else {
				out.add(s, new ArgumentCompleter(new StringsCompleter(s)
						, new Completers.OptionCompleter(NullCompleter.INSTANCE, optionValues, options, 1)));
			}
		}
		return out;
	}
}
