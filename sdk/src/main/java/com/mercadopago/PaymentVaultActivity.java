package com.mercadopago;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.adapters.PaymentMethodSearchItemAdapter;
import com.mercadopago.callbacks.GetPaymentMethodCallback;
import com.mercadopago.callbacks.PaymentMethodSearchCallback;
import com.mercadopago.controllers.ShoppingCartViewController;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.AccountMoneyRequest;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentMethodSearchItem;
import com.mercadopago.model.PaymentType;
import com.mercadopago.model.SavedCardToken;
import com.mercadopago.model.Token;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.CurrenciesUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentVaultActivity extends AppCompatActivity {
    //TODO validaciones de currency no finaliza activity
    private static final int PURCHASE_TITLE_MAX_LENGTH = 50;
    // Local vars
    protected Activity mActivity;
    protected String mExceptionOnMethod;
    protected MercadoPago mMercadoPago;
    protected PaymentMethod mSelectedPaymentMethod;
    protected CardToken mCardToken;
    protected Issuer mSelectedIssuer;
    protected PayerCost mSelectedPayerCost;
    protected ShoppingCartViewController mShoppingCartViewController;

    // Controls
    protected RecyclerView mGroupsRecyclerView;
    protected RecyclerView mPreferredRecyclerView;
    protected TextView mPreferredTitleTextView;
    protected TextView mGroupsTitleTextView;
    protected TextView mActivityTitle;

    // Current values
    protected PaymentMethodSearch mPaymentMethodSearch;

    // Activity parameters
    protected String mMerchantPublicKey;
    protected BigDecimal mAmount;
    protected String mMerchantAccessToken;
    protected String mMerchantBaseUrl;
    protected String mMerchantGetCustomerUri;
    protected boolean mShowBankDeals;
    protected boolean mCardGuessingEnabled;
    protected Integer mDefaultInstallments;
    protected Integer mMaxInstallments;
    protected List<String> mExcludedPaymentMethodIds;
    protected List<String> mExcludedPaymentTypes;
    protected String mDefaultPaymentMethodId;
    protected Boolean mSupportMPApp;
    protected PaymentMethodSearchItem mSelectedSearchItem;
    protected String mPurchaseTitle;
    protected String mItemImageUri;
    protected String mCurrencyId;
    protected String mPayerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_vault);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getActivityParameters();

        try {
            validateActivityParameters();
        } catch (IllegalStateException e) {
            //TODO: ver como reaccionar ante errores
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mMercadoPago = new MercadoPago.Builder()
                .setPublicKey(mMerchantPublicKey)
                .setContext(this)
                .build();

        initializeControls();
        setActivity();


        if(isItemSelected()) {
            showItemChildren(mSelectedSearchItem);
        }
        else {
            String initialTitle = getString(R.string.mpsdk_title_activity_payment_vault);
            setActivityTitle(initialTitle);
            LayoutUtil.showProgressLayout(this);
            getPaymentMethodSearch();
        }
    }

    private void validateActivityParameters() {

        if (!isAmountValid()){
            throw new IllegalStateException(getString(R.string.mpsdk_error_message_invalid_amount));
        }
        else if(!isCurrencyIdValid()){
            throw new IllegalStateException(getString(R.string.mpsdk_error_message_invalid_currency));
        }
        else if (!isPurchaseTitleValid()){
            throw new IllegalStateException(getString(R.string.mpsdk_error_message_invalid_title));
        }
        else if (!isMerchantPublicKeyValid()){
            throw new IllegalStateException(getString(R.string.mpsdk_error_message_invalid_merchant));
        }
        else if (!validInstallmentsPreferences()){
            throw new IllegalStateException(getString(R.string.mpsdk_error_message_invalid_installments));
        }
        else if (!validPaymentTypes()){
            throw new IllegalStateException(getString(R.string.mpsdk_error_message_excluded_all_payment_type));
        }
    }

    private boolean validPaymentTypes() {
        boolean valid = true;
        if(mExcludedPaymentTypes != null && mExcludedPaymentTypes.size() >= PaymentType.getAllPaymentTypes().size()) {
            valid = false;
        }
        return valid;
    }

    private boolean validInstallmentsPreferences() {

        boolean isValid = true;
        if(mDefaultInstallments != null && mDefaultInstallments <= 0
                || mMaxInstallments != null && mMaxInstallments <= 0) {
            isValid = false;
        }
        return isValid;
    }

    private boolean isAmountValid() {
        return mAmount != null && mAmount.compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean isMerchantPublicKeyValid() {
        return mMerchantPublicKey != null;
    }

    private boolean isPurchaseTitleValid() {
        return mPurchaseTitle != null;
    }

    private boolean isCurrencyIdValid() {

        boolean isValid = true;

        if(mCurrencyId == null) {
            isValid = false;
        }
        else if((!mCurrencyId.equals(CurrenciesUtil.CURRENCY_ARGENTINA))
                && (!mCurrencyId.equals(CurrenciesUtil.CURRENCY_BRAZIL))
                && (!mCurrencyId.equals(CurrenciesUtil.CURRENCY_CHILE))
                && (!mCurrencyId.equals(CurrenciesUtil.CURRENCY_COLOMBIA))
                && (!mCurrencyId.equals(CurrenciesUtil.CURRENCY_MEXICO))
                && (!mCurrencyId.equals(CurrenciesUtil.CURRENCY_VENEZUELA))
                && (!mCurrencyId.equals(CurrenciesUtil.CURRENCY_USA))){
            isValid = false;
        }
        return isValid;
    }

    protected void getActivityParameters() {
        if (this.getIntent().getSerializableExtra("selectedSearchItem") != null) {
            mSelectedSearchItem = (PaymentMethodSearchItem) this.getIntent().getSerializableExtra("selectedSearchItem");
        }

        try {
            mAmount = new BigDecimal(this.getIntent().getStringExtra("amount"));
        } catch (Exception ex) {
            mAmount = null;
        }
        mCurrencyId = this.getIntent().getStringExtra("currencyId");
        mItemImageUri = this.getIntent().getStringExtra("itemImageUri");
        mPurchaseTitle = getFormattedPurchaseTitle();

        mSupportMPApp = this.getIntent().getBooleanExtra("supportMPApp", false);

        mMerchantPublicKey = this.getIntent().getStringExtra("merchantPublicKey");

        mMerchantBaseUrl = this.getIntent().getStringExtra("merchantBaseUrl");
        mMerchantGetCustomerUri = this.getIntent().getStringExtra("merchantGetCustomerUri");
        mMerchantAccessToken = this.getIntent().getStringExtra("merchantAccessToken");
        mCardGuessingEnabled = this.getIntent().getBooleanExtra("cardGuessingEnabled", false);
        mShowBankDeals = this.getIntent().getBooleanExtra("showBankDeals", true);
        mPayerEmail = this.getIntent().getStringExtra("payerEmail");

        if (this.getIntent().getStringExtra("excludedPaymentMethodIds") != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            mExcludedPaymentMethodIds = gson.fromJson(this.getIntent().getStringExtra("excludedPaymentMethodIds"), listType);
        }
        if (this.getIntent().getStringExtra("excludedPaymentTypes") != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            mExcludedPaymentTypes = gson.fromJson(this.getIntent().getStringExtra("excludedPaymentTypes"), listType);
        }
        mDefaultPaymentMethodId = this.getIntent().getStringExtra("defaultPaymentMethodId");

        if(this.getIntent().getStringExtra("maxInstallments") != null) {
            mMaxInstallments = Integer.valueOf(this.getIntent().getStringExtra("maxInstallments"));
        }
        if(this.getIntent().getStringExtra("defaultInstallments") != null) {
            mDefaultInstallments = Integer.valueOf(this.getIntent().getStringExtra("defaultInstallments"));
        }
    }

    protected String getFormattedPurchaseTitle() {
        if(this.getIntent().getStringExtra("purchaseTitle") != null) {
            String purchaseTitle = this.getIntent().getStringExtra("purchaseTitle");
            if (purchaseTitle.length() > PURCHASE_TITLE_MAX_LENGTH) {
                purchaseTitle = purchaseTitle.substring(0, PURCHASE_TITLE_MAX_LENGTH);
                purchaseTitle = purchaseTitle + "…";
            }
            return purchaseTitle;
        }
        else return null;
    }

    protected void initializeControls() {
        initializeGroupRecyclerView();
        initializePreferredRecyclerView();
        mActivityTitle = (TextView) findViewById(R.id.title);
        mPreferredTitleTextView = (TextView) findViewById(R.id.preferredLabel);
        mGroupsTitleTextView = (TextView) findViewById(R.id.groupsLabel);
    }

    protected void initializeGroupRecyclerView() {
        mGroupsRecyclerView = (RecyclerView) findViewById(R.id.groupsList);
        mGroupsRecyclerView.setHasFixedSize(true);
        mGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    protected void initializePreferredRecyclerView() {
        mPreferredRecyclerView = (RecyclerView) findViewById(R.id.preferredList);
        mPreferredRecyclerView.setHasFixedSize(true);
        mPreferredRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopping_cart_menu, menu);
        mShoppingCartViewController = new ShoppingCartViewController(this, menu.findItem(R.id.shopping_cart), mItemImageUri, mPurchaseTitle,
                mAmount, mCurrencyId, false, findViewById(R.id.content_layout));
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

    protected void setActivity() {
        this.mActivity = this;
    }

    protected boolean isItemSelected() {
        return mSelectedSearchItem != null;
    }

    protected void getPaymentMethodSearch() {
        //TODO: Si no viene nada en ninguna llamada, finalizo
        mMercadoPago.getPaymentMethodSearch(mAmount, mExcludedPaymentTypes, mExcludedPaymentMethodIds, new Callback<PaymentMethodSearch>() {
            @Override
            public void success(PaymentMethodSearch paymentMethodSearch, Response response) {
                mPaymentMethodSearch = paymentMethodSearch;
                if (preferredPaymentMethodsPossible()) {
                    getPreferredPaymentMethods();
                } else {
                    setSearchLayout();
                    LayoutUtil.showRegularLayout(mActivity);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                ApiUtil.finishWithApiException(mActivity, error);
            }
        });
    }

    private void getPreferredPaymentMethods() {
        mMercadoPago.getPreferredPaymentMethods(mPayerEmail, new Callback<List<PaymentMethodSearchItem>>() {
            @Override
            public void success(List<PaymentMethodSearchItem> paymentMethodSearchItems, Response response) {
                mPaymentMethodSearch.setPreferred(paymentMethodSearchItems);
                setSearchLayout();
                LayoutUtil.showRegularLayout(mActivity);
            }

            @Override
            public void failure(RetrofitError error) {
                setSearchLayout();
                LayoutUtil.showRegularLayout(mActivity);
            }
        });
    }

    private boolean preferredPaymentMethodsPossible() {
        return this.mPayerEmail != null && !this.mPayerEmail.isEmpty();
    }

    private void finishWithEmptyPaymentMethodSearch() {
        //TODO modificar
        Toast.makeText(mActivity, "No hay medios de pago disponibles", Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void setSearchLayout() {
        if(mPaymentMethodSearch.hasPreferred() && mPaymentMethodSearch.hasSearchItems()) {
            showListTitles();
            populateSearchList(mPaymentMethodSearch.getGroups());
            populatePreferredList(mPaymentMethodSearch.getPreferred());
        }
        else if(mPaymentMethodSearch.hasSearchItems()) {
            populateSearchList(mPaymentMethodSearch.getGroups());
        }
        else if(mPaymentMethodSearch.hasPreferred()) {
            populatePreferredList(mPaymentMethodSearch.getPreferred());
        }
    }

    private void showListTitles() {
        this.mPreferredTitleTextView.setVisibility(View.VISIBLE);
        this.mGroupsTitleTextView.setVisibility(View.VISIBLE);
    }

    protected void populateSearchList(List<PaymentMethodSearchItem> items) {
        PaymentMethodSearchItemAdapter groupsAdapter = new PaymentMethodSearchItemAdapter(this, items, getPaymentMethodSearchCallback());
        mGroupsRecyclerView.setAdapter(groupsAdapter);
        int recyclerViewSize = Math.round(items.size()*getResources().getDimension(R.dimen.list_item_height_large));
        mGroupsRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, recyclerViewSize));
    }

    private void populatePreferredList(List<PaymentMethodSearchItem> preferred) {
        mPreferredRecyclerView.setVisibility(View.VISIBLE);
        PaymentMethodSearchItemAdapter preferredAdapter = new PaymentMethodSearchItemAdapter(this, preferred, getPaymentMethodSearchCallback());
        mPreferredRecyclerView.setAdapter(preferredAdapter);
        int recyclerViewSize = Math.round(preferred.size()*getResources().getDimension(R.dimen.list_item_height_large));
        mPreferredRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, recyclerViewSize));
    }

    protected PaymentMethodSearchCallback getPaymentMethodSearchCallback() {
        return new PaymentMethodSearchCallback() {
            @Override
            public void onGroupItemClicked(PaymentMethodSearchItem groupIem) {
                restartActivityWithSelectedItem(groupIem);
            }

            @Override
            public void onPaymentTypeItemClicked(PaymentMethodSearchItem paymentTypeItem) {
                if(paymentTypeItem.hasChildren()) {
                    restartActivityWithSelectedItem(paymentTypeItem);
                }
                else {
                    startNextStepForPaymentType(paymentTypeItem.getId());
                }
            }

            @Override
            public void onPaymentMethodItemClicked(final PaymentMethodSearchItem paymentMethodItem) {
                mMercadoPago.getPaymentMethodById(paymentMethodItem.getId(), new GetPaymentMethodCallback() {
                    @Override
                    public void onSuccess(PaymentMethod paymentMethod) {
                        finishWithPaymentMethodResult(paymentMethod, paymentMethodItem.getComment());
                    }
                    @Override
                    public void onFailure() {
                        PaymentMethod paymentMethod = new PaymentMethod();
                        paymentMethod.setId(paymentMethodItem.getId());
                        finishWithPaymentMethodResult(paymentMethod, paymentMethodItem.getComment());
                    }
                });
            }

            @Override
            public void onPreferredPaymentMethodItemClicked(PaymentMethodSearchItem preferredPaymentMethodItem) {
                if(preferredPaymentMethodItem.getId().equals("account_money")){
                    resolveAccountMoneySelected(preferredPaymentMethodItem);
                }
                else {
                    resolveSavedCardSelected(preferredPaymentMethodItem);
                }
            }
        };
    }

    private void resolveAccountMoneySelected(PaymentMethodSearchItem preferredPaymentMethodItem) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(preferredPaymentMethodItem.getId());
        paymentMethod.setName(preferredPaymentMethodItem.getDescription());

        AccountMoneyRequest accountMoneyRequest = new AccountMoneyRequest(preferredPaymentMethodItem.getItemToken(), preferredPaymentMethodItem.isSecurityCodeRequired());
        finishWithAccountMoneyResult(paymentMethod, accountMoneyRequest);
    }

    private void resolveSavedCardSelected(final PaymentMethodSearchItem savedCardItem) {
        LayoutUtil.showProgressLayout(this);
        final SavedCardToken savedCardToken = new SavedCardToken(savedCardItem.getItemToken(), "");
        mMercadoPago.getPaymentMethodById(savedCardItem.getId(), new GetPaymentMethodCallback() {
            @Override
            public void onSuccess(PaymentMethod paymentMethod) {
                finishWithSavedCardResult(paymentMethod, savedCardToken, savedCardItem.getDescription());
            }

            @Override
            public void onFailure() {
                PaymentMethod paymentMethod = new PaymentMethod();
                paymentMethod.setId(savedCardItem.getId());
                finishWithSavedCardResult(paymentMethod, savedCardToken, savedCardItem.getDescription());
            }
        });
    }

    private void restartActivityWithSelectedItem(PaymentMethodSearchItem groupIem) {
        Intent intent = new Intent(this, PaymentVaultActivity.class);
        intent.putExtra("selectedSearchItem", groupIem);
        intent.putExtra("merchantPublicKey", mMerchantPublicKey);
        intent.putExtra("currencyId", mCurrencyId);
        intent.putExtra("amount", mAmount.toString());
        intent.putExtra("purchaseTitle", mPurchaseTitle);
        intent.putExtra("itemImageUri", mItemImageUri);
        startActivityForResult(intent, MercadoPago.PAYMENT_VAULT_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_right_to_left_in, R.anim.slide_right_to_left_out);
    }

    protected void showItemChildren(PaymentMethodSearchItem item) {
        setActivityTitle(item.getChildrenHeader());
        populateSearchList(item.getChildren());
    }

    protected void startNextStepForPaymentType(String paymentTypeId) {

        MercadoPago.StartActivityBuilder builder = new MercadoPago.StartActivityBuilder()
                .setActivity(this)
                .setPublicKey(mMerchantPublicKey)
                .setExcludedPaymentMethodIds(mExcludedPaymentMethodIds)
                .setPaymentTypeId(paymentTypeId);

        if(MercadoPagoUtil.isCardPaymentType(paymentTypeId)){
            builder.startGuessingCardActivity();
        }
        else {
            builder.startPaymentMethodsActivity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == MercadoPago.GUESSING_CARD_REQUEST_CODE) {
            resolveGuessingCardRequest(resultCode, data);
        }
        else if (requestCode == MercadoPago.PAYMENT_METHODS_REQUEST_CODE) {
            resolvePaymentMethodsRequest(resultCode, data);
        }
        else if (requestCode == MercadoPago.PAYMENT_VAULT_REQUEST_CODE) {
            resolvePaymentVaultRequest(resultCode, data);
        }
    }

    private void resolvePaymentVaultRequest(int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    protected void resolveGuessingCardRequest(int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            mSelectedPaymentMethod = (PaymentMethod) data.getSerializableExtra("paymentMethod");
            mCardToken = (CardToken) data.getSerializableExtra("cardToken");
            mSelectedIssuer = (Issuer) data.getSerializableExtra("issuer");
            mSelectedPayerCost = (PayerCost) data.getSerializableExtra("payerCost");

            LayoutUtil.showProgressLayout(mActivity);
            mMercadoPago.createToken(mCardToken, new Callback<Token>() {
                @Override
                public void success(Token token, Response response) {
                    finishWithTokenResult(token);
                }

                @Override
                public void failure(RetrofitError error) {
                    mExceptionOnMethod = "getCreateTokenCallback";
                    ApiUtil.finishWithApiException(mActivity, error);
                }
            });

        } else if ((data != null) && (data.getSerializableExtra("apiException") != null)) {
            finishWithApiException(data);
        }
    }

    protected void resolvePaymentMethodsRequest(int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            PaymentMethod paymentMethod = (PaymentMethod) data.getSerializableExtra("paymentMethod");
            finishWithPaymentMethodResult(paymentMethod, "");

        } else {
            if ((data != null) && (data.getSerializableExtra("apiException") != null)) {
                finishWithApiException(data);
            }
        }
    }

    private void finishWithAccountMoneyResult(PaymentMethod paymentMethod, AccountMoneyRequest accountMoneyRequest) {
        LayoutUtil.showRegularLayout(mActivity);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("paymentMethod", paymentMethod);
        returnIntent.putExtra("accountMoneyRequest", accountMoneyRequest);
        this.setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }

    private void finishWithSavedCardResult(PaymentMethod paymentMethod, SavedCardToken savedCardToken, String description) {
        LayoutUtil.showRegularLayout(mActivity);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("paymentMethod", paymentMethod);
        returnIntent.putExtra("paymentMethodInfo", description);
        returnIntent.putExtra("savedCardToken", savedCardToken);
        this.setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }

    protected void finishWithPaymentMethodResult(PaymentMethod paymentMethod, String paymentMethodInfo) {
        LayoutUtil.showRegularLayout(mActivity);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("paymentMethod", paymentMethod);
        returnIntent.putExtra("paymentMethodInfo", paymentMethodInfo);
        this.setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }

    protected void finishWithTokenResult(Token token) {
        LayoutUtil.showRegularLayout(this);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("token", token);
        if (mSelectedIssuer != null) {
            returnIntent.putExtra("issuer", mSelectedIssuer);
        }
        returnIntent.putExtra("payerCost", mSelectedPayerCost);
        returnIntent.putExtra("paymentMethod", mSelectedPaymentMethod);
        this.setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }

    protected void finishWithApiException(Intent data) {
        setResult(Activity.RESULT_CANCELED, data);
        finish();
    }

    protected void setActivityTitle(String title) {
        mActivityTitle.setText(title);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
        if(isItemSelected()) {
            overridePendingTransition(R.anim.slide_left_to_right_in, R.anim.silde_left_to_right_out);
        }
    }
}
