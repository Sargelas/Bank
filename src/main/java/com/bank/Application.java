package com.bank;

import com.bank.bo.Account;
import com.bank.controller.TransferController;
import com.bank.controller.to.ErrorTO;
import com.bank.service.AccountService;
import com.bank.service.TransferService;
import com.bank.util.json.MoneyDeserializer;
import com.bank.util.json.MoneySerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static spark.Spark.exception;
import static spark.Spark.post;

/**
 * @author Zaycev Denis
 */
public class Application {

    private EntityManagerFactory entityManagerFactory;

    private AccountService accountService;
    private TransferService transferService;

    private Gson gson;
    private TransferController transferController;

    public static void main(String[] args) {
        Application application = new Application();
        application.init();
        application.startWebServer();
    }

    public void init() {
        initDBEmulator();
        initApplication();
        initTestData();
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public TransferService getTransferService() {
        return transferService;
    }

    public void startWebServer() {
        post("/transfers", transferController);

        exception(Exception.class, (e, request, response) -> {
            response.header("Content-Type", "application/json");
            response.status(e instanceof IllegalArgumentException ? 400 : 500);
            response.body(gson.toJson(
                    new ErrorTO(e instanceof IllegalArgumentException ? e.getMessage() : "Internal server error")
            ));
        });
    }

    private void initDBEmulator() {
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
        Runtime.getRuntime().addShutdownHook(new Thread(entityManagerFactory::close));
    }

    private void initApplication() {
        accountService = new AccountService(entityManagerFactory);
        transferService = new TransferService(entityManagerFactory);

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Money.class, new MoneySerializer())
                .registerTypeAdapter(Money.class, new MoneyDeserializer())
                .create();

        transferController = new TransferController(gson, accountService, transferService);
    }

    private void initTestData() {
        Account account1 = new Account();
        account1.setNumber("123456789");
        account1.setBalance(Money.of(CurrencyUnit.GBP, 100));

        Account account2 = new Account();
        account2.setNumber("987654321");
        account2.setBalance(Money.of(CurrencyUnit.GBP, 150));

        accountService.saveAccount(account1);
        accountService.saveAccount(account2);
    }

}
