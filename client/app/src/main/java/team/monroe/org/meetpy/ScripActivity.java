package team.monroe.org.meetpy;

import android.animation.Animator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;

import java.util.Map;

import team.monroe.org.meetpy.ui.AnswerFormComponent;
import team.monroe.org.meetpy.ui.ArgumentFormComponent;
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


public class ScripActivity extends ActivitySupport<AppMeetPy> {

    private boolean awesomeAppearance = true;
    private AppearanceController contentAC;
    private Representations.Script script;
    private ArgumentFormComponent argumentFormComponent;
    private AnswerFormComponent answerFormComponent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crunch_requestNoAnimation();
        setContentView(R.layout.activity_script);
        script = getFromIntent("script", null);
        View baseContainer = view(R.id.smoke_view);

        contentAC = combine(animateAppearance(view(R.id.real_content), xSlide(0f, DisplayUtils.screenWidth(getResources())))
                        .showAnimation(duration_constant(500), interpreter_accelerate_decelerate())
                        .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f)),
                animateAppearance(baseContainer, alpha(1f, 0.2f))
                        .showAnimation(duration_constant(300), interpreter_accelerate(0.8f))
                        .hideAnimation(duration_constant(500), interpreter_decelerate(0.8f))
        );

        RelativeLayoutHack1 shadow_dog_nail_view = view(R.id.real_content, RelativeLayoutHack1.class);
        shadow_dog_nail_view.hackListener = new RelativeLayoutHack1.TranslationListener() {
            @Override
            public void onX(float value) {
                if (value < DisplayUtils.dpToPx(4, getResources())) {
                    view(R.id.shadow_view).setVisibility(View.GONE);
                } else {
                    view(R.id.shadow_view).setVisibility(View.VISIBLE);
                }
            }
        };
        view_text(R.id.page_caption).setText(script.scriptTitle);
        view_text(R.id.script_description_text).setText(script.scriptDescription);
        final float maxSlideValue = DisplayUtils.dpToPx(200, getResources());
        view(R.id.slide_back_stub).setOnTouchListener(new SlideTouchGesture(maxSlideValue, SlideTouchGesture.Axis.X_RIGHT) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.real_content).setTranslationX(maxSlideValue * fraction);
                view(R.id.smoke_view).setAlpha(1 - 0.5f * fraction);
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


        if (isFirstRun(savedInstanceState)) {
            contentAC.hideWithoutAnimation();
        } else {
            contentAC.showWithoutAnimation();
        }
        fetchScriptSignature();
    }

    private void fetchScriptSignature() {
        application().getScriptSignature(script, new ApplicationSupport.ValueObserver<ArgumentFormComponent>() {
            @Override
            public void onSuccess(ArgumentFormComponent argFormView) {
                installArgumentForm(argFormView);
            }

            @Override
            public void onFail(int errorCode) {
                toast_UnsupportedErrorCode(errorCode);
            }
        });
    }

    private void toast_UnsupportedErrorCode(int errorCode) {
        Toast.makeText(application(), "Upps something goes wrong! Error code = " + errorCode, Toast.LENGTH_LONG).show();
    }

    private void installArgumentForm(ArgumentFormComponent formComponent) {
        final ViewGroup content = view(R.id.script_content_panel, ViewGroup.class);
        formComponent.addUI(content, getLayoutInflater(), this);
        content.requestLayout();
        argumentFormComponent = formComponent;
        argumentFormComponent.setSubmitListener(new ArgumentFormComponent.SubmitListener(){
            @Override
            public void onValues(Map<String, Object> data) {
                argumentFormComponent.progress(true);
                argumentFormComponent.userInput(false);
                releasePreviousAnswers();
                executeScript(data);
            }

            @Override
            public void onValueNotSet(String fieldTitle, String fieldId) {
                releasePreviousAnswers();
                Toast.makeText(application(),"Value for '"+fieldTitle+"' not set",Toast.LENGTH_LONG).show();
                argumentFormComponent.highlightComponent(fieldId);
            }
        });
    }

    private void releasePreviousAnswers() {
        if (answerFormComponent != null) {
            answerFormComponent.releaseUI(view(R.id.script_content_panel, ViewGroup.class));
            answerFormComponent = null;
        }
    }

    private void executeScript(Map<String, Object> data) {
        application().executeScript(script, data, new ApplicationSupport.ValueObserver<AnswerFormComponent>() {
            @Override
            public void onSuccess(AnswerFormComponent formComponent) {
                answerFormComponent = formComponent;
                final ViewGroup content = view(R.id.script_content_panel, ViewGroup.class);
                answerFormComponent.addUI(content, getLayoutInflater(), application());
                argumentFormComponent.progress(false);
                argumentFormComponent.userInput(true);
                content.requestLayout();
            }

            @Override
            public void onFail(int errorCode) {
                argumentFormComponent.progress(false);
                argumentFormComponent.userInput(true);
                toast_UnsupportedErrorCode(errorCode);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun() && awesomeAppearance) {
            contentAC.show();
        }
     }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        view(R.id.slide_back_stub).setVisibility(View.GONE);
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
