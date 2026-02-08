package ru.yandex.practicum.bank.transfer.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.bank.transfer.service.TransferService;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferShortInfo;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;

@Slf4j
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<TransferShortInfo>> findById(@PathVariable Long id) {
        TransferShortInfo transfer = transferService.findById(id);
        return ResponseEntity.ok(BankResponse.success(transfer));
    }

    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<List<TransferShortInfo>>> findAllByAccountNumber(@PathVariable String accountNumber, @RequestParam TransferType type) {
        List<TransferShortInfo> transfers = transferService.findAllByAccountNumber(accountNumber, type);
        return ResponseEntity.ok(BankResponse.success(transfers));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('transfer.write')")
    public ResponseEntity<BankResponse<TransferInfo>> processTransfer(@Valid @RequestBody TransferRequest request) {
        TransferInfo transfer = transferService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BankResponse.success(transfer));
    }
}