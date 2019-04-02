package com.bank.service;

import com.bank.Application;
import com.bank.bo.Transfer;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.bank.bo.Transfer.Status.FAIL;
import static com.bank.bo.Transfer.Status.SUCCESS;
import static org.junit.Assert.assertEquals;

/**
 * @author Zaycev Denis
 */
public class TransferServiceTest {

    private static final String ACCOUNT_1 = "123456789";
    private static final String ACCOUNT_2 = "987654321";

    private TransferService transferService;
    private AccountService accountService;

    @Before
    public void setUp() {
        Application application = new Application();
        application.init();

        transferService = application.getTransferService();
        accountService = application.getAccountService();
    }

    @Test
    public void balancesMustBeCorrectlyChanged() {
        // given
        Money firstInitialBalance = getFirstAccountBalance();
        Money secondInitialBalance = getSecondAccountBalance();

        // when
        Transfer transfer = transferService.execute(newTransfer(Money.of(CurrencyUnit.GBP, 10)));

        // then
        assertEquals(SUCCESS, transfer.getStatus());

        assertEquals(new BigDecimal("10.00"), firstInitialBalance.minus(getFirstAccountBalance()).getAmount());
        assertEquals(new BigDecimal("10.00"), getSecondAccountBalance().minus(secondInitialBalance).getAmount());
    }

    @Test
    public void balancesMustNotBeChangedOnException() {
        // given
        Money firstInitialBalance = getFirstAccountBalance();
        Money secondInitialBalance = getSecondAccountBalance();

        // when
        Transfer transfer = transferService.execute(newTransfer(Money.of(CurrencyUnit.GBP, 99999)));

        // then
        assertEquals(FAIL, transfer.getStatus());

        assertEquals(firstInitialBalance, getFirstAccountBalance());
        assertEquals(secondInitialBalance, getSecondAccountBalance());
    }

    @Test
    public void concurrentTransfersMustBeExecutedSuccessfully() {
        // given
        Money firstInitialBalance = getFirstAccountBalance();
        Money secondInitialBalance = getSecondAccountBalance();

        Money transferAmount = Money.of(CurrencyUnit.GBP, 1);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // when
        List<Future> transfers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            transfers.add(executor.submit(() -> transferService.execute(newTransfer(transferAmount))));
            transfers.add(executor.submit(() -> transferService.execute(newBackwardTransfer(transferAmount))));
        }

        for (Future task: transfers) {
            try {
                task.get();
            } catch (Exception e) { /* continue waiting for others on any exception */ }
        }

        executor.shutdownNow();

        // then
        assertEquals(firstInitialBalance, getFirstAccountBalance());
        assertEquals(secondInitialBalance, getSecondAccountBalance());
    }

    private Money getFirstAccountBalance() {
        return accountService.getAccount(ACCOUNT_1).getBalance();
    }

    private Money getSecondAccountBalance() {
        return accountService.getAccount(ACCOUNT_2).getBalance();
    }

    private Transfer newTransfer(Money amount) {
        Transfer transfer = new Transfer();
        transfer.setRemitter(accountService.getAccount(ACCOUNT_1));
        transfer.setBeneficiary(accountService.getAccount(ACCOUNT_2));
        transfer.setAmount(amount);

        return transfer;
    }

    private Transfer newBackwardTransfer(Money amount) {
        Transfer transfer = new Transfer();
        transfer.setRemitter(accountService.getAccount(ACCOUNT_2));
        transfer.setBeneficiary(accountService.getAccount(ACCOUNT_1));
        transfer.setAmount(amount);

        return transfer;
    }

}
