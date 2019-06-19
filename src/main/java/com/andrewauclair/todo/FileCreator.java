// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

class FileCreator {
	OutputStream createOutputStream(String fileName) throws FileNotFoundException {
		return new FileOutputStream(new File(fileName));
	}
}
