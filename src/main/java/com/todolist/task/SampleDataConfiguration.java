package com.todolist.task;

import java.time.Instant;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SampleDataConfiguration {

	@Bean
	@ConditionalOnProperty(name = "todolist.seed-sample-data", havingValue = "true")
	ApplicationRunner seedSampleTasks(TaskRepository repository) {
		return args -> {
			if (repository.count() > 0) {
				return;
			}
			repository.save(new Task("Buy groceries", Instant.now()));
			repository.save(new Task("Walk the dog", Instant.now()));
			repository.save(completed("Read a chapter of a book"));
			repository.save(completed("Call mom"));
			repository.save(new Task("Write the report", Instant.now()));
		};
	}

	private static Task completed(String title) {
		Task task = new Task(title, Instant.now());
		task.setCompleted(true);
		return task;
	}

}
