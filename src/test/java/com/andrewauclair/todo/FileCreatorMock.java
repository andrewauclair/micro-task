package com.andrewauclair.todo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

class FileCreatorMock extends FileCreator {
	@Override
	public OutputStream createOutputStream(String fileName) {
		return new ByteArrayOutputStream();
	}
}
