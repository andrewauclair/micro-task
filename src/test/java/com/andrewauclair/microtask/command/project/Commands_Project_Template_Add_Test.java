// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Project_Template_Add_Test extends CommandsBaseTestCase {
	@Test
	void create_template() {
		// design/
		// design/lma

		// "One" false
		// "Two" true

		// design/lptd

		// "Three" true
		// "Four" false

		// implementation/
		// implementation/lma

		// "Five" false
		// "Six" true

		// implementation/lptd

		// "Seven" true
		// "Eight" false

		Mockito.when(osInterface.promptChoice(Mockito.anyString())).thenReturn(
				true, // [<y>/n] design/
				true, // add list? [<y>/n] design/lma
				true, // add task? [<y>/n] 'One'
				false, // recurring? [y/<n>]
				true, // add task? [<y>/n] 'Two'
				true, // recurring? [<y>/n]
				false, // add task? [y/<n>]
				true, // add list? [<y>/n] design/lptd
				true, // add task? [<y>/n] 'Three'
				true, // recurring? [<y>/n]
				true, // add task? [<y>/n] 'Four'
				false, // recurring? [y/<n>]
				false, // add task? [y/<n>]
				false, // add list? [y/<n>]
				true, // add group? [<y>/n] implementation/
				true, // add list? [<y>/n] implementation/lma
				true, // add task? [<y>/n] 'Five'
				false, // recurring? [y/<n>]
				true, // add task? [<y>/n] 'Six'
				true, // recurring? [<y>/n]
				false, // add task? [y/<n>]
				true, // add list? [<y>/n] implementation/lptd
				true, // add task? [<y>/n] 'Seven'
				true, // recurring? [<y>/n]
				true, // add task? [<y>/n] 'Eight'
				false, // recurring? [y/<n>]
				false, // add task? [y/<n>]
				false // add group? [y/<n>]
		);

		Mockito.when(osInterface.promptForString(Mockito.anyString(), Mockito.any())).thenReturn(
				"design/", // create design group
				"design/lma",
				"One",
				"Two",
				"design/lptd",
				"Three",
				"Four",
				"implementation/",
				"implementation/lma",
				"Five",
				"Six",
				"implementation/lptd",
				"Seven",
				"Eight"
		);

		commands.execute(printStream, "project template --create release");

		assertOutput(
				"Created project template 'release'",
				"",
				"design/",
				"design/lma",
				"'One'",
				"'Two' recurring",
				"design/lptd",
				"'Three' recurring",
				"'Four'",
				"implementation/",
				"implementation/lma",
				"'Five'",
				"'Six' recurring",
				"implementation/lptd",
				"'Seven' recurring",
				"'Eight'"
		);
	}
}
