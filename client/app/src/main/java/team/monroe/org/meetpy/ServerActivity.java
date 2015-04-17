package team.monroe.org.meetpy;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.data.RefreshableCachedData;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import team.monroe.org.meetpy.uc.entities.TaskIdentifier;
import team.monroe.org.meetpy.ui.CircleAppearanceRelativeLayout;
import team.monroe.org.meetpy.ui.PanelUtils;
import team.monroe.org.meetpy.ui.RelativeLayoutHack1;
import team.monroe.org.meetpy.ui.TaskComponent;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.combine;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.xSlide;


public class ServerActivity extends ActivitySupport<AppMeetPy> {

    private boolean awesomeAppearance = true;
    private AppearanceController contentAC;
    private AppearanceController taskContentAC;
    private Representations.Server myServer;

    private GenericListViewAdapter<Representations.Script,GetViewImplementation.ViewHolder<Representations.Script>> scriptListAdapter;
    private ListView scriptsListView;


    private RefreshTimer refreshTaskTimer = new RefreshTimer(3000);
    private List<TaskIdentifier> taskIdentifierList;
    private List<TaskComponent> taskComponentList = new ArrayList<>();
    private RefreshableCachedData<Representations.Scripts> cashedScriptData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crunch_requestNoAnimation();
        setContentView(R.layout.activity_server);

        myServer = getFromIntent("server",null);
        PointF position = getFromIntent("position" , null);
        View baseContainer= view(R.id.smoke_view);

        contentAC = combine(animateAppearance(view(R.id.real_content), xSlide(0f, DisplayUtils.screenWidth(getResources())))
                        .showAnimation(duration_constant(500), interpreter_accelerate_decelerate())
                        .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f)),
                animateAppearance(baseContainer, alpha(1f,0.2f))
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
        PanelUtils.pageHeader(view(R.id.header), myServer.serverAlias, "server");
        view_text(R.id.server_details).setText(myServer.hostDescription);

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

        init_TaskSlideBar();


        if(isFirstRun(savedInstanceState)){
            contentAC.hideWithoutAnimation();
        }else {
            contentAC.showWithoutAnimation();
        }
        taskContentAC.hideWithoutAnimation();

        scriptsListView = view_list(R.id.main_list);
        View header = getLayoutInflater().inflate(R.layout.header_general,null,false);
        ((TextView)header.findViewById(R.id.item_title)).setText("Available scripts");
        scriptsListView.addHeaderView(header,null,false);

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

        cashedScriptData = application().data_scriptsFor(myServer.id);
        cashedScriptData.setObserver(new RefreshableCachedData.DataObserver<Representations.Scripts>() {
            @Override
            public void onData(Representations.Scripts data) {
                scriptListAdapter.clear();
                scriptListAdapter.addAll(data.scriptList);
                scriptListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void init_TaskSlideBar() {
        view(R.id.task_content_panel).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        final float slideWidth = DisplayUtils.screenWidth(getResources()) - DisplayUtils.dpToPx(50, getResources());

        taskContentAC = combine(animateAppearance(view(R.id.task_side_panel), xSlide(0f, slideWidth))
                        .showAnimation(duration_constant(500), interpreter_accelerate_decelerate())
                        .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f)),
                animateAppearance(view(R.id.task_smoke_view), alpha(1f, 0f))
                        .showAnimation(duration_constant(300), interpreter_accelerate(0.8f))
                        .hideAnimation(duration_constant(500), interpreter_decelerate(0.8f))
        );

        view(R.id.task_slide_edge).setOnTouchListener(new SlideTouchGesture(
                slideWidth, SlideTouchGesture.Axis.X) {

            public boolean isClosedAtStart;
            public float startTranslationX = 0;
            @Override
            protected void onStart(float x, float y) {
               isClosedAtStart = isTaskBoardClosed();
               startTranslationX = view(R.id.task_side_panel).getTranslationX();
            }

            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.task_side_panel).setTranslationX(startTranslationX-slideValue);
                if (isClosedAtStart) {
                    view(R.id.task_smoke_view).setAlpha(0.5f * fraction);
                }else{
                    view(R.id.task_smoke_view).setAlpha(1 - 0.5f * fraction);
                }
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                if (isClosedAtStart){
                    taskContentAC.hide();
                }else {
                    taskContentAC.show();
                }
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                if (isClosedAtStart){
                    taskContentAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                        @Override
                        public void customize(Animator animator) {
                            animator.addListener(new AnimatorListenerSupport(){
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    fetchServerTasks(true);
                                }
                            });
                        }
                    });
                }else {
                    taskContentAC.hide();
                }
            }
        });
    }

    private boolean isTaskBoardClosed() {
        return view(R.id.task_side_panel).getTranslationX() != 0;
    }

    private void requestScriptForm(Representations.Script script) {
        Intent intent = new Intent(this, ScripActivity.class);
        intent.putExtra("script",script);
        startActivity(intent);
    }


    private void fetchServerTasks(final boolean forced) {
        application().getServerTasks(myServer.id,new ApplicationSupport.ValueObserver<List<TaskIdentifier>>() {
            @Override
            public void onSuccess(List<TaskIdentifier> tasksList) {
                if (forced || !isTaskBoardClosed()) {
                    updateTasksView(tasksList);
                }
                if (!forced) {
                    refreshTaskTimer.schedule(new Runnable() {
                        @Override
                        public void run() {
                            fetchServerTasks(false);
                        }
                    });
                }
            }

            @Override
            public void onFail(int errorCode) {
                onSuccess(Collections.EMPTY_LIST);
            }
        });
    }

    private void updateTasksView(List<TaskIdentifier> tasksList) {
        taskIdentifierList = tasksList;
        final ViewGroup taskContentPanel = view(R.id.task_content_panel, ViewGroup.class);
        Lists.iterateAndRemove(taskComponentList, new Closure<Iterator<TaskComponent>, Boolean>() {
            @Override
            public Boolean execute(Iterator<TaskComponent> iterator) {
                TaskComponent component = iterator.next();
                if (taskIdentifierList.indexOf(component.getId()) == -1) {
                    component.onDestroy();
                    taskContentPanel.removeView(component.getRootView());
                    iterator.remove();
                } else {
                    taskIdentifierList.remove(component.getId());
                }
                return false;
            }
        });

        for (TaskIdentifier taskIdentifier : taskIdentifierList) {
            TaskComponent component = new TaskComponent(taskIdentifier, application());
            View taskView = component.createView(getLayoutInflater(), taskContentPanel);
            taskContentPanel.addView(taskView);
            component.onCreate();
            taskComponentList.add(component);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun() && awesomeAppearance) {
            contentAC.show();
        }
        refreshTaskTimer.activate();
        cashedScriptData.activateRefreshing();
        fetchServerTasks(false);
    }


    @Override
    protected void onPause() {
        super.onPause();
        refreshTaskTimer.deactivate();
        cashedScriptData.deactivateRefreshing();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateTasksView(Collections.EMPTY_LIST);
        taskContentAC.hide();
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

    public static class RefreshTimer {

        private Timer timer;
        private final long refreshTime;

        public RefreshTimer(long refreshTime) {
            this.refreshTime = refreshTime;
        }

        public synchronized void activate(){
            timer = new Timer();
        }

        public synchronized void deactivate(){
            if (isDeactivated()) return;
            timer.cancel();
            timer.purge();
            timer = null;
        }

        public synchronized void schedule(final Runnable task){
            if (isDeactivated()) return;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    task.run();
                }
            }, refreshTime);
        }

        public boolean isDeactivated() {
            return timer == null;
        }
    }
}
