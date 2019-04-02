package com.bank.service;

import com.bank.bo.Account;
import com.bank.bo.Transfer;
import org.joda.money.Money;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

import static com.bank.bo.Transfer.Status.FAIL;
import static com.bank.bo.Transfer.Status.SUCCESS;

/**
 * @author Zaycev Denis
 */
public class TransferService extends BaseDBService {

    public TransferService(EntityManagerFactory factory) {
        super(factory);
    }

    public Transfer execute(Transfer transfer) {
        return inTransaction(manager -> {
            Account remitter = transfer.getRemitter();
            Account beneficiary = transfer.getBeneficiary();

            if (remitter.getNumber().compareTo(beneficiary.getNumber()) < 0) {
                remitter = lockAccount(manager, remitter);
                beneficiary = lockAccount(manager, beneficiary);
            } else {
                beneficiary = lockAccount(manager, beneficiary);
                remitter = lockAccount(manager, remitter);
            }

            try {

                checkTransferAvailable(remitter, beneficiary, transfer.getAmount());

            } catch (IllegalArgumentException e) {
                transfer.setStatus(FAIL);
                return transfer;
            }

            moveMoney(remitter, beneficiary, transfer.getAmount());

            manager.merge(remitter);
            manager.merge(beneficiary);

            transfer.setStatus(SUCCESS);

            return transfer;
        });
    }

    private Account lockAccount(EntityManager manager, Account account) {
        return manager.find(Account.class, account.getId(), LockModeType.PESSIMISTIC_WRITE);
    }

    private void checkTransferAvailable(Account remitter, Account beneficiary, Money amount) {
        Money remitterBalance = remitter.getBalance();
        Money beneficiaryBalance = beneficiary.getBalance();

        if (!remitterBalance.getCurrencyUnit().equals(beneficiaryBalance.getCurrencyUnit())
                || !remitterBalance.getCurrencyUnit().equals(amount.getCurrencyUnit())) {
            throw new UnsupportedOperationException("Currency conversion not supported yet");
        }

        if (remitterBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
    }

    private void moveMoney(Account remitter, Account beneficiary, Money amount) {
        Money remitterBalance = remitter.getBalance();
        Money beneficiaryBalance = beneficiary.getBalance();

        remitter.setBalance(remitterBalance.minus(amount));
        beneficiary.setBalance(beneficiaryBalance.plus(amount));
    }

}
