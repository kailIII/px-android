package com.mercadopago.mpservices.core;

import okhttp3.logging.HttpLoggingInterceptor;

public class Settings {
    public static final HttpLoggingInterceptor.Level OKHTTP_LOGGING = HttpLoggingInterceptor.Level.BODY;
}
