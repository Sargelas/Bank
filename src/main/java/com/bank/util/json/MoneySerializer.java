package com.bank.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joda.money.Money;

import java.lang.reflect.Type;

/**
 * @author Zaycev Denis
 */
public class MoneySerializer implements JsonSerializer<Money> {

    @Override
    public JsonElement serialize(Money src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", src.getAmount().toString());
        jsonObject.addProperty("currency", src.getCurrencyUnit().toString());

        return jsonObject;
    }

}
