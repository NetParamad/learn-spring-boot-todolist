package com.todolist.task;

import java.time.Instant;

record TaskResponse(Long id, String title, boolean completed, Instant createdAt) {

	static TaskResponse from(Task task) {
		return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted(), task.getCreatedAt());
	}

}
