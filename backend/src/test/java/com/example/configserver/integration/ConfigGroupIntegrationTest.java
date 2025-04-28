package com.example.configserver.integration;

import com.example.configserver.model.ConfigurationGroup;
import com.example.configserver.repository.ConfigGroupRepository;
import com.example.configserver.security.JwtAuthenticationFilter;
import com.example.configserver.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ConfigGroupIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ConfigGroupRepository groupRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ConfigurationGroup testGroup;

    @BeforeEach
    void setUp() {
        // Clean up the repository
        groupRepository.deleteAll();

        // Create a test group
        testGroup = new ConfigurationGroup();
        testGroup.setName("test-group");
        testGroup.setDescription("Test group for integration tests");
        testGroup = groupRepository.save(testGroup);

        // Mock JWT token validation
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("admin");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllGroups_ReturnsAllGroups() {
        webTestClient.get()
                .uri("/api/groups")
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ConfigurationGroup.class)
                .hasSize(1)
                .contains(testGroup);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupById_WhenGroupExists_ReturnsGroup() {
        webTestClient.get()
                .uri("/api/groups/{id}", testGroup.getId())
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigurationGroup.class)
                .isEqualTo(testGroup);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupById_WhenGroupDoesNotExist_ReturnsNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        webTestClient.get()
                .uri("/api/groups/{id}", nonExistentId)
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupByName_WhenGroupExists_ReturnsGroup() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/groups/byName")
                        .queryParam("name", testGroup.getName())
                        .build())
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigurationGroup.class)
                .isEqualTo(testGroup);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGroupByName_WhenGroupDoesNotExist_ReturnsNotFound() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/groups/byName")
                        .queryParam("name", "non-existent-group")
                        .build())
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGroup_WithValidData_CreatesAndReturnsGroup() {
        ConfigurationGroup newGroup = new ConfigurationGroup();
        newGroup.setName("new-group");
        newGroup.setDescription("Newly created group");

        webTestClient.post()
                .uri("/api/groups")
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newGroup))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigurationGroup.class)
                .value(group -> {
                    assert group.getId() != null;
                    assert group.getName().equals("new-group");
                    assert group.getDescription().equals("Newly created group");
                });

        // Verify the group was actually saved in the repository
        List<ConfigurationGroup> allGroups = groupRepository.findAll();
        assert allGroups.size() == 2;
        assert allGroups.stream().anyMatch(g -> g.getName().equals("new-group"));
    }

    @Test
    @WithMockUser(roles = "USER") // Not an admin
    void createGroup_WithInsufficientPermissions_ReturnsForbidden() {
        ConfigurationGroup newGroup = new ConfigurationGroup();
        newGroup.setName("unauthorized-group");
        newGroup.setDescription("Should not be created");

        webTestClient.post()
                .uri("/api/groups")
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newGroup))
                .exchange()
                .expectStatus().isForbidden();

        // Verify the group was not saved in the repository
        List<ConfigurationGroup> allGroups = groupRepository.findAll();
        assert allGroups.size() == 1;
        assert allGroups.stream().noneMatch(g -> g.getName().equals("unauthorized-group"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGroup_WithValidData_UpdatesAndReturnsGroup() {
        ConfigurationGroup updatedGroup = new ConfigurationGroup();
        updatedGroup.setName("updated-group");
        updatedGroup.setDescription("Updated description");

        webTestClient.put()
                .uri("/api/groups/{id}", testGroup.getId())
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedGroup))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ConfigurationGroup.class)
                .value(group -> {
                    assert group.getId().equals(testGroup.getId());
                    assert group.getName().equals("updated-group");
                    assert group.getDescription().equals("Updated description");
                });

        // Verify the group was actually updated in the repository
        ConfigurationGroup retrieved = groupRepository.findById(testGroup.getId()).orElse(null);
        assert retrieved != null;
        assert retrieved.getName().equals("updated-group");
        assert retrieved.getDescription().equals("Updated description");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGroup_WhenGroupDoesNotExist_ReturnsNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        ConfigurationGroup updatedGroup = new ConfigurationGroup();
        updatedGroup.setName("updated-non-existent");
        updatedGroup.setDescription("Should not update");

        webTestClient.put()
                .uri("/api/groups/{id}", nonExistentId)
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedGroup))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(roles = "USER") // Not an admin
    void updateGroup_WithInsufficientPermissions_ReturnsForbidden() {
        ConfigurationGroup updatedGroup = new ConfigurationGroup();
        updatedGroup.setName("unauthorized-update");
        updatedGroup.setDescription("Should not update");

        webTestClient.put()
                .uri("/api/groups/{id}", testGroup.getId())
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedGroup))
                .exchange()
                .expectStatus().isForbidden();

        // Verify the group was not updated in the repository
        ConfigurationGroup retrieved = groupRepository.findById(testGroup.getId()).orElse(null);
        assert retrieved != null;
        assert retrieved.getName().equals(testGroup.getName());
        assert retrieved.getDescription().equals(testGroup.getDescription());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGroup_WhenGroupExists_DeletesGroup() {
        webTestClient.delete()
                .uri("/api/groups/{id}", testGroup.getId())
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isNoContent();

        // Verify the group was actually deleted from the repository
        assert !groupRepository.existsById(testGroup.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGroup_WhenGroupDoesNotExist_ReturnsNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        webTestClient.delete()
                .uri("/api/groups/{id}", nonExistentId)
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(roles = "USER") // Not an admin
    void deleteGroup_WithInsufficientPermissions_ReturnsForbidden() {
        webTestClient.delete()
                .uri("/api/groups/{id}", testGroup.getId())
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().isForbidden();

        // Verify the group was not deleted from the repository
        assert groupRepository.existsById(testGroup.getId());
    }

    @Test
    void accessEndpoint_WithoutAuthentication_ReturnsUnauthorized() {
        webTestClient.get()
                .uri("/api/groups")
                .exchange()
                .expectStatus().isUnauthorized();
    }
} 