package ru.yandex.practicum.bank.transfer.service;

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
import ru.yandex.practicum.bank.transfer.client.AccountServiceClient;
import ru.yandex.practicum.bank.transfer.mapper.TransferMapper;
import ru.yandex.practicum.bank.transfer.mapper.TransferMapperImpl;
import ru.yandex.practicum.bank.transfer.repository.TransferRepository;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferShortInfo;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.bank.transfer.exception.EntityNotFoundException;
import ru.yandex.practicum.bank.transfer.exception.InvalidTransferException;
import ru.yandex.practicum.bank.metrics.BankMetrics;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AccountServiceClient accountServiceClient;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BankMetrics bankMetrics;
    private final TransferMapper transferMapper = new TransferMapperImpl();
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
        transferService = new TransferServiceImpl(
                transferRepository,
                transferMapper,
                accountServiceClient,
                notificationService,
                bankMetrics
        );
    }

    @AfterEach
    void tearDown() {
        transfer = null;
        transferService = null;
    }

    @Test
    void findById() {
        when(transferRepository.findById(transfer.getId())).thenReturn(Optional.of(transfer));

        TransferShortInfo transferDb = transferService.findById(transfer.getId());

        assertThat(transferDb).isNotNull();
        assertThat(transferDb.getId()).isEqualTo(transfer.getId());
        assertThat(transferDb.getPayerAccountNumber()).isEqualTo(transfer.getPayerAccountNumber());
        assertThat(transferDb.getPayeeAccountNumber()).isEqualTo(transfer.getPayeeAccountNumber());
        assertThat(transferDb.getAmount()).isEqualByComparingTo(transfer.getAmount());
        assertThat(transferDb.getStatus()).isEqualTo(transfer.getStatus().name());

        verify(transferRepository, times(1)).findById(transfer.getId());
    }

    @Test
    void findById_throwsEntityNotFoundException() {
        when(transferRepository.findById(transfer.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.findById(transfer.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Transfer not found with id: %s", transfer.getId()));

        verify(transferRepository, times(1)).findById(transfer.getId());
    }

    @Test
    void findAllByAccountNumber_transferIn() {
        when(transferRepository.findAllByPayeeAccountNumber(transfer.getPayeeAccountNumber())).thenReturn(List.of(transfer));

        List<TransferShortInfo> transfers = transferService.findAllByAccountNumber(transfer.getPayeeAccountNumber(), TransferType.TRANSFER_IN);

        assertThat(transfers).hasSize(1);
        assertThat(transfers.get(0).getPayeeAccountNumber()).isEqualTo(transfer.getPayeeAccountNumber());

        verify(transferRepository, times(1)).findAllByPayeeAccountNumber(transfer.getPayeeAccountNumber());
        verify(transferRepository, never()).findAllByPayerAccountNumber(any());
    }

    @Test
    void findAllByAccountNumber_transferOut() {
        when(transferRepository.findAllByPayerAccountNumber(transfer.getPayerAccountNumber())).thenReturn(List.of(transfer));

        List<TransferShortInfo> transfers = transferService.findAllByAccountNumber(transfer.getPayerAccountNumber(), TransferType.TRANSFER_OUT);

        assertThat(transfers).hasSize(1);
        assertThat(transfers.get(0).getPayerAccountNumber()).isEqualTo(transfer.getPayerAccountNumber());

        verify(transferRepository, times(1)).findAllByPayerAccountNumber(transfer.getPayerAccountNumber());
        verify(transferRepository, never()).findAllByPayeeAccountNumber(any());
    }

    @Test
    void findAllByAccountNumber_isEmptyList() {
        when(transferRepository.findAllByPayerAccountNumber(transfer.getPayerAccountNumber())).thenReturn(List.of());

        List<TransferShortInfo> transfers = transferService.findAllByAccountNumber(transfer.getPayerAccountNumber(), TransferType.TRANSFER_OUT);

        assertThat(transfers).isEmpty();

        verify(transferRepository, times(1)).findAllByPayerAccountNumber(transfer.getPayerAccountNumber());
    }

    @Test
    void processTransfer() {
        AccountInfo payerInfo = AccountInfo.builder().accountNumber(transfer.getPayerAccountNumber()).balance(new BigDecimal("400.00")).build();
        AccountInfo payeeInfo = AccountInfo.builder().accountNumber(transfer.getPayeeAccountNumber()).balance(new BigDecimal("600.50")).build();

        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountServiceClient.processBalance(eq(transfer.getPayerAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(BankResponse.success(payerInfo));
        when(accountServiceClient.processBalance(eq(transfer.getPayeeAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(BankResponse.success(payeeInfo));

        TransferRequest request = new TransferRequest(
                transfer.getPayerAccountNumber(),
                transfer.getPayeeAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription()
        );

        TransferInfo transferDb = transferService.processTransfer(request);

        assertThat(transferDb).isNotNull();
        assertThat(transferDb.getPayerAccountNumber()).isEqualTo(transfer.getPayerAccountNumber());
        assertThat(transferDb.getPayeeAccountNumber()).isEqualTo(transfer.getPayeeAccountNumber());
        assertThat(transferDb.getStatus()).isEqualTo(transfer.getStatus().name());

        verify(accountServiceClient, times(2)).processBalance(any(), any());
        verify(notificationService, times(2)).sendNotification(any(), any(), any());
    }

    @Test
    void processTransfer_throwsInvalidTransferException() {
        BankResponse<AccountInfo> payerInfo = BankResponse.error("Insufficient funds", null);

        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountServiceClient.processBalance(eq(transfer.getPayerAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(payerInfo);

        TransferRequest request = new TransferRequest(
                transfer.getPayerAccountNumber(),
                transfer.getPayeeAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription()
        );

        assertThatThrownBy(() -> transferService.processTransfer(request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("Transfer failed");

        verify(accountServiceClient, times(1)).processBalance(eq(transfer.getPayerAccountNumber()), any(UpdateBalanceRequest.class));
        verify(accountServiceClient, never()).processBalance(eq(transfer.getPayeeAccountNumber()), any());
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void processTransfer_whenDepositFails_throwsInvalidTransferException() {
        AccountInfo payerInfo = AccountInfo.builder().accountNumber(transfer.getPayerAccountNumber()).balance(BigDecimal.ZERO).build();
        BankResponse<AccountInfo> payeeInfo = BankResponse.error("Account not found", null);

        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountServiceClient.processBalance(eq(transfer.getPayerAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(BankResponse.success(payerInfo));
        when(accountServiceClient.processBalance(eq(transfer.getPayeeAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(payeeInfo);

        TransferRequest request = new TransferRequest(
                transfer.getPayerAccountNumber(),
                transfer.getPayeeAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription()
        );

        assertThatThrownBy(() -> transferService.processTransfer(request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("Transfer failed");

        verify(accountServiceClient, atLeast(2)).processBalance(any(), any());
    }

    @Test
    void processTransfer_whenAccountServiceThrows_throwsInvalidTransferException() {
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(accountServiceClient.processBalance(eq(transfer.getPayerAccountNumber()), any(UpdateBalanceRequest.class))).thenThrow(new RuntimeException("Connection refused"));

        TransferRequest request = new TransferRequest(
                transfer.getPayerAccountNumber(),
                transfer.getPayeeAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription()
        );

        assertThatThrownBy(() -> transferService.processTransfer(request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("Transfer failed");

        verify(notificationService, never()).sendNotification(any(), any(), any());
    }
}