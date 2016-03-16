package com.mercadopago;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mercadopago.controllers.ShoppingCartViewController;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.exceptions.CheckoutPreferenceException;
import com.mercadopago.exceptions.ExceptionHandler;
import com.mercadopago.model.CheckoutPreference;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Item;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentIntent;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.SavedCardToken;
import com.mercadopago.model.Token;
import com.mercadopago.util.CurrenciesUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;

import java.math.BigDecimal;

public class CheckoutActivity extends AppCompatActivity{

    //Parameters
    protected CheckoutPreference mCheckoutPreference;
    protected String mMerchantPublicKey;
    protected Boolean mShowBankDeals;

    //Local vars
    protected MercadoPago mMercadoPago;
    protected Activity mActivity;
    protected Payment mPayment;
    protected boolean mSupportMPApp = true;
    protected PaymentMethod mSelectedPaymentMethod;
    protected Issuer mSelectedIssuer;
    protected PayerCost mSelectedPayerCost;
    protected Token mCreatedToken;
    protected String mPurchaseTitle;
    protected ShoppingCartViewController mShoppingCartViewController;
    protected String mErrorMessage;
    protected String mSelectedPaymentMethodInfo;
    protected SavedCardToken mSavedCardToken;

    //Controls
    protected TextView mPaymentMethodCommentTextView;
    protected TextView mTermsAndConditionsTextView;
    protected TextView mTotalAmountTextView;
    protected ImageView mPaymentMethodImageView;
    protected ImageView mEditPaymentMethodImageView;
    protected LinearLayout mPaymentMethodLayout;
    protected Button mPayButton;
    protected TextView mPaymentMethodDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mCheckoutPreference = (CheckoutPreference) this.getIntent().getSerializableExtra("checkoutPreference");
        mMerchantPublicKey = this.getIntent().getStringExtra("merchantPublicKey");
        mShowBankDeals = this.getIntent().getBooleanExtra("showBankDeals", true);

        boolean validState = true;
        try{
            validateParameters();
        }
        catch(CheckoutPreferenceException e){
            mErrorMessage = ExceptionHandler.getErrorMessage(this, e);
            validState = false;
        } catch (Exception e) {
            mErrorMessage = e.getMessage();
            validState = false;
        }
        if(validState) {
            getApplicationContext();
            initializeActivityControls();

            setActivity();

            mMercadoPago = new MercadoPago.Builder()
                    .setContext(this)
                    .setPublicKey(mMerchantPublicKey)
                    .build();
            startPaymentVaultActivity();
        }
        else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("message", mErrorMessage);
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    private void validateParameters() throws Exception {
        if(!validParameters()) {
            throw new IllegalStateException("Invalid parameters");
        }
        else {
            mCheckoutPreference.validate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.shopping_cart_menu, menu);
        mShoppingCartViewController = new ShoppingCartViewController(this, menu.findItem(R.id.shopping_cart), mCheckoutPreference.getItems().get(0).getPictureUrl(), mPurchaseTitle,
                mCheckoutPreference.getAmount(), mCheckoutPreference.getItems().get(0).getCurrencyId(), true, findViewById(R.id.contentLayout));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.shopping_cart) {
            mShoppingCartViewController.toggle(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeActivityControls() {

        mTermsAndConditionsTextView = (TextView) findViewById(R.id.termsAndConditions);
        mTermsAndConditionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTermsAndConditionsActivity();
            }
        });

