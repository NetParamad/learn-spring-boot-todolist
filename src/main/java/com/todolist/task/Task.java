package com.todolist.task;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private boolean completed;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Task() {
	}

	Task(String title, Instant createdAt) {
		this.title = title;
		this.createdAt = createdAt;
		this.completed = false;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	void rename(String title) {
		this.title = title;
	}

	void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
