package com.bank.controller;

import com.bank.bo.Transfer;
import com.bank.controller.to.TransferTO;
import com.bank.service.AccountService;
import com.bank.service.TransferService;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;

import static com.bank.bo.Transfer.Status.FAIL;

/**
 * @author Zaycev Denis
 */
public class TransferController implements Route {

    private AccountService accountService;
    private TransferService transferService;

    private Gson gson;

    public TransferController(Gson gson, AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;

        this.gson = gson;
    }

    @Override
    public Object handle(Request request, Response response) {
        TransferTO to = gson.fromJson(request.body(), TransferTO.class);
        validate(to);

        Transfer transfer = transferService.execute(toBO(to));

        response.status(FAIL.equals(transfer.getStatus()) ? 400 : 200);
        response.header("Content-Type", "application/json");

        return gson.toJson(toTO(transfer));
    }

    private void validate(TransferTO to) {
        if (to.beneficiaryAccount == null || to.remitterAccount == null || to.amount == null) {
            throw new IllegalArgumentException("Invalid transfer request");
        }

        if (to.amount.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private Transfer toBO(TransferTO to) {
        Transfer transfer = new Transfer();
        transfer.setRemitter(accountService.getAccount(to.remitterAccount));
        transfer.setBeneficiary(accountService.getAccount(to.beneficiaryAccount));
        transfer.setAmount(to.amount);

        return transfer;
    }

    private TransferTO toTO(Transfer transfer) {
        TransferTO to = new TransferTO();
        to.beneficiaryAccount = transfer.getBeneficiary().getNumber();
        to.remitterAccount = transfer.getRemitter().getNumber();
        to.amount = transfer.getAmount();
        to.status = transfer.getStatus();

        return to;
    }

}
