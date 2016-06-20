package com.mercadopago.mpservices.services;

import com.mercadopago.mpservices.adapters.MPCall;
import com.mercadopago.mpservices.model.CheckoutPreference;
import com.mercadopago.mpservices.model.Customer;
import com.mercadopago.mpservices.model.MerchantPayment;
import com.mercadopago.mpservices.model.Payment;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MerchantService {

    @GET("/{uri}")
    MPCall<Customer> getCustomer(@Path(value="uri", encoded = true) String uri, @Query(value="merchant_access_token", encoded = true) String merchantAccessToken);

    @POST("/{uri}")
    MPCall<Payment> createPayment(@Path(value="uri", encoded = true) String uri, @Body MerchantPayment body);

    @POST("/{uri}")
    MPCall<CheckoutPreference> createPreference(@Path(value = "uri", encoded = true) String uri, @Body Map<String, Object> body);
}
