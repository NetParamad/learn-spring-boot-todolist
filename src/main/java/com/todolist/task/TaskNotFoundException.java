package com.todolist.task;

class TaskNotFoundException extends RuntimeException {

	TaskNotFoundException(Long id) {
		super("Task " + id + " not found");
	}

}
