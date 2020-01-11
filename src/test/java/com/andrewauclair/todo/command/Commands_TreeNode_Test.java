// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class Commands_TreeNode_Test extends CommandsBaseTestCase {
	@Test
	void tree_nodes_list_is_not_null() {
		assertNotNull(commands.getAutoCompleteNodes());
	}
}
