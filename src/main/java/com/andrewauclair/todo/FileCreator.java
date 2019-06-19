// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.*;

class FileCreator {
	OutputStream createOutputStream(String fileName) throws IOException {
		return new FileOutputStream(new File(fileName));
	}
}
