// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

package com.andrewauclair.todo.git;

class GitControls {
	GitCommand addFile(String fileName) {
		return new GitCommand("git add " + fileName);
	}
}
