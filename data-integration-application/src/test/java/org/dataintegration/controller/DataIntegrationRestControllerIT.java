package org.dataintegration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataintegration.jpa.repository.JpaProjectRepository;
import org.dataintegration.usecase.ProjectsUsecase;
import org.dataintegration.model.ProjectModel;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DataIntegrationRestControllerIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaProjectRepository jpaProjectRepository;

    @Autowired
    private ProjectsUsecase getProject;

    private UUID projectId;

    @Test
    @Order(10)
    void createNewProject() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/data-migration/project/create/anyProjectName")
                        .param("owner", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("anyProjectName")))
                .andExpect(jsonPath("$.owner", is(1)))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andReturn();

        final ProjectModel projectInformation =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProjectModel.class);

        projectId = projectInformation.getId();

        assertThat(jpaProjectRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(20)
    void addItemIntoNonExistingScope() throws Exception {
        final List<Map<String, String>> newItems = List.of(
                Map.of(
                        "column1", "value1",
                        "column2", "value2",
                        "column3", "value3"
                )
        );

        mockMvc.perform(put("/data-migration/items/import/" + projectId)
                        .param("dataProvider", "GBIF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItems)))
                .andExpect(status().isOk());

        assertThat(jpaProjectRepository.count()).isEqualTo(1);
    }
}
