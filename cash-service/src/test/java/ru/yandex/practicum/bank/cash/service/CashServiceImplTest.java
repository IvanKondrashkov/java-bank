package ru.yandex.practicum.bank.cash.service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.cash.client.AccountServiceClient;
import ru.yandex.practicum.bank.cash.mapper.CashOperationMapper;
import ru.yandex.practicum.bank.cash.mapper.CashOperationMapperImpl;
import ru.yandex.practicum.bank.cash.repository.CashOperationRepository;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationShortInfo;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.bank.cash.exception.EntityNotFoundException;
import ru.yandex.practicum.bank.cash.exception.InvalidCashOperationException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CashServiceImplTest {
    @Mock
    private CashOperationRepository cashOperationRepository;
    @Mock
    private AccountServiceClient accountServiceClient;
    @Mock
    private NotificationService notificationService;
    private final CashOperationMapper cashOperationMapper = new CashOperationMapperImpl();
    private CashService cashService;
    private CashOperation operation;

    @BeforeEach
    void setUp() {
        operation = CashOperation.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .amount(new BigDecimal("100.0"))
                .type(CashOperationType.DEPOSIT)
                .status(PaymentStatus.COMPLETED)
                .description("Deposit to ACC-001")
                .createdAt(LocalDateTime.now())
                .build();
        cashService = new CashServiceImpl(
                cashOperationRepository,
                cashOperationMapper,
                accountServiceClient,
                notificationService
        );
    }

    @AfterEach
    void tearDown() {
        operation = null;
        cashService = null;
    }

    @Test
    void findById() {
        when(cashOperationRepository.findById(operation.getId())).thenReturn(Optional.of(operation));

        CashOperationShortInfo operationDb = cashService.findById(operation.getId());

        assertThat(operationDb).isNotNull();
        assertThat(operationDb.getId()).isEqualTo(operation.getId());
        assertThat(operationDb.getAccountNumber()).isEqualTo(operation.getAccountNumber());
        assertThat(operationDb.getAmount()).isEqualByComparingTo(operation.getAmount());

        verify(cashOperationRepository, times(1)).findById(operation.getId());
    }

    @Test
    void findById_throwsEntityNotFoundException() {
        when(cashOperationRepository.findById(operation.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cashService.findById(operation.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Cash operation not found with id: %s", operation.getId()));

        verify(cashOperationRepository, times(1)).findById(operation.getId());
    }

    @Test
    void findAllByAccountNumber() {
        when(cashOperationRepository.findByAccountNumber(operation.getAccountNumber())).thenReturn(List.of(operation));

        List<CashOperationShortInfo> operations = cashService.findAllByAccountNumber(operation.getAccountNumber());

        assertThat(operations).hasSize(1);
        verify(cashOperationRepository, times(1)).findByAccountNumber(operation.getAccountNumber());
    }

    @Test
    void findAllByAccountNumber_isEmptyList() {
        when(cashOperationRepository.findByAccountNumber(operation.getAccountNumber())).thenReturn(List.of());

        List<CashOperationShortInfo> operations = cashService.findAllByAccountNumber(operation.getAccountNumber());

        assertThat(operations).isEmpty();
        verify(cashOperationRepository, times(1)).findByAccountNumber(operation.getAccountNumber());
    }

    @Test
    void processOperation() {
        when(cashOperationRepository.save(any(CashOperation.class))).thenReturn(operation);
        when(accountServiceClient.processBalance(eq(operation.getAccountNumber()), any(UpdateBalanceRequest.class)))
                .thenReturn(BankResponse.success(AccountInfo.builder()
                        .accountNumber(operation.getAccountNumber())
                        .balance(new BigDecimal("600.5"))
                        .build()
                ));

        CashOperationRequest request = new CashOperationRequest(
                operation.getAccountNumber(),
                operation.getAmount(),
                operation.getType().name(),
                operation.getDescription()
        );

        CashOperationInfo operationDb = cashService.processOperation(request);

        assertThat(operationDb).isNotNull();
        assertThat(operationDb.getAccountNumber()).isEqualTo(operation.getAccountNumber());
        assertThat(operationDb.getAmount()).isEqualByComparingTo(operation.getAmount());
        assertThat(operationDb.getNewBalance()).isEqualByComparingTo(new BigDecimal("600.5"));
        assertThat(operationDb.getStatus()).isEqualTo(PaymentStatus.COMPLETED.name());

        verify(cashOperationRepository, atLeast(2)).save(any(CashOperation.class));
        verify(accountServiceClient, times(1)).processBalance(eq(operation.getAccountNumber()), any(UpdateBalanceRequest.class));
        verify(notificationService, times(1)).sendNotification(any(), eq(CashOperationType.DEPOSIT), any());
    }

    @Test
    void processOperation_throwsInvalidCashOperationException() {
        when(cashOperationRepository.save(any(CashOperation.class))).thenReturn(operation);
        when(accountServiceClient.processBalance(eq(operation.getAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(BankResponse.error("Insufficient funds", null));

        CashOperationRequest request = new CashOperationRequest(
                operation.getAccountNumber(),
                operation.getAmount(),
                operation.getType().name(),
                operation.getDescription()
        );

        assertThatThrownBy(() -> cashService.processOperation(request))
                .isInstanceOf(InvalidCashOperationException.class)
                .hasMessageContaining("Cash operation failed");

        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void processOperation_whenAccountServiceThrows_throwsInvalidCashOperationException() {
        when(cashOperationRepository.save(any(CashOperation.class))).thenReturn(operation);
        when(accountServiceClient.processBalance(eq(operation.getAccountNumber()), any(UpdateBalanceRequest.class))).thenThrow(new RuntimeException("Connection refused"));

        CashOperationRequest request = new CashOperationRequest(
                operation.getAccountNumber(),
                operation.getAmount(),
                operation.getType().name(),
                operation.getDescription()
        );

        assertThatThrownBy(() -> cashService.processOperation(request))
                .isInstanceOf(InvalidCashOperationException.class)
                .hasMessageContaining("Cash operation failed");

        verify(notificationService, never()).sendNotification(any(), any(), any());
    }
}