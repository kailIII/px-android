package com.mercadopago.examples.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.core.MerchantServer;
import com.mercadopago.examples.step2.SimpleVaultActivity;
import com.mercadopago.examples.step3.AdvancedVaultActivity;
import com.mercadopago.examples.step1.CardActivity;
import com.mercadopago.examples.step4.FinalVaultActivity;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Item;
import com.mercadopago.model.MerchantPayment;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentPreference;
import com.mercadopago.util.LayoutUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

public class ExamplesUtils {

    public static final int SIMPLE_VAULT_REQUEST_CODE = 10;
    public static final int ADVANCED_VAULT_REQUEST_CODE = 11;
    public static final int FINAL_VAULT_REQUEST_CODE = 12;
    public static final int CARD_REQUEST_CODE = 13;

    // * Merchant public key
    public static final String DUMMY_MERCHANT_PUBLIC_KEY = "TEST-ad365c37-8012-4014-84f5-6c895b3f8e0a";
    // DUMMY_MERCHANT_PUBLIC_KEY_AR = "444a9ef5-8a6b-429f-abdf-587639155d88";
    // DUMMY_MERCHANT_PUBLIC_KEY_BR = "APP_USR-f163b2d7-7462-4e7b-9bd5-9eae4a7f99c3";
    // DUMMY_MERCHANT_PUBLIC_KEY_MX = "6c0d81bc-99c1-4de8-9976-c8d1d62cd4f2";
    // DUMMY_MERCHANT_PUBLIC_KEY_VZ = "2b66598b-8b0f-4588-bd2f-c80ca21c6d18";
    // DUMMY_MERCHANT_PUBLIC_KEY_CO = "aa371283-ad00-4d5d-af5d-ed9f58e139f1";

    // * Merchant server vars
    public static final String DUMMY_MERCHANT_BASE_URL = "https://www.mercadopago.com";
    public static final String DUMMY_MERCHANT_GET_CUSTOMER_URI = "/checkout/examples/getCustomer";
    public static final String DUMMY_MERCHANT_CREATE_PAYMENT_URI = "/checkout/examples/doPayment";
    //public static final String DUMMY_MERCHANT_GET_DISCOUNT_URI = "/checkout/examples/getDiscounts";

    // * Merchant access token
    public static final String DUMMY_MERCHANT_ACCESS_TOKEN = "mlm-cards-data";
    // DUMMY_MERCHANT_ACCESS_TOKEN_AR = "mla-cards-data";
    // DUMMY_MERCHANT_ACCESS_TOKEN_BR = "mlb-cards-data";
    // DUMMY_MERCHANT_ACCESS_TOKEN_MX = "mlm-cards-data";
    // DUMMY_MERCHANT_ACCESS_TOKEN_VZ = "mlv-cards-data";
    // DUMMY_MERCHANT_ACCESS_TOKEN_VZ = "mco-cards-data";
    // DUMMY_MERCHANT_ACCESS_TOKEN_NO_CCV = "mla-cards-data-tarshop";

    // * Payment item
    public static final String DUMMY_ITEM_ID = "id1";
    public static final Integer DUMMY_ITEM_QUANTITY = 1;
    public static final BigDecimal DUMMY_ITEM_UNIT_PRICE = new BigDecimal("100");

    public static final Integer DUMMY_MAX_INSTALLMENTS = 6;
    public static final Integer DUMMY_DEFAULT_INSTALLMENTS = 3;

    public static void startCardActivity(Activity activity, String merchantPublicKey, PaymentMethod paymentMethod) {

        Intent cardIntent = new Intent(activity, CardActivity.class);
        cardIntent.putExtra("merchantPublicKey", merchantPublicKey);
        cardIntent.putExtra("paymentMethod", paymentMethod);
        activity.startActivityForResult(cardIntent, CARD_REQUEST_CODE);
    }

    public static void startGuessingCardActivity(Activity activity, String merchantPublicKey,
                                                 boolean issuerRequired, PaymentPreference paymentPreference,
                                                 BigDecimal amount) {

        new MercadoPago.StartActivityBuilder()
                .setActivity(activity)
                .setPublicKey(merchantPublicKey)
                .setRequireSecurityCode(true)
                .setRequireIssuer(issuerRequired)
                .setShowBankDeals(true)
                .setAmount(amount)
                .setPaymentPreference(paymentPreference)
                .startGuessingCardActivity();
    }

    public static void startSimpleVaultActivity(Activity activity, String merchantPublicKey, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, List<String> excludedPaymentTypes) {

        Intent simpleVaultIntent = new Intent(activity, SimpleVaultActivity.class);
        simpleVaultIntent.putExtra("merchantPublicKey", merchantPublicKey);
        simpleVaultIntent.putExtra("merchantBaseUrl", merchantBaseUrl);
        simpleVaultIntent.putExtra("merchantGetCustomerUri", merchantGetCustomerUri);
        simpleVaultIntent.putExtra("merchantAccessToken", merchantAccessToken);
        putListExtra(simpleVaultIntent, "excludedPaymentTypes", excludedPaymentTypes);
        activity.startActivityForResult(simpleVaultIntent, SIMPLE_VAULT_REQUEST_CODE);
    }

