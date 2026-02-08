package com.khanabook.pos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khanabook.pos.dto.request.AuthRequest;
import com.khanabook.pos.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IntegrationTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testFullAuthFlow() throws Exception {
    // 1. Register
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setUsername("testuser");
    registerRequest.setPassword("Test@1234");
    registerRequest.setEmail("test@example.com");
    registerRequest.setFullName("Test User");
    registerRequest.setPhoneNumber("+1234567890");

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // 2. Login
    AuthRequest authRequest = new AuthRequest();
    authRequest.setUsername("testuser");
    authRequest.setPassword("Test@1234");

    MvcResult result = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andReturn();

    String response = result.getResponse().getContentAsString();
    String token = objectMapper.readTree(response).get("token").asText();

    // 3. Access Protected Resource (Self-info or categories)
    mockMvc.perform(get("/api/categories")
        .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void testUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testInvalidLogin() throws Exception {
    AuthRequest authRequest = new AuthRequest();
    authRequest.setUsername("wronguser");
    authRequest.setPassword("WrongPass");

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isUnauthorized());
  }
}
