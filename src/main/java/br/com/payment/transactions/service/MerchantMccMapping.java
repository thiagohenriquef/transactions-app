package br.com.payment.transactions.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MerchantMccMapping {
    private static final Map<Pattern, String> merchantMccMap = new HashMap<>();

    static {
        merchantMccMap.put(Pattern.compile("UBER TRIP\\s+SAO PAULO BR"), "4121");
        merchantMccMap.put(Pattern.compile("UBER EATS\\s+SAO PAULO BR"), "5812");
        merchantMccMap.put(Pattern.compile("PAG\\*JoseDaSilva\\s+RIO DE JANEI BR"), "6011");
        merchantMccMap.put(Pattern.compile("PICPAY\\*BILHETEUNICO\\s+GOIANIA BR"), "4111");
    }

    public static String getMccForMerchant(String merchant) {
        for (Map.Entry<Pattern, String> entry : merchantMccMap.entrySet()) {
            if (entry.getKey().matcher(merchant).matches()) {
                return entry.getValue();
            }
        }
        return null;
    }
}

