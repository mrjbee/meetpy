package team.monroe.org.meetpy;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.app.ui.RelativeLayoutExt;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    private GenericListViewAdapter<Representations.Script,GetViewImplementation.ViewHolder<Representations.Script>> scriptListAdapter;
    private Timer refreshTimer;
    private ListView scriptsListView;


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

        scriptsListView = view_list(R.id.main_list);
        scriptListAdapter = new GenericListViewAdapter<Representations.Script, GetViewImplementation.ViewHolder<Representations.Script>>(getApplicationContext(),new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Representations.Script>>() {
            @Override
            public GetViewImplementation.ViewHolder<Representations.Script> create(final View convertView) {
                return new GetViewImplementation.ViewHolder<Representations.Script>() {

                    TextView valueText = (TextView) convertView.findViewById(R.id.item_value_text);
                    TextView subValueText = (TextView) convertView.findViewById(R.id.item_sub_value_text);

                    @Override
                    public void update(Representations.Script script, int position) {
                        valueText.setText(script.scriptTitle);
                        subValueText.setText(script.scriptDescription);
                    }

                    @Override
                    public void cleanup() {}
                };
            }
        },R.layout.item_script);
        scriptsListView.setAdapter(scriptListAdapter);
        scriptsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Representations.Script script = (Representations.Script) parent.getItemAtPosition(position);
                requestScriptForm(script);
            }
        });

    }

    private void requestScriptForm(Representations.Script script) {
        Intent intent = new Intent(this, ScriptExecutionActivity.class);
        intent.putExtra("script",script);
        startActivity(intent);
    }

    private void fetchServerScripts() {
        application().getScriptListForServer(myServer.id, new ApplicationSupport.ValueObserver<List<Representations.Script>>() {
            @Override
            public void onSuccess(List<Representations.Script> value) {
                scriptListAdapter.clear();
                scriptListAdapter.addAll(value);
                scriptListAdapter.notifyDataSetChanged();
                scheduleNextFetch();
            }

            @Override
            public void onFail(int errorCode) {
                onSuccess(Collections.EMPTY_LIST);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun() && awesomeAppearance) {
            contentAC.show();
        }
        refreshTimer = new Timer(true);
        fetchServerScripts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    private synchronized void stopTimer() {
        refreshTimer.cancel();
        refreshTimer.purge();
        refreshTimer = null;
    }

    private synchronized void  scheduleNextFetch() {
        if (refreshTimer == null)return;
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchServerScripts();
            }
        }, 5000);
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
