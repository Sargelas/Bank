package com.bank.it;

import io.restassured.specification.RequestSpecification;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * @author Zaycev Denis
 */
public class ITTransferController {

    private static final String TRANSFERS_PATH = "/transfers";

    @Test
    public void accountToAccountMustSucceed() {
        request().body(transferJson("123456789", "987654321", "1.00", "GBP")).post(TRANSFERS_PATH)
                .then()
                .statusCode(200)
                .body("remitterAccount", is("123456789"))
                .body("beneficiaryAccount", is("987654321"))
                .body("amount.value", is("1.00"))
                .body("amount.currency", is("GBP"))
                .body("status", is("SUCCESS"));
    }

    @Test
    public void accountToAccountMustFailOnCurrencyConversion() {
        request().body(transferJson("123456789", "987654321", "1.00", "USD")).post(TRANSFERS_PATH)
                .then()
                .statusCode(500)
                .body("message", is("Internal server error"));
    }

    @Test
    public void accountToAccountMustFailOnInsufficientFunds() {
        request().body(transferJson("123456789", "987654321", "99999.00", "GBP")).post(TRANSFERS_PATH)
                .then()
                .statusCode(400)
                .body("remitterAccount", is("123456789"))
                .body("beneficiaryAccount", is("987654321"))
                .body("amount.value", is("99999.00"))
                .body("amount.currency", is("GBP"))
                .body("status", is("FAIL"));
    }

    @Test
    public void accountToAccountMustFailOnNoSuchAccount() {
        request().body(transferJson("123", "987654321", "1.00", "GBP")).post(TRANSFERS_PATH)
                .then()
                .statusCode(400)
                .body("message", is("No such account: 123"));
    }

    @Test
    public void accountToAccountMustFailOnBadRestRequestFormat() {
        request().body("{}").post(TRANSFERS_PATH)
                .then()
                .statusCode(400)
                .body("message", is("Invalid transfer request"));

        request().body(transferJson("123456789", "987654321", "-1.00", "GBP")).post(TRANSFERS_PATH)
                .then()
                .statusCode(400)
                .body("message", is("Amount must be greater than zero"));
    }

    private String transferJson(String remitter, String beneficiary, String amount, String currency) {
        return "{"
                +     "\"remitterAccount\": \"" + remitter + "\", "
                +     "\"beneficiaryAccount\": \"" + beneficiary + "\", "
                +     "\"amount\": { "
                +         "\"currency\": \"" + currency + "\", "
                +         "\"value\": \"" + amount + "\""
                +     "}"
                + "}";
    }

    private RequestSpecification request() {
        return given().port(4567);
    }

}
