package com.mercadopago.controllers;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mercadopago.R;
import com.mercadopago.util.CurrenciesUtil;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

/**
 * Created by mreverter on 10/2/16.
 */
public class ShoppingCartViewController {

    private Boolean mStartShowingItemInfo;
    private Activity mActivity;

    private Boolean mItemDescriptionShown;
    private ImageView mImageViewTogglerShoppingCart;
    private MenuItem mMenuItemTogglerShoppingCart;
    private RelativeLayout mItemInfoLayout;
    private View mViewBelowShoppingCart;
    private String mPictureUrl;
    private ImageView mItemImageView;

    public ShoppingCartViewController(Activity activity, MenuItem toggler, String pictureUri, String purchaseTitle, BigDecimal amount, String currencyId, Boolean startShowingItemInfo, View viewBelowShoppingCart) {
        mMenuItemTogglerShoppingCart = toggler;
        initialize(activity, pictureUri, purchaseTitle, amount, currencyId, startShowingItemInfo, viewBelowShoppingCart);
        start();
    }
    public ShoppingCartViewController(Activity activity, ImageView toggler, String pictureUri, String purchaseTitle, BigDecimal amount, String currencyId, Boolean startShowingItemInfo, View viewBelowShoppingCart) {
        mImageViewTogglerShoppingCart = toggler;
        initialize(activity, pictureUri, purchaseTitle, amount, currencyId, startShowingItemInfo, viewBelowShoppingCart);
        start();

    }
    private void initialize(Activity activity, String pictureUri, String purchaseTitle, BigDecimal amount, String currencyId, Boolean startShowingItemInfo, View viewBelowShoppingCart) {
        mActivity = activity;
        mStartShowingItemInfo = startShowingItemInfo;
        mItemInfoLayout = (RelativeLayout) mActivity.findViewById(R.id.itemInfoLayout);
        mItemImageView = (ImageView) mActivity.findViewById(R.id.itemImage);
        mViewBelowShoppingCart = viewBelowShoppingCart;
        mPictureUrl = pictureUri;

        TextView itemDescriptionTextView = (TextView) mActivity.findViewById(R.id.itemTitle);
        TextView itemAmountTextView = (TextView) mActivity.findViewById(R.id.itemAmount);
        itemDescriptionTextView.setText(purchaseTitle);
        itemAmountTextView.setText(getAmountLabel(amount, currencyId));
        showItemImage();
    }

    private void showItemImage() {
        if(mPictureUrl != null && !mPictureUrl.isEmpty()) {
            Picasso.with(mActivity).load(mPictureUrl).into(mItemImageView);
        }
    }

    private void start() {
        if(mStartShowingItemInfo) {
            showItemInfo(false);
        }
        else {
            hideItemInfo();
        }
        tintTogglerDrawableWithColor(mActivity.getResources().getColor(R.color.mpsdk_white));
    }

    public Spanned getAmountLabel(BigDecimal amount, String currencyId) {
        return CurrenciesUtil.formatNumber(amount, currencyId, true, true);
    }

    public void toggle(boolean withAnimation) {
        if(!isItemShown()) {
            showItemInfo(withAnimation);
        }
        else {
            hideItemInfo();
        }
    }
    public boolean isItemShown() {
        return this.mItemDescriptionShown;
    }

    public void hideItemInfo() {
        mItemInfoLayout.setVisibility(View.GONE);
        mItemDescriptionShown = false;
        tintTogglerDrawableWithColor(mActivity.getResources().getColor(R.color.mpsdk_white));
    }

    public void showItemInfo(boolean enableAnimation) {

        mItemInfoLayout.setVisibility(View.VISIBLE);
        if(enableAnimation) {
            enableAnimation();
        }
        mItemDescriptionShown = true;
        tintTogglerDrawableWithColor(mActivity.getResources().getColor(R.color.mpsdk_white));
    }

    private void enableAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation a = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF - mItemInfoLayout.getHeight(), 0);
        a.setDuration(mActivity.getResources().getInteger(android.R.integer.config_shortAnimTime));
        animationSet.addAnimation(a);

        mViewBelowShoppingCart.setAnimation(animationSet);

        mItemInfoLayout.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.slide_up_to_down_in));
    }

    private void tintTogglerDrawableWithColor(int color) {
        Drawable togglerDrawable = getTogglerDrawable();
        if (togglerDrawable != null) {
            togglerDrawable.mutate();
            togglerDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private Drawable getTogglerDrawable() {
        Drawable togglerDrawable = null;
        if(mMenuItemTogglerShoppingCart != null) {
            togglerDrawable = mMenuItemTogglerShoppingCart.getIcon();
        }
        else if(mImageViewTogglerShoppingCart != null) {
            togglerDrawable = mImageViewTogglerShoppingCart.getDrawable();
        }
        return togglerDrawable;
    }
}
