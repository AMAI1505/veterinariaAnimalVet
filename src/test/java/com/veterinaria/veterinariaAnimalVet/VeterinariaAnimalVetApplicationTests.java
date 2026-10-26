package com.veterinaria.veterinariaAnimalVet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VeterinariaAnimalVetApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cargaElContextoDeLaAplicacion() {
    }

    @Test
    void registroPublicoNoEstaDisponible() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/registro").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
