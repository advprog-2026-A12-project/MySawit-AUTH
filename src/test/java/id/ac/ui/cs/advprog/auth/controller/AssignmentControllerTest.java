package id.ac.ui.cs.advprog.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.auth.config.SecurityConfig;
import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.AssignmentUserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorUnassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.ReassignmentUserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.service.AssignmentService;
import id.ac.ui.cs.advprog.auth.service.JwtService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssignmentController.class)
@Import(SecurityConfig.class)
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void assignBuruhToMandorReturns201ForAdmin() throws Exception {
        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(UUID.randomUUID())
                .mandorId(UUID.randomUUID())
                .build();

        BuruhMandorAssignmentResponseData response = BuruhMandorAssignmentResponseData.builder()
                .id(UUID.randomUUID())
                .buruh(AssignmentUserSummaryResponseData.builder()
                        .id(request.getBuruhId())
                        .name("Ahmad Buruh")
                        .email("ahmad@example.com")
                        .build())
                .mandor(AssignmentUserSummaryResponseData.builder()
                        .id(request.getMandorId())
                        .name("Budi Mandor")
                        .email("budi@example.com")
                        .build())
                .assignedAt(Instant.now())
                .build();

        when(assignmentService.assignBuruhToMandor(any(AssignBuruhMandorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/assignments/buruh-mandor")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Buruh assigned to Mandor successfully"))
                .andExpect(jsonPath("$.data.buruh.email").value("ahmad@example.com"))
                .andExpect(jsonPath("$.data.mandor.email").value("budi@example.com"));
    }

    @Test
    void assignBuruhToMandorReturns403ForNonAdmin() throws Exception {
        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(UUID.randomUUID())
                .mandorId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/assignments/buruh-mandor")
                        .with(user("buruh").roles("BURUH"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
    }

    @Test
    void assignBuruhToMandorReturns401WithoutAuth() throws Exception {
        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(UUID.randomUUID())
                .mandorId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/v1/assignments/buruh-mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void reassignBuruhToMandorReturns200ForAdmin() throws Exception {
        UUID buruhId = UUID.randomUUID();
        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder()
                .newMandorId(UUID.randomUUID())
                .build();

        BuruhMandorReassignmentResponseData response = BuruhMandorReassignmentResponseData.builder()
                .buruh(ReassignmentUserSummaryResponseData.builder()
                        .id(buruhId)
                        .name("Ahmad Buruh")
                        .build())
                .previousMandor(ReassignmentUserSummaryResponseData.builder()
                        .id(UUID.randomUUID())
                        .name("Budi Mandor")
                        .build())
                .newMandor(ReassignmentUserSummaryResponseData.builder()
                        .id(request.getNewMandorId())
                        .name("Dedi Mandor")
                        .build())
                .reassignedAt(Instant.now())
                .build();

        when(assignmentService.reassignBuruhToMandor(org.mockito.ArgumentMatchers.eq(buruhId), any(ReassignBuruhMandorRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/assignments/buruh-mandor/{buruhId}", buruhId)
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Buruh reassigned successfully"))
                .andExpect(jsonPath("$.data.buruh.name").value("Ahmad Buruh"))
                .andExpect(jsonPath("$.data.newMandor.name").value("Dedi Mandor"));
    }

    @Test
    void reassignBuruhToMandorReturns403ForNonAdmin() throws Exception {
        UUID buruhId = UUID.randomUUID();
        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder()
                .newMandorId(UUID.randomUUID())
                .build();

        mockMvc.perform(put("/api/v1/assignments/buruh-mandor/{buruhId}", buruhId)
                        .with(user("buruh").roles("BURUH"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
    }

    @Test
    void reassignBuruhToMandorReturns401WithoutAuth() throws Exception {
        UUID buruhId = UUID.randomUUID();
        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder()
                .newMandorId(UUID.randomUUID())
                .build();

        mockMvc.perform(put("/api/v1/assignments/buruh-mandor/{buruhId}", buruhId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void unassignBuruhFromMandorReturns200ForAdmin() throws Exception {
        UUID buruhId = UUID.randomUUID();

        BuruhMandorUnassignmentResponseData response = BuruhMandorUnassignmentResponseData.builder()
                .buruh(ReassignmentUserSummaryResponseData.builder()
                        .id(buruhId)
                        .name("Ahmad Buruh")
                        .build())
                .previousMandor(ReassignmentUserSummaryResponseData.builder()
                        .id(UUID.randomUUID())
                        .name("Budi Mandor")
                        .build())
                .unassignedAt(Instant.now())
                .build();

        when(assignmentService.unassignBuruhFromMandor(buruhId)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/assignments/buruh-mandor/{buruhId}", buruhId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Buruh unassigned from Mandor successfully"))
                .andExpect(jsonPath("$.data.buruh.name").value("Ahmad Buruh"));
    }

    @Test
    void unassignBuruhFromMandorReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/assignments/buruh-mandor/{buruhId}", UUID.randomUUID())
                        .with(user("buruh").roles("BURUH")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
    }

    @Test
    void unassignBuruhFromMandorReturns401WithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/assignments/buruh-mandor/{buruhId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }
}