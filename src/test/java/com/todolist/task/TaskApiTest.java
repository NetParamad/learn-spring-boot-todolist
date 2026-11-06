package com.todolist.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskApiTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TaskRepository taskRepository;

	@BeforeEach
	void setUp() {
		taskRepository.deleteAll();
	}

	@Test
	void createsATask() throws Exception {
		mockMvc.perform(post("/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Buy milk"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern(".*/tasks/\\d+")))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("Buy milk"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void rejectsBlankTitle() throws Exception {
		mockMvc.perform(post("/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "   "}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void listsNoTasksInitially() throws Exception {
		mockMvc.perform(get("/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void listsTasksNewestFirst() throws Exception {
		createTask("first");
		createTask("second");

		mockMvc.perform(get("/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].title").value("second"))
				.andExpect(jsonPath("$[1].title").value("first"));
	}

	@Test
	void filtersActiveTasks() throws Exception {
		long active = createTask("active one");
		completeTask(createTask("done one"));

		mockMvc.perform(get("/tasks").param("status", "active"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(active))
				.andExpect(jsonPath("$[0].completed").value(false));
	}

	@Test
	void filtersCompletedTasks() throws Exception {
		createTask("active one");
		long done = createTask("done one");
		completeTask(done);

		mockMvc.perform(get("/tasks").param("status", "completed"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(done))
				.andExpect(jsonPath("$[0].completed").value(true));
	}

	@Test
	void getsATaskById() throws Exception {
		long id = createTask("Buy milk");

		mockMvc.perform(get("/tasks/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.title").value("Buy milk"));
	}

	@Test
	void returnsNotFoundForUnknownTask() throws Exception {
		mockMvc.perform(get("/tasks/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void updatesTitle() throws Exception {
		long id = createTask("Buy milk");

		mockMvc.perform(patch("/tasks/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title": "Buy oat milk"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Buy oat milk"))
				.andExpect(jsonPath("$.completed").value(false));
	}

	@Test
	void returnsNotFoundWhenUpdatingUnknownTask() throws Exception {
		mockMvc.perform(patch("/tasks/999999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"completed": true}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void completesATaskWithoutChangingTitle() throws Exception {
		long id = createTask("Buy milk");

		mockMvc.perform(patch("/tasks/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"completed": true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Buy milk"))
				.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	void completingTwiceIsIdempotent() throws Exception {
		long id = createTask("Buy milk");
		completeTask(id);

		mockMvc.perform(patch("/tasks/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"completed": true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true));
	}

	private void completeTask(long id) throws Exception {
		mockMvc.perform(patch("/tasks/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"completed": true}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void deletesATask() throws Exception {
		long id = createTask("Buy milk");

		mockMvc.perform(delete("/tasks/" + id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/tasks/" + id))
				.andExpect(status().isNotFound());
	}

	@Test
	void returnsNotFoundWhenDeletingUnknownTask() throws Exception {
		mockMvc.perform(delete("/tasks/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	private long createTask(String title) throws Exception {
		MvcResult result = mockMvc.perform(post("/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\": \"" + title + "\"}"))
				.andExpect(status().isCreated())
				.andReturn();
		String location = result.getResponse().getHeader("Location");
		return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
	}

}
