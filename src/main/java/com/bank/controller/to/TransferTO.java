package com.bank.controller.to;

import com.bank.bo.Transfer.Status;
import org.joda.money.Money;

/**
 * @author Zaycev Denis
 */
public class TransferTO {

    public String remitterAccount;
    public String beneficiaryAccount;
    public Money amount;

    public Status status;

}
