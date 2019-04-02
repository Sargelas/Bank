package com.bank.bo;

import org.joda.money.Money;

/**
 * @author Zaycev Denis
 */
public class Transfer {

    public enum Status { SUCCESS, FAIL }

    private Account remitter;
    private Account beneficiary;
    private Money amount;

    private Status status;

    public Account getRemitter() {
        return remitter;
    }

    public void setRemitter(Account remitter) {
        this.remitter = remitter;
    }

    public Account getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Account beneficiary) {
        this.beneficiary = beneficiary;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
