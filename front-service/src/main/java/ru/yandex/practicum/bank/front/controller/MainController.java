package ru.yandex.practicum.bank.front.controller;

import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import ru.yandex.practicum.bank.front.service.AccountService;
import ru.yandex.practicum.bank.front.service.NotificationService;
import ru.yandex.practicum.bank.front.service.TransferService;
import ru.yandex.practicum.bank.front.service.CashOperationService;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.bank.front.exception.BackendApiException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
    private final AccountService accountService;
    private final CashOperationService cashOperationService;
    private final TransferService transferService;
    private final NotificationService notificationService;

    @GetMapping("/")
    @PreAuthorize("hasRole('USER')")
    public String main(Model model) {
        UserProfileInfo profile = accountService.findUserProfileByUsername();
        List<AccountInfo> accounts = accountService.findAllUserAccounts();
        Map<String, BigDecimal> totalBalance = accountService.calculateBalanceByCurrency();
        List<NotificationInfo> notifications = notificationService.findAllUserNotifications();

        model.addAttribute("profile", profile);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("notifications", notifications);
        return "main";
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String createOrUpdateUserProfile(
            @Valid @ModelAttribute CreateOrUpdateUserProfileRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка формы обновления профиля.");
            redirectAttributes.addFlashAttribute("profile", request);
            return "redirect:/";
        }

        try {
            UserProfileInfo profile = accountService.findUserProfileByUsername();
            if (profile == null) {
                accountService.createUserProfile(request);
                redirectAttributes.addFlashAttribute("success", "Профиль создан.");
            } else {
                accountService.updateUserProfile(request);
                redirectAttributes.addFlashAttribute("success", "Данные сохранены.");
            }
        } catch (BackendApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/cash/deposit")
    @PreAuthorize("hasRole('USER')")
    public String deposit(@RequestParam String accountNumber, @RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        try {
            cashOperationService.deposit(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", String.format("Счёт пополнен на %s.", amount));
        } catch (BackendApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/cash/withdraw")
    @PreAuthorize("hasRole('USER')")
    public String withdraw(@RequestParam String accountNumber, @RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        try {
            cashOperationService.withdraw(accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", String.format("Со счёта снято %s.", amount));
        } catch (BackendApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public String transfer(
            @RequestParam String payerAccountNumber,
            @RequestParam String payeeAccountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        try {
            transferService.transfer(payerAccountNumber, payeeAccountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Перевод выполнен.");
        } catch (BackendApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/accounts")
    @PreAuthorize("hasRole('USER')")
    public String createAccount(@RequestParam String currency, RedirectAttributes redirectAttributes) {
        try {
            UserProfileInfo profile = accountService.findUserProfileByUsername();
            if (profile == null) {
                redirectAttributes.addFlashAttribute("error", "Сначала заполните данные профиля.");
                return "redirect:/";
            }
            accountService.createAccount(profile.getUsername(), currency);
            redirectAttributes.addFlashAttribute("success", "Счёт создан.");
        } catch (BackendApiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }
}