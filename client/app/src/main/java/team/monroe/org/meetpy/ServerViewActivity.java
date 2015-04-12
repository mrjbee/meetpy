package team.monroe.org.meetpy;

import android.animation.Animator;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.RelativeLayoutExt;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;

import team.monroe.org.meetpy.ui.CircleAppearanceRelativeLayout;
import team.monroe.org.meetpy.ui.RelativeLayoutHack1;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.combine;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.xSlide;


public class ServerViewActivity extends ActivitySupport<AppMeetPy> {

    private boolean awesomeAppearance = true;
    private AppearanceController contentAC;
    private Representations.Server myServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_view);
        myServer = getFromIntent("server",null);
        PointF position = getFromIntent("position" , null);
        View baseContainer= view(R.id.smoke_view);

        contentAC = combine(animateAppearance(view(R.id.real_content), xSlide(0f, DisplayUtils.screenWidth(getResources())))
                        .showAnimation(duration_constant(500), interpreter_accelerate_decelerate())
                        .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f)),
                animateAppearance(baseContainer, alpha(1f,0f))
                        .showAnimation(duration_constant(300), interpreter_accelerate(0.8f))
                        .hideAnimation(duration_constant(500), interpreter_decelerate(0.8f))
        );

        RelativeLayoutHack1 shadow_dog_nail_view = view(R.id.real_content,RelativeLayoutHack1.class);
        shadow_dog_nail_view.hackListener = new RelativeLayoutHack1.TranslationListener() {
            @Override
            public void onX(float value) {
                if (value < DisplayUtils.dpToPx(4,getResources())){
                    view(R.id.shadow_view).setVisibility(View.GONE);
                }else{
                    view(R.id.shadow_view).setVisibility(View.VISIBLE);
                }
            }
        };
        view_text(R.id.page_caption).setText(myServer.serverAlias);
        final float maxSlideValue = DisplayUtils.dpToPx(200, getResources());
        view(R.id.slide_back_stub).setOnTouchListener(new SlideTouchGesture(maxSlideValue, SlideTouchGesture.Axis.X_RIGHT) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.real_content).setTranslationX(maxSlideValue*fraction);
                view(R.id.smoke_view).setAlpha(1-0.5f*fraction);
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                contentAC.show();
                view(R.id.smoke_view).setAlpha(1f);
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                onBackPressed();
            }
        });


        if(isFirstRun(savedInstanceState)){
            contentAC.hideWithoutAnimation();
        }else {
            contentAC.showWithoutAnimation();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun() && awesomeAppearance) {
            contentAC.show();
        }
    }

    @Override
    public void onBackPressed() {
        contentAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AnimatorListenerSupport() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                       onSuperBackPressed();
                    }
                });
            }
        });
    }

    private void onSuperBackPressed() {
        super.onBackPressed();
    }

    private AppearanceControllerBuilder.TypeBuilder<Float> circleGrowing() {
        return new AppearanceControllerBuilder.TypeBuilder<Float>() {
            @Override
            public DefaultAppearanceController.ValueGetter<Float> buildValueGetter() {
                return new DefaultAppearanceController.ValueGetter<Float>() {
                    @Override
                    public Float getShowValue() {
                        return 1f;
                    }

                    @Override
                    public Float getHideValue() {
                        return 0f;
                    }

                    @Override
                    public Float getCurrentValue(View view) {
                        return ((CircleAppearanceRelativeLayout)view).getFraction();
                    }
                };
            }

            @Override
            public AppearanceControllerBuilder.TypedValueSetter<Float> buildValueSetter() {
                return new AppearanceControllerBuilder.TypedValueSetter<Float>(Float.class) {
                    @Override
                    public void setValue(View view, Float value) {
                        ((CircleAppearanceRelativeLayout)view).setFraction(value);
                        view.invalidate();
                    }
                };
            }
        };
    }
}
