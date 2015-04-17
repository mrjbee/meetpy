package team.monroe.org.meetpy.ui;

import android.animation.Animator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;

import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.GetScriptSignature;
import team.monroe.org.meetpy.uc.entities.ScriptArgument;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;

public class ArgumentFormComponent {

    private final List<ArgumentComponent> argumentComponentList = new ArrayList<>();
    private final String actionName;
    private ViewGroup childContainerView;
    private SubmitListener submitListener;
    private Button submitButton;
    private ProgressBar progressBar;
    private AppearanceController submitButtonAC;
    private AppearanceController progressBarAC;

    public ArgumentFormComponent(String actionName) {
        this.actionName = actionName;
    }


    public static ArgumentFormComponent createFor(GetScriptSignature.ScriptSignature signature){
        ArgumentFormComponent argumentFormComponent = new ArgumentFormComponent(signature.actionName);
        for (ScriptArgument argument : signature.arguments) {
            ArgumentComponent formView = ArgumentViewBuilder.buildFor(argument);
            argumentFormComponent.argumentComponentList.add(formView);
        }
        return argumentFormComponent;
    }

    public void addUI(ViewGroup parentView, LayoutInflater inflater, Context context) {
        ViewGroup argumentFormView = (ViewGroup) inflater.inflate(R.layout.item_argument_form, parentView, false);
        parentView.addView(argumentFormView,parentView.getChildCount());
        if (argumentComponentList.isEmpty()){
            argumentFormView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            argumentFormView.requestLayout();
        }

        childContainerView = (ViewGroup) argumentFormView.findViewById(R.id.form_child_content_panel);
        for (ArgumentComponent argumentComponent : argumentComponentList) {
            int layoutId = argumentComponent.getLayoutId();
            View argumentView = inflater.inflate(layoutId, childContainerView, false);
            argumentView.setTag(argumentComponent.getId());
            argumentComponent.onCreate(argumentView);
            childContainerView.addView(argumentView);
        }

        progressBar = (ProgressBar) argumentFormView.findViewById(R.id.form_progress);
        submitButton = (Button) argumentFormView.findViewById(R.id.form_submit_button);
        submitButton.setText(actionName);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submitListener == null)return;
                collectDataAndNotify();
            }
        });

        submitButtonAC = animateAppearance(submitButton, scale(1f,0f))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_accelerate(0.8f))
                .hideAndGone()
                .build();

        progressBarAC = animateAppearance(progressBar, scale(1f,0f))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_accelerate(0.8f))
                .hideAndGone()
                .build();

        submitButtonAC.showWithoutAnimation();
        progressBarAC.hideWithoutAnimation();
    }

    private void collectDataAndNotify() {
        Map<String,Object> dataMap = new HashMap<>();
        for (ArgumentComponent argumentComponent : argumentComponentList) {
            try {
                Object value = argumentComponent.getValue();
                dataMap.put(argumentComponent.getId(),value);
            } catch (ArgumentComponent.ValueNotSetException e) {
                submitListener.onValueNotSet(e.title,e.id);
                return;
            }
        }
        submitListener.onValues(dataMap);
    }

    public void setSubmitListener(SubmitListener submitListener) {
        this.submitListener = submitListener;
    }

    public SubmitListener getSubmitListener() {
        return submitListener;
    }

    public void highlightComponent(final String fieldId) {
        final View componentView = findComponentViewById(fieldId);
        if (componentView != null){
            final AppearanceController highlighter = animateAppearance(componentView, scale(1f,0.8f))
                    .showAnimation(duration_constant(300), interpreter_overshot())
                    .hideAnimation(duration_constant(400), interpreter_overshot())
                    .hideAndInvisible()
                    .build();
            highlighter.showWithoutAnimation();
            highlighter.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            highlighter.show();
                        }
                    });
                }
            });

        }
    }

    private View findComponentViewById(String fieldId) {
        View componentView = null;
        for (int i=0; i< childContainerView.getChildCount();i++){
            View probeView = childContainerView.getChildAt(i);
            if (fieldId.equals(probeView.getTag())){
                componentView =probeView;
                break;
            }
        }
        return componentView;
    }

    private ArgumentComponent getComponentById(final String fieldId) {
        return Lists.find(argumentComponentList, new Closure<ArgumentComponent, Boolean>() {
            @Override
            public Boolean execute(ArgumentComponent arg) {
                return arg.getId().equals(fieldId);
            }
        });
    }

    public void progress(boolean progress) {
        if (progress) {
            progressBarAC.hideWithoutAnimation();
            submitButtonAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progressBarAC.show();
                        }
                    });
                }
            });
        } else {
            submitButtonAC.hideWithoutAnimation();
            progressBarAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            submitButtonAC.show();
                        }
                    });
                }
            });
        }
    }

    public void userInput(final boolean enabled) {
        Lists.each(argumentComponentList, new Closure<ArgumentComponent, Void>() {
            @Override
            public Void execute(ArgumentComponent arg) {
                arg.userInput(enabled);
                return null;
            }
        });
    }

    public static interface SubmitListener {
        public void onValues(Map<String,Object> data);
        public void onValueNotSet(String fieldTitle, String fieldId);
    }

}
