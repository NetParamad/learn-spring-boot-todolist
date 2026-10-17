package com.todolist.task;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findAllByCompleted(boolean completed, Sort sort);

}
