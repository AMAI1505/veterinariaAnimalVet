package com.veterinaria.veterinariaAnimalVet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = VeterinariaApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:animalvet-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.flyway.enabled=true",
                "spring.jpa.hibernate.ddl-auto=validate",
                "ANIMALVET_JWT_SECRET=integration-test-secret-key-at-least-32-bytes-long"
        })
@AutoConfigureMockMvc
class VeterinariaApplicationTests {

	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private jakarta.validation.Validator validator;
	
	@Test
	void registroPublicoNoEstaDisponible() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/registro").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void endpointsPrivadosRechazanUsuariosAnonimosYRolesSinPermiso() throws Exception {
        mockMvc.perform(get("/api/mascotas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/doctores")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("tutor").roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/mascotas")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("groomi").roles("PERSONAL_VETERINARIA")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/citas")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("tutor").roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api")).andExpect(status().isOk());
    }

    @Test
    void loginEntregaTokenBearerYLogoutLoRevoca() throws Exception {
        String body = "{\"username\":\"administrador\",\"password\":\"admin123\"}";
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var matcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(response);
        org.junit.jupiter.api.Assertions.assertTrue(matcher.find(), "El login debe devolver un token de acceso.");
        String token = matcher.group(1);
        mockMvc.perform(get("/api/admin/doctores").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/admin/doctores").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginYRegistroRechazanCamposInvalidos() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"no-es-correo\",\"dni\":\"123\",\"password\":\"corta\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void formulariosDelBackendRechazanValoresInvalidosEnSusCampos() {
        var cita = new com.veterinaria.veterinariaAnimalVet.controller.CitaApiController.CitaRequest(null, null, null, "Higiene libre");
        var mascota = new com.veterinaria.veterinariaAnimalVet.controller.MascotaApiController.MascotaRequest(
                "A", "X", "OTRO", null, "R", "999999999", "C", "1", "A", "123", "correo-invalido");
        var turno = new com.veterinaria.veterinariaAnimalVet.controller.AdminApiController.TurnoRequest(
                "FUNDAY", true, null, null, null, 500);
        var personal = new com.veterinaria.veterinariaAnimalVet.controller.AdminApiController.DoctorRequest(
                "A", "correo", "debil", "telefono", "OTRO", "", List.of(turno));
        var atencion = new com.veterinaria.veterinariaAnimalVet.controller.AtencionApiController.AtencionRequest(
                new BigDecimal("0"), "x".repeat(4001), " ", " ", null, null);
        var registro = new com.veterinaria.veterinariaAnimalVet.controller.RegistroClienteApiController.RegistroRequest(
                "correo", "123", "debil");
        for (Object request : List.of(cita, mascota, personal, atencion, registro)) {
            org.junit.jupiter.api.Assertions.assertFalse(validator.validate(request).isEmpty(),
                    "El formulario debe rechazar sus campos inválidos: " + request.getClass().getSimpleName());
        }
    }

}
