package com.todolist.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"todolist.seed-sample-data=true",
		"spring.datasource.url=jdbc:h2:mem:sample-data-test;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class SampleDataTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void seedsFiveSampleTasksOnStartup() throws Exception {
		mockMvc.perform(get("/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)));
	}

}
