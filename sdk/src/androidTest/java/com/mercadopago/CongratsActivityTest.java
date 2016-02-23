package com.mercadopago;

import android.content.Intent;

import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.test.BaseTest;
import com.mercadopago.test.StaticMock;

public class CongratsActivityTest extends BaseTest<OldCongratsActivity> {

    public CongratsActivityTest() {

        super(OldCongratsActivity.class);
    }

    public void testApprovedPaymentCongrats() {

        // Set activity
        OldCongratsActivity activity = prepareActivity(StaticMock.getPayment(getApplicationContext()),
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.mpsdk_approved_title)));
    }

    public void testPendingPaymentCongrats() {

        // Set activity
        Payment payment = StaticMock.getPayment(getApplicationContext());
        payment.setStatus("pending");
        OldCongratsActivity activity = prepareActivity(payment,
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.mpsdk_pending_title)));
    }

    public void testInProcessPaymentCongrats() {

        // Set activity
        Payment payment = StaticMock.getPayment(getApplicationContext());
        payment.setStatus("in_process");
        OldCongratsActivity activity = prepareActivity(payment,
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.mpsdk_in_process_title)));
    }

    public void testRejectedPaymentCongrats() {

        // Set activity
        Payment payment = StaticMock.getPayment(getApplicationContext());
        payment.setStatus("rejected");
        OldCongratsActivity activity = prepareActivity(payment,
                StaticMock.getPaymentMethod(getApplicationContext()));

        // Validate view
        assertTrue(activity.getTitle().equals(activity.getString(R.string.mpsdk_rejected_title)));
    }

    private OldCongratsActivity prepareActivity(Payment payment, PaymentMethod paymentMethod) {

        Intent intent = new Intent();
        if (payment != null) {
            intent.putExtra("payment", payment);
        }
        if (paymentMethod != null) {
            intent.putExtra("paymentMethod", paymentMethod);
        }
        setActivityIntent(intent);
        return getActivity();
    }
}