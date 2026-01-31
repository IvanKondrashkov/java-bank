package ru.yandex.practicum.bank.cash.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.bank.cash.service.CashService;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationShortInfo;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;

@Slf4j
@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {
    private final CashService cashService;

    @GetMapping("/operations/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<CashOperationShortInfo>> findById(@PathVariable Long id) {
        CashOperationShortInfo operation = cashService.findById(id);
        return ResponseEntity.ok(BankResponse.success(operation));
    }

    @GetMapping("/operations/account/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<List<CashOperationShortInfo>>> findAllByAccountNumber(@PathVariable String accountNumber) {
        List<CashOperationShortInfo> operations = cashService.findAllByAccountNumber(accountNumber);
        return ResponseEntity.ok(BankResponse.success(operations));
    }

    @PostMapping("/operations")
    @PreAuthorize("hasRole('USER') && hasAuthority('cash.write')")
    public ResponseEntity<BankResponse<CashOperationInfo>> processOperation(@Valid @RequestBody CashOperationRequest request) {
        CashOperationInfo operation = cashService.processOperation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BankResponse.success(operation));
    }
}