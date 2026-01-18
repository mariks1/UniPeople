    package com.khasanshin.organizationservice.unit;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.khasanshin.organizationservice.controller.PositionController;
    import com.khasanshin.organizationservice.dto.CreatePositionDto;
    import com.khasanshin.organizationservice.dto.PositionDto;
    import com.khasanshin.organizationservice.dto.UpdatePositionDto;
    import com.khasanshin.organizationservice.application.PositionUseCase;
    import jakarta.persistence.EntityNotFoundException;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.data.domain.PageImpl;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.http.MediaType;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.test.web.servlet.request.RequestPostProcessor;

    import java.util.List;
    import java.util.Map;
    import java.util.UUID;

    import static org.mockito.ArgumentMatchers.*;
    import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @WebMvcTest(controllers = PositionController.class)
    @Import(com.khasanshin.organizationservice.exception.GlobalExceptionHandler.class)
    class PositionControllerTest {

        @Autowired MockMvc mvc;
        @Autowired ObjectMapper mapper;

        @MockitoBean PositionUseCase positionService;

        private static RequestPostProcessor asHr() {
            return SecurityMockMvcRequestPostProcessors
                    .jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_HR"))
                    .jwt(jwt -> jwt.claim("roles", List.of("HR")));
        }

        private static RequestPostProcessor asEmployee() {
            return SecurityMockMvcRequestPostProcessors
                    .jwt()
                    .authorities(new SimpleGrantedAuthority("EMPLOYEE"))
                    .jwt(jwt -> jwt.claim("roles", List.of("EMPLOYEE")));
        }

        private static RequestPostProcessor asOrgAdmin() {
            return SecurityMockMvcRequestPostProcessors
                    .jwt()
                    .authorities(new SimpleGrantedAuthority("ORG_ADMIN"))
                    .jwt(jwt -> jwt.claim("roles", List.of("ORG_ADMIN")));
        }

        @Test
        void list_200_withHeader_andQuery() throws Exception {
            var dto = PositionDto.builder().id(UUID.randomUUID()).name("Professor").build();
            var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 5), 1);

            when(positionService.findAll(eq("pro"), any())).thenReturn(page);

            mvc.perform(get("/api/v1/positions")
                            .param("q", "pro")
                            .param("page","0").param("size","5")
                            .with(asEmployee()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Total-Count", "1"))
                    .andExpect(jsonPath("$.content[0].name").value("Professor"));
        }

        @Test
        void get_200() throws Exception {
            var id = UUID.randomUUID();
            var dto = PositionDto.builder().id(id).name("Assistant").build();
            when(positionService.get(id)).thenReturn(dto);

            mvc.perform(get("/api/v1/positions/{id}", id)
                            .with(asOrgAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void get_404() throws Exception {
            when(positionService.get(any())).thenThrow(new EntityNotFoundException());
            mvc.perform(get("/api/v1/positions/{id}", UUID.randomUUID())
                            .with(asOrgAdmin()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void create_201() throws Exception {
            var body = Map.of("name", "Lecturer");
            var saved = PositionDto.builder().id(UUID.randomUUID()).name("Lecturer").build();
            when(positionService.create(any(CreatePositionDto.class))).thenReturn(saved);

            mvc.perform(post("/api/v1/positions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(body))
                            .with(asOrgAdmin()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Lecturer"));
        }

        @Test
        void create_400_validation() throws Exception {
            var bad = Map.of("name", "");
            mvc.perform(post("/api/v1/positions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(bad))
                            .with(asOrgAdmin()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void update_200() throws Exception {
            var id = UUID.randomUUID();
            var req = Map.of("name", "Senior Lecturer");
            var dto = PositionDto.builder().id(id).name("Senior Lecturer").build();
            when(positionService.update(eq(id), any(UpdatePositionDto.class))).thenReturn(dto);

            mvc.perform(put("/api/v1/positions/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(req))
                            .with(asOrgAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Senior Lecturer"));
        }

        @Test
        void delete_204() throws Exception {
            mvc.perform(delete("/api/v1/positions/{id}", UUID.randomUUID())
                            .with(asOrgAdmin()))
                    .andExpect(status().isNoContent());
        }

        @Test
        void head_exists_200_and_notExists_404() throws Exception {
            UUID existing = UUID.randomUUID();
            UUID missing  = UUID.randomUUID();

            when(positionService.exists(existing)).thenReturn(true);
            when(positionService.exists(missing)).thenReturn(false);

            mvc.perform(head("/api/v1/positions/{id}", existing)
                            .with(asEmployee()))
                    .andExpect(status().isOk());

            mvc.perform(head("/api/v1/positions/{id}", missing)
                            .with(asEmployee()))
                    .andExpect(status().isNotFound());
        }
    }