    public static void startAdvancedVaultActivity(Activity activity, String merchantPublicKey, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, BigDecimal amount, List<String> excludedPaymentTypes) {
        startAdvancedVaultActivity(activity, ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY,
                ExamplesUtils.DUMMY_MERCHANT_BASE_URL, ExamplesUtils.DUMMY_MERCHANT_GET_CUSTOMER_URI,
                ExamplesUtils.DUMMY_MERCHANT_ACCESS_TOKEN, new BigDecimal("20"), excludedPaymentTypes, false);
    }

    public static void startAdvancedVaultActivityWithGuessing(Activity activity, String dummyMerchantPublicKey, String dummyMerchantBaseUrl, String dummyMerchantGetCustomerUri, String dummyMerchantAccessToken, BigDecimal bigDecimal, List<String> excludedPaymentTypes) {
        startAdvancedVaultActivity(activity, ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY,
                ExamplesUtils.DUMMY_MERCHANT_BASE_URL, ExamplesUtils.DUMMY_MERCHANT_GET_CUSTOMER_URI,
                ExamplesUtils.DUMMY_MERCHANT_ACCESS_TOKEN, new BigDecimal("20"), excludedPaymentTypes, true);
    }

    public static void startAdvancedVaultActivity(Activity activity, String merchantPublicKey, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, BigDecimal amount, List<String> excludedPaymentTypes, Boolean guessingEnabled) {

        Intent advVaultIntent = new Intent(activity, AdvancedVaultActivity.class);
        advVaultIntent.putExtra("merchantPublicKey", merchantPublicKey);
        advVaultIntent.putExtra("merchantBaseUrl", merchantBaseUrl);
        advVaultIntent.putExtra("merchantGetCustomerUri", merchantGetCustomerUri);
        advVaultIntent.putExtra("merchantAccessToken", merchantAccessToken);
        advVaultIntent.putExtra("amount", amount.toString());
        putListExtra(advVaultIntent, "excludedPaymentTypes", excludedPaymentTypes);
        activity.startActivityForResult(advVaultIntent, ADVANCED_VAULT_REQUEST_CODE);
    }

    public static void startFinalVaultActivity(Activity activity, String merchantPublicKey, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, BigDecimal amount, List<String> excludedPaymentTypes) {

        Intent finalVaultIntent = new Intent(activity, FinalVaultActivity.class);
        finalVaultIntent.putExtra("merchantPublicKey", merchantPublicKey);
        finalVaultIntent.putExtra("merchantBaseUrl", merchantBaseUrl);
        finalVaultIntent.putExtra("merchantGetCustomerUri", merchantGetCustomerUri);
        finalVaultIntent.putExtra("merchantAccessToken", merchantAccessToken);
        finalVaultIntent.putExtra("amount", amount.toString());
        putListExtra(finalVaultIntent, "excludedPaymentTypes", excludedPaymentTypes);
        activity.startActivityForResult(finalVaultIntent, FINAL_VAULT_REQUEST_CODE);
    }

    public static void createPayment(final Activity activity, String token, Integer installments, Long cardIssuerId, final PaymentMethod paymentMethod, Discount discount) {

        if (paymentMethod != null) {

            LayoutUtil.showProgressLayout(activity);

            // Set item
            Item item = new Item(DUMMY_ITEM_ID, DUMMY_ITEM_QUANTITY,
                    DUMMY_ITEM_UNIT_PRICE);

            // Set payment method id
            String paymentMethodId = paymentMethod.getId();

            // Set campaign id
            Long campaignId = (discount != null) ? discount.getId() : null;

            // Set merchant payment
            MerchantPayment payment = new MerchantPayment(item, installments, cardIssuerId,
                    token, paymentMethodId, campaignId, DUMMY_MERCHANT_ACCESS_TOKEN);

            // Create payment
            MerchantServer.createPayment(activity, DUMMY_MERCHANT_BASE_URL, DUMMY_MERCHANT_CREATE_PAYMENT_URI, payment, new Callback<Payment>() {
                @Override
                public void success(Payment payment) {

                    new MercadoPago.StartActivityBuilder()
                            .setActivity(activity)
                            .setPayment(payment)
                            .setPaymentMethod(paymentMethod)
                            .startCongratsActivity();
                }

                @Override
                public void failure(ApiException error) {
                    LayoutUtil.showRegularLayout(activity);
                    Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {

            Toast.makeText(activity, "Invalid payment method", Toast.LENGTH_LONG).show();
        }
    }

//
//    //TODO consumir servicio de preferencia
//    public static String getFile(Context context, String fileName) {
//
//        try {
//            AssetManager assetManager = context.getResources().getAssets();
//
//            InputStream is = assetManager.open(fileName);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//
//            return new String(buffer);
//
//        } catch (Exception e) {
//
//            return "";
//        }
//    }

    private static void putListExtra(Intent intent, String listName, List<String> list) {

        if (list != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            intent.putExtra(listName, gson.toJson(list, listType));
        }
    }
}
