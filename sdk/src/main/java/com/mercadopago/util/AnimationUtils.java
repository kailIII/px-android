package com.mercadopago.util;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.mercadopago.R;

/**
 * Created by vaserber on 6/22/16.
 */
public class AnimationUtils {

    public static final int ANIMATION_EXTRA_FACTOR = 3;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void fadeInLollipop(final int color, final ImageView imageView, final Context context) {

        imageView.post(new Runnable() {

            @Override
            public void run() {
                imageView.setColorFilter(ContextCompat.getColor(context, color),
                        PorterDuff.Mode.SRC_ATOP);

                int width = imageView.getWidth();

                Animator anim = ViewAnimationUtils.createCircularReveal(imageView, -width, 0,
                        width, ANIMATION_EXTRA_FACTOR * width);
                anim.setDuration(300);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.start();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void fadeOutLollipop(final int color, final ImageView imageView, final Context context) {
        imageView.post(new Runnable() {

            @Override
            public void run() {

                int width = imageView.getWidth();

                Animator anim = ViewAnimationUtils.createCircularReveal(imageView, -width, 0,
                        ANIMATION_EXTRA_FACTOR * width, width);
                anim.setDuration(300);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageView.setColorFilter(ContextCompat.getColor(context, color),
                                PorterDuff.Mode.SRC_ATOP);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim.start();
            }
        });

    }

    public static void fadeIn(final int color, final ImageView imageView, final Context context) {
        imageView.post(new Runnable() {

            @Override
            public void run() {
                Animation mAnimFadeIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in);
                imageView.setBackgroundColor(ContextCompat.getColor(context, color));
                imageView.startAnimation(mAnimFadeIn);
            }
        });
    }

    public static void fadeOut(final int color, final ImageView imageView, final Context context) {
        imageView.post(new Runnable() {

            @Override
            public void run() {
                Animation mAnimFadeOut = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_out);
                mAnimFadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageView.setBackgroundColor(ContextCompat.getColor(context, color));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(mAnimFadeOut);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setCardColorLollipop(ImageView imageView, Context context, int color) {
        imageView.setColorFilter(ContextCompat.getColor(context, color),
                PorterDuff.Mode.SRC_ATOP);
    }

    public static void setCardColor(ImageView imageView, Context context, int color) {
        imageView.setBackgroundColor(ContextCompat.getColor(context, color));
    }
}
