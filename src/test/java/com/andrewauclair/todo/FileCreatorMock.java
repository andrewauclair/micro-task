package com.andrewauclair.todo;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

public class FileCreatorMock extends FileCreator {
	@Override
	public OutputStream createOutputStream(String fileName) throws FileNotFoundException {
		return new ByteArrayOutputStream();
	}
}
