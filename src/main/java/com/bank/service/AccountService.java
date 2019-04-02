package com.bank.service;

import com.bank.bo.Account;

import javax.persistence.EntityManagerFactory;

/**
 * @author Zaycev Denis
 */
public class AccountService extends BaseDBService {

    private static final String ACCOUNT_BY_NUMBER_SELECT = "select a from Account a where a.number = :number";

    public AccountService(EntityManagerFactory factory) {
        super(factory);
    }

    public Account saveAccount(Account account) {
        return inTransaction(manager -> {
            manager.persist(account);
            return account;
        });
    }

    @SuppressWarnings("unchecked")
    public Account getAccount(String number) {
        Account account = withJPA(manager -> (Account) manager
                .createQuery(ACCOUNT_BY_NUMBER_SELECT)
                .setParameter("number", number)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null));

        if (account == null) {
            throw new IllegalArgumentException("No such account: " + number);
        }

        return account;
    }

}
