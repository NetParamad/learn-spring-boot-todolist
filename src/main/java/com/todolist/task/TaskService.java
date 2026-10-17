package com.todolist.task;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

	private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final TaskRepository repository;

	TaskService(TaskRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public Task create(String title) {
		return repository.save(new Task(title, Instant.now()));
	}

	@Transactional(readOnly = true)
	public List<Task> findAll(String status) {
		return switch (status) {
			case "active" -> repository.findAllByCompleted(false, NEWEST_FIRST);
			case "completed" -> repository.findAllByCompleted(true, NEWEST_FIRST);
			default -> repository.findAll(NEWEST_FIRST);
		};
	}

	@Transactional(readOnly = true)
	public Task findById(Long id) {
		return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}

	@Transactional
	public Task update(Long id, String title, Boolean completed) {
		Task task = findById(id);
		if (title != null) {
			task.rename(title);
		}
		if (completed != null) {
			task.setCompleted(completed);
		}
		return task;
	}

	@Transactional
	public void delete(Long id) {
		repository.delete(findById(id));
	}

}
