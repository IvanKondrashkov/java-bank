package ru.yandex.practicum.bank.transfer.controller;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import ru.yandex.practicum.bank.transfer.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.yandex.practicum.bank.transfer.mapper.TransferMapper;
import ru.yandex.practicum.bank.transfer.mapper.TransferMapperImpl;
import ru.yandex.practicum.bank.transfer.service.TransferService;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;
import ru.yandex.practicum.bank.transfer.exception.EntityNotFoundException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Import({TestSecurityConfig.class, TransferMapperImpl.class})
@WebMvcTest(TransferController.class)
class TransferControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TransferMapper transferMapper;
    @MockitoBean
    private TransferService transferService;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        transfer = Transfer.builder()
                .id(1L)
                .payerAccountNumber("ACC-001")
                .payeeAccountNumber("ACC-002")
                .amount(new BigDecimal("100.0"))
                .status(PaymentStatus.COMPLETED)
                .description("Transfer from ACC-001 to ACC-002")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        transfer = null;
    }

    @Test
    @WithMockUser(username = "testUser")
    void findById() throws Exception {
        when(transferService.findById(transfer.getId())).thenReturn(transferMapper.toTransferShortInfo(transfer));

        mockMvc.perform(get("/transfers/{id}", transfer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(transfer.getId()))
                .andExpect(jsonPath("$.data.payerAccountNumber").value(transfer.getPayerAccountNumber()))
                .andExpect(jsonPath("$.data.payeeAccountNumber").value(transfer.getPayeeAccountNumber()))
                .andExpect(jsonPath("$.data.amount").value(transfer.getAmount()))
                .andExpect(jsonPath("$.data.status").value(transfer.getStatus().name()));

        verify(transferService, times(1)).findById(transfer.getId());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findById_throwsEntityNotFoundException() throws Exception {
        when(transferService.findById(transfer.getId())).thenThrow(
                new EntityNotFoundException(String.format("Transfer not found with id: %s", transfer.getId()))
        );

        mockMvc.perform(get("/transfers/{id}", transfer.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.message").value(String.format("Transfer not found with id: %s", transfer.getId())));

        verify(transferService, times(1)).findById(transfer.getId());
    }

    @Test
    @WithAnonymousUser
    void findById_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/transfers/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findAllByAccountNumber() throws Exception {
        when(transferService.findAllByAccountNumber(eq(transfer.getPayerAccountNumber()), eq(TransferType.TRANSFER_IN))).thenReturn(List.of(transferMapper.toTransferShortInfo(transfer)));

        mockMvc.perform(get("/transfers/account/{accountNumber}", transfer.getPayerAccountNumber())
                        .param("type", TransferType.TRANSFER_IN.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(transfer.getId()))
                .andExpect(jsonPath("$.data[0].payerAccountNumber").value(transfer.getPayerAccountNumber()))
                .andExpect(jsonPath("$.data[0].payeeAccountNumber").value(transfer.getPayeeAccountNumber()))
                .andExpect(jsonPath("$.data[0].amount").value(transfer.getAmount()))
                .andExpect(jsonPath("$.data[0].status").value(transfer.getStatus().name()));

        verify(transferService, times(1)).findAllByAccountNumber(eq(transfer.getPayerAccountNumber()), eq(TransferType.TRANSFER_IN));
    }

    @Test
    @WithMockUser(username = "testUser")
    void findAllByAccountNumber_isEmptyList() throws Exception {
        when(transferService.findAllByAccountNumber(eq(transfer.getPayerAccountNumber()), eq(TransferType.TRANSFER_OUT))).thenReturn(List.of());

        mockMvc.perform(get("/transfers/account/{accountNumber}", transfer.getPayerAccountNumber())
                        .param("type", TransferType.TRANSFER_OUT.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(transferService, times(1)).findAllByAccountNumber(eq(transfer.getPayerAccountNumber()), eq(TransferType.TRANSFER_OUT));
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"ROLE_USER", "transfer.write"})
    void processTransfer() throws Exception {
        when(transferService.processTransfer(any(TransferRequest.class))).thenReturn(transferMapper.toTransferInfo(transfer));

        TransferRequest request = new TransferRequest(
                transfer.getPayerAccountNumber(),
                transfer.getPayeeAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription()
        );

        mockMvc.perform(post("/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(transfer.getId()))
                .andExpect(jsonPath("$.data.payerAccountNumber").value(transfer.getPayerAccountNumber()))
                .andExpect(jsonPath("$.data.payeeAccountNumber").value(transfer.getPayeeAccountNumber()))
                .andExpect(jsonPath("$.data.amount").value(transfer.getAmount()))
                .andExpect(jsonPath("$.data.status").value(transfer.getStatus().name()));

        verify(transferService, times(1)).processTransfer(any(TransferRequest.class));
    }

    @Test
    @WithAnonymousUser
    void processTransfer_throwsAccessDeniedException() throws Exception {
        TransferRequest request = new TransferRequest(
                transfer.getPayerAccountNumber(),
                transfer.getPayeeAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription()
        );

        mockMvc.perform(post("/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}