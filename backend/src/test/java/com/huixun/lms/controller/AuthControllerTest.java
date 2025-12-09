package com.huixun.lms.controller;

import com.huixun.lms.LmsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LmsApplication.class)
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void registerAndLogin() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content("{\"username\":\"u1\",\"password\":\"p\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content("{\"username\":\"u1\",\"password\":\"p\"}"))
                .andExpect(status().isOk());
    }
}
