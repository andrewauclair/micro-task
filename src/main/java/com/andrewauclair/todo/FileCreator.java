// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class FileCreator {
	OutputStream createOutputStream(String fileName) throws IOException {
		return new FileOutputStream(new File(fileName));
	}
}