        mPayButton = (Button) findViewById(R.id.payButton);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPayment();
            }
        });
        mPurchaseTitle = getPurchaseTitle();
        mTotalAmountTextView = (TextView) findViewById(R.id.totalAmount);
        mPaymentMethodLayout = (LinearLayout) findViewById(R.id.paymentMethodLayout);
    }

    protected void startTermsAndConditionsActivity() {
        Intent termsAndConditionsIntent = new Intent(this, TermsAndConditionsActivity.class);
        startActivity(termsAndConditionsIntent);
    }

    protected void setActivity() {
        this.mActivity = this;
    }

    protected void startPaymentVaultActivity() {

        MercadoPago.StartActivityBuilder builder = new MercadoPago.StartActivityBuilder();
        builder.setActivity(this);
        builder.setPublicKey(mMerchantPublicKey);
        builder.setSupportMPApp(mSupportMPApp);
        builder.setItemImageUri(mCheckoutPreference.getItems().get(0).getPictureUrl());
        builder.setPurchaseTitle(mPurchaseTitle);
        builder.setCurrency(mCheckoutPreference.getItems().get(0).getCurrencyId());
        builder.setAmount(mCheckoutPreference.getAmount());
        builder.setShowBankDeals(mShowBankDeals);
        builder.setDefaultPaymentMethodId(mCheckoutPreference.getDefaultPaymentMethodId());
        builder.setExcludedPaymentMethodIds(mCheckoutPreference.getExcludedPaymentMethods());
        builder.setExcludedPaymentTypes(mCheckoutPreference.getExcludedPaymentTypes());
        builder.setDefaultInstallments(mCheckoutPreference.getDefaultInstallments());
        builder.setMaxInstallments(mCheckoutPreference.getMaxInstallments());

        if(payerHasEmail())
        {
            builder.setPayerEmail(mCheckoutPreference.getPayer().getEmail());
        }

        builder.startPaymentVaultActivity();
    }

    private Spanned getAmountLabel() {
        String currencyId = mCheckoutPreference.getItems().get(0).getCurrencyId();
        return CurrenciesUtil.formatNumber(mCheckoutPreference.getAmount(), currencyId, false, true);
    }

    private String getPurchaseTitle() {
        StringBuilder purchaseTitle = new StringBuilder();
        int itemListSize = mCheckoutPreference.getItems().size();

        if(itemListSize == 1) {
            purchaseTitle.append(mCheckoutPreference.getItems().get(0).getTitle());
        }
        else {
            for(Item item : mCheckoutPreference.getItems()){
                purchaseTitle.append(item.getTitle());
                if(!item.equals(mCheckoutPreference.getItems().get(itemListSize))) {
                    purchaseTitle.append(", ");
                }
                purchaseTitle.append(item.getTitle());
            }
        }
        return purchaseTitle.toString();
    }

    protected boolean payerHasEmail() {

        return mCheckoutPreference != null
                && mCheckoutPreference.getPayer() != null
                && mCheckoutPreference.getPayer().getEmail() != null
                && !mCheckoutPreference.getPayer().getEmail().equals("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MercadoPago.PAYMENT_VAULT_REQUEST_CODE) {
            resolvePaymentVaultResponse(resultCode, data);
        }
    }

    private void resolvePaymentVaultResponse(int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {

            mSelectedIssuer = (Issuer) data.getSerializableExtra("issuer");

            mSelectedPayerCost = (PayerCost) data.getSerializableExtra("payerCost");

            mCreatedToken = (Token) data.getSerializableExtra("token");

            mSavedCardToken = (SavedCardToken) data.getSerializableExtra("token");

            mSelectedPaymentMethod = (PaymentMethod) data.getSerializableExtra("paymentMethod");

            mSelectedPaymentMethodInfo = data.getStringExtra("paymentMethodInfo");

            showReviewAndConfirm();
        }
        else if (resultCode == RESULT_CANCELED) {
            if(mSelectedPaymentMethod == null) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        }
    }

    private void showReviewAndConfirm() {
        drawPaymentMethodRow();
        drawTermsAndConditionsText();
        setAmountLabel();
    }

    private void setAmountLabel() {
        mTotalAmountTextView.setText(getAmountLabel());
    }

    private void drawTermsAndConditionsText() {
        StringBuilder termsAndConditionsText = new StringBuilder();
        termsAndConditionsText.append(getString(R.string.mpsdk_text_terms_and_conditions_start) + " ");
        termsAndConditionsText.append(" <font color='blue'>" + getString(R.string.mpsdk_text_terms_and_conditions_linked) + "</font> ");
        termsAndConditionsText.append(" " + getString(R.string.mpsdk_text_terms_and_conditions_end));
        mTermsAndConditionsTextView.setText(Html.fromHtml(termsAndConditionsText.toString()));
    }

    private void drawPaymentMethodRow() {
        mPaymentMethodLayout.removeAllViewsInLayout();
        if(this.isCardSelected()) {
            drawCardRow();
        }
        else if (isAccountMoneySelected()){
            drawAccountMoney();
        }
        else {
            drawOfflinePaymentMethod();
        }
    }

    private void drawOfflinePaymentMethod() {
        getLayoutInflater().inflate(R.layout.row_pm_no_description_edit, mPaymentMethodLayout);
        initializePaymentMethodRowControls();
        mPaymentMethodImageView.setImageResource(MercadoPagoUtil.getPaymentMethodSearchItemIcon(this, mSelectedPaymentMethod.getId()));
        mPaymentMethodCommentTextView.setText(mSelectedPaymentMethodInfo);
    }

    private void drawAccountMoney() {
        getLayoutInflater().inflate(R.layout.row_pm_with_description_edit, mPaymentMethodLayout);
        initializePaymentMethodRowControls();
        mPaymentMethodDescription.setText(mSelectedPaymentMethod.getName());
        mPaymentMethodImageView.setImageResource(MercadoPagoUtil.getPaymentMethodSearchItemIcon(this, mSelectedPaymentMethod.getId()));

    }

    private void drawCardRow() {
        getLayoutInflater().inflate(R.layout.row_pm_with_description_edit, mPaymentMethodLayout);
        initializePaymentMethodRowControls();
        mPaymentMethodDescription.setText(mSelectedPaymentMethodInfo);
        mPaymentMethodImageView.setImageResource(MercadoPagoUtil.getPaymentMethodSearchItemIcon(this, mSelectedPaymentMethod.getId()));
    }

    private boolean isAccountMoneySelected() {
        return mSelectedPaymentMethod != null && mSelectedPaymentMethod.getId().equals("account_money");
    }

    private boolean isCardSelected() {
        return MercadoPagoUtil.isCardPaymentType(mSelectedPaymentMethod.getPaymentTypeId());
    }

    private void initializePaymentMethodRowControls() {

        mPaymentMethodDescription = (TextView) findViewById(R.id.paymentMethodDescription);
        mPaymentMethodCommentTextView = (TextView) findViewById(R.id.paymentMethodComment);
        mPaymentMethodImageView = (ImageView) findViewById(R.id.paymentMethodImage);
        mEditPaymentMethodImageView = (ImageView) findViewById(R.id.imageEdit);
        mEditPaymentMethodImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPaymentVaultActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected boolean validParameters() {

        return (mMerchantPublicKey != null) && (mCheckoutPreference != null);
    }

    protected void createPayment() {
        PaymentIntent paymentIntent = new PaymentIntent();

        paymentIntent.setPrefId(mCheckoutPreference.getId());
        if(mCreatedToken != null) {
            paymentIntent.setToken(mCreatedToken.getId());
        }
        if (mSelectedIssuer != null) {
            paymentIntent.setIssuerId(mSelectedIssuer.getId());
        }
        if(mSelectedPayerCost != null) {
            paymentIntent.setInstallments(mSelectedPayerCost.getInstallments());
        }

        paymentIntent.setPaymentMethodId(mSelectedPaymentMethod.getId());
        // Create payment
        //TODO ir a nuevo servicio de payment
        createMockPayment(paymentIntent);

    }

    //TODO ir a nuevo servicio de payment
    private void createMockPayment(PaymentIntent paymentIntent) {
        if (paymentIntent.getPaymentMethodId() != null) {

            // Set item
            Item item = new Item("id1", 1,
                    new BigDecimal("100"));

            // Set payment method id
            String paymentMethodId = paymentIntent.getPaymentMethodId();

            // Create payment
            /*MerchantPayment payment = new MerchantPayment(item, paymentIntent.getInstallments(), paymentIntent.getIssuerId(),
                    paymentIntent.getToken(), oldPaymentMethodId, null, "mlm-cards-data");

            MerchantServer.createPayment(this, "https://www.mercadopago.com", "/checkout/examples/doPayment", payment, new Callback<Payment>() {
                @Override
                public void success(Payment payment, Response response) {
                    mPayment = payment;
                }

                @Override
                public void failure(RetrofitError error) {

                    LayoutUtil.showRegularLayout(mActivity);
                    Toast.makeText(mActivity, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
       */

           // hideActionBar();
            LayoutUtil.showProgressLayout(this);
            if (MercadoPagoUtil.isCardPaymentType(mSelectedPaymentMethod.getPaymentTypeId())) {
                new MercadoPago.StartActivityBuilder()
                        .setActivity(mActivity)
                        .setPayment(new Payment())
                        .setPaymentMethod(mSelectedPaymentMethod)
                        .startCongratsActivity();
            } else {
                new MercadoPago.StartActivityBuilder()
                        .setPublicKey(mMerchantPublicKey)
                        .setActivity(mActivity)
                        .setPayment(new Payment())
                        .setPaymentMethod(mSelectedPaymentMethod)
                        .startInstructionsActivity();
            }
            LayoutUtil.showRegularLayout(mActivity);
        } else {
            Toast.makeText(mActivity, "Invalid payment method", Toast.LENGTH_LONG).show();
        }
    }

    protected void startMPApp() {

        if ((mCheckoutPreference != null) && (mCheckoutPreference.getId() != null)) {
            Intent intent = new Intent(this, InstallAppActivity.class);
            intent.putExtra("preferenceId", mCheckoutPreference.getId());
            intent.putExtra("packageName", this.getPackageName());
            intent.putExtra("deepLink", "mercadopago://mpsdk_install_app");
            startActivityForResult(intent, MercadoPago.INSTALL_APP_REQUEST_CODE);
        }
    }
}
