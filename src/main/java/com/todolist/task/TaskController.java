package com.todolist.task;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService service;

	TaskController(TaskService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
		Task task = service.create(request.title());
		return ResponseEntity.created(URI.create("/tasks/" + task.getId())).body(TaskResponse.from(task));
	}

	@GetMapping
	List<TaskResponse> list(@RequestParam(defaultValue = "all") String status) {
		return service.findAll(status).stream().map(TaskResponse::from).toList();
	}

	@GetMapping("/{id}")
	TaskResponse get(@PathVariable Long id) {
		return TaskResponse.from(service.findById(id));
	}

	@PatchMapping("/{id}")
	TaskResponse update(@PathVariable Long id, @RequestBody UpdateTaskRequest request) {
		return TaskResponse.from(service.update(id, request.title(), request.completed()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable Long id) {
		service.delete(id);
	}

}
