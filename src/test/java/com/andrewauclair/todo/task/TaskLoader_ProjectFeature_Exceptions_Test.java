// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

class TaskLoader_ProjectFeature_Exceptions_Test extends TaskBaseTestCase {
	@Test
	void catch_write_list_info_io_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);
		
		tasks.setProject(tasks.getListByName("/default"), "Test", true);
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
	
	@Test
	void catch_write_group_info_io_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);
		
		tasks.addGroup("/test/");
		tasks.setProject(tasks.getGroup("/test/"), "Test", true);
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
}
