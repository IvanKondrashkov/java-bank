package ru.yandex.practicum.bank.cash.controller;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.cash.config.TestSecurityConfig;
import ru.yandex.practicum.bank.cash.mapper.CashOperationMapper;
import ru.yandex.practicum.bank.cash.mapper.CashOperationMapperImpl;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.bank.cash.service.CashService;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;
import ru.yandex.practicum.bank.cash.exception.EntityNotFoundException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Import({TestSecurityConfig.class, CashOperationMapperImpl.class})
@WebMvcTest(CashController.class)
class CashControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CashOperationMapper cashOperationMapper;
    @MockitoBean
    private CashService cashService;
    private CashOperation operation;

    @BeforeEach
    void setUp() {
        operation = CashOperation.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .type(CashOperationType.DEPOSIT)
                .amount(new BigDecimal("100.0"))
                .status(PaymentStatus.COMPLETED)
                .description("Deposit to ACC-001")
                .build();
    }

    @AfterEach
    void tearDown() {
        operation = null;
    }

    @Test
    @WithMockUser(username = "testUser")
    void findById() throws Exception {
        when(cashService.findById(operation.getId())).thenReturn(cashOperationMapper.toShortInfo(operation));

        mockMvc.perform(get("/cash/operations/{id}", operation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(operation.getId()))
                .andExpect(jsonPath("$.data.accountNumber").value(operation.getAccountNumber()))
                .andExpect(jsonPath("$.data.amount").value(operation.getAmount()))
                .andExpect(jsonPath("$.data.status").value(operation.getStatus().name()));

        verify(cashService, times(1)).findById(operation.getId());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findById_throwsEntityNotFoundException() throws Exception {
        when(cashService.findById(operation.getId())).thenThrow(
                new EntityNotFoundException(String.format("Cash operation not found with id: %s", operation.getId()))
        );

        mockMvc.perform(get("/cash/operations/{id}", operation.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.message").value(String.format("Cash operation not found with id: %s", operation.getId())));

        verify(cashService, times(1)).findById(operation.getId());
    }

    @Test
    @WithAnonymousUser
    void findById_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/cash/operations/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findAllByAccountNumber() throws Exception {
        when(cashService.findAllByAccountNumber(operation.getAccountNumber())).thenReturn(List.of(cashOperationMapper.toShortInfo(operation)));

        mockMvc.perform(get("/cash/operations/account/{accountNumber}", operation.getAccountNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].accountNumber").value(operation.getAccountNumber()))
                .andExpect(jsonPath("$.data[0].amount").value(operation.getAmount()))
                .andExpect(jsonPath("$.data[0].status").value(PaymentStatus.COMPLETED.name()));

        verify(cashService, times(1)).findAllByAccountNumber(operation.getAccountNumber());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findAllByAccountNumber_isEmptyList() throws Exception {
        when(cashService.findAllByAccountNumber(operation.getAccountNumber())).thenReturn(List.of());

        mockMvc.perform(get("/cash/operations/account/{accountNumber}", operation.getAccountNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(cashService, times(1)).findAllByAccountNumber(operation.getAccountNumber());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"ROLE_USER", "cash.write"})
    void processOperation() throws Exception {
        when(cashService.processOperation(any(CashOperationRequest.class)))
                .thenReturn(cashOperationMapper.toInfo(operation, UUID.randomUUID().toString(), new BigDecimal("200.0")));

        CashOperationRequest request = new CashOperationRequest(
                operation.getAccountNumber(),
                operation.getAmount(),
                operation.getType().name(),
                operation.getDescription()
        );

        mockMvc.perform(post("/cash/operations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(operation.getAccountNumber()))
                .andExpect(jsonPath("$.data.amount").value(operation.getAmount()))
                .andExpect(jsonPath("$.data.newBalance").value(200.0))
                .andExpect(jsonPath("$.data.status").value(PaymentStatus.COMPLETED.name()));

        verify(cashService, times(1)).processOperation(any(CashOperationRequest.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void processOperation_throwsAccessDeniedException() throws Exception {
        CashOperationRequest request = new CashOperationRequest(
                operation.getAccountNumber(),
                operation.getAmount(),
                operation.getType().name(),
                operation.getDescription()
        );

        mockMvc.perform(post("/cash/operations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}