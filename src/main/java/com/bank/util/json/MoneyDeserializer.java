package com.bank.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @author Zaycev Denis
 */
public class MoneyDeserializer implements JsonDeserializer<Money> {

    @Override
    public Money deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        return Money.of(
                CurrencyUnit.of(jsonObject.get("currency").getAsString()),
                new BigDecimal(jsonObject.get("value").getAsString())
        );
    }

}
