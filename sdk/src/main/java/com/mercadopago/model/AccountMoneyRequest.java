package com.mercadopago.model;

import java.io.Serializable;

/**
 * Created by mreverter on 17/3/16.
 */
public class AccountMoneyRequest implements Serializable {

    private String authCode;
    private boolean securityCodeRequired;

    public AccountMoneyRequest(String authCode, boolean securityCodeRequired) {
        this.authCode = authCode;
        this.securityCodeRequired = securityCodeRequired;
    }

    public String getAuthCode() {
        return authCode;
    }

    public boolean isSecurityCodeRequired() {
        return securityCodeRequired;
    }
}
