package com.todolist.task;

import jakarta.validation.constraints.NotBlank;

record CreateTaskRequest(@NotBlank String title) {
}
