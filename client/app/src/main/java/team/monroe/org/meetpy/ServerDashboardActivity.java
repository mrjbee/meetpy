package team.monroe.org.meetpy;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.app.ui.PushToActionAdapter;
import org.monroe.team.android.box.app.ui.PushToListView;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.Closure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import team.monroe.org.meetpy.uc.entities.TaskIdentifier;
import team.monroe.org.meetpy.ui.MyListView;
import team.monroe.org.meetpy.ui.PanelUtils;


public class ServerDashboardActivity extends ActivitySupport<AppMeetPy> {

    private GenericListViewAdapter<Representations.Server, GetViewImplementation.ViewHolder<Representations.Server>> serverListAdapter;
    private AppearanceController bodyAC;
    private AppearanceController subBodyAC;
    private AppearanceController addBtnAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_dashboard);
        PanelUtils.pageHeader(view(R.id.header),"MeetPY","remote runner");
        serverListAdapter =
                new GenericListViewAdapter<Representations.Server, GetViewImplementation.ViewHolder<Representations.Server>>(
                        this,
                        new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Representations.Server>>() {
                            @Override
                            public GetViewImplementation.ViewHolder<Representations.Server> create(View convertView) {
                                return new ServerViewHolder(convertView);
                            }
                        },R.layout.item_server
                );

        View header = getLayoutInflater().inflate(R.layout.header_general,null,false);
        ((TextView)header.findViewById(R.id.item_title)).setText("Configured servers");
        view_list(R.id.main_list).addHeaderView(header,null,false);

        view_list(R.id.main_list).setAdapter(serverListAdapter);
        view_list(R.id.main_list).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Representations.Server server = serverListAdapter.getItem(position-1);
                showDetails(server,new PointF(0,0));
            }
        });

        bodyAC = animateAppearance(view(R.id.sd_hidden_space),
                heightSlide(0,(int) DisplayUtils.dpToPx(200f,getResources())))
                .showAnimation(duration_constant(200),interpreter_decelerate(0.8f))
                .hideAnimation(duration_constant(300),interpreter_overshot())
                .build();

        subBodyAC = animateAppearance(view(R.id.sd_sub_body),
                alpha(1f,0f))
                .showAnimation(duration_constant(200),interpreter_decelerate(0.8f))
                .hideAnimation(duration_constant(300),interpreter_accelerate_decelerate())
                .hideAndGone()
                .build();

        addBtnAC = combine(animateAppearance(view(R.id.sd_add_btn),
                        scale(1f, 0f))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_accelerate_decelerate())
                .hideAndGone(),
                animateAppearance(view(R.id.sd_add_btn),
                        rotate(360f,0f))
                        .showAnimation(duration_constant(500),interpreter_overshot())
                        .hideAnimation(duration_constant(200),interpreter_accelerate_decelerate())
                        );

        subBodyAC.hideWithoutAnimation();
        addBtnAC.hideWithoutAnimation();
        bodyAC.showWithoutAnimation();

        view_text(R.id.sc_url_value).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    apply_configuration();
                    handled = true;
                }
                return handled;
            }
        });

        view_button(R.id.sd_add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply_configuration();
            }
        });


        view(R.id.main_list, PushToListView.class).setPushListener(new PushToActionAdapter(DisplayUtils.dpToPx(100f, getResources())) {

            private int initialHeight = 0;
            private View animateView = view(R.id.sd_hidden_space);
            public float lastUsedCoefficient;

            @Override
            protected void beforePush(float x, float y) {
                initialHeight = animateView.getLayoutParams().height;
                lastUsedCoefficient = 0f;
                view(R.id.main_list, MyListView.class).layoutUpdatingEnabled = false;
            }

            @Override
            protected void pushInProgress(float pushCoefficient, float x, float y) {
                if (Math.abs(lastUsedCoefficient - pushCoefficient) > 0.01) {
                    int newHeight = (int) (initialHeight + DisplayUtils.dpToPx(80f, getResources()) * pushCoefficient);
                    animateView.getLayoutParams().height = newHeight;
                    animateView.requestLayout();
                    lastUsedCoefficient = pushCoefficient;
                }
            }

            @Override
            protected void applyPushAction(float x, float y) {
                if (!isSubContentVisible()) {
                    showSubContent();
                } else {
                    hideSubContent();
                }
            }

            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                if (!isSubContentVisible()) {
                    hideSubContent();
                } else {
                    showSubContent();
                }
            }
        });
    }

    private void apply_configuration() {

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view_text(R.id.sc_alias_value).getWindowToken(), 0);

        final String serverName = view_text(R.id.sc_alias_value).getText().toString();
        final String serverUrl = view_text(R.id.sc_url_value).getText().toString();
        if (serverName.isEmpty()){
            Toast.makeText(this, "Please specify server name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (serverUrl.isEmpty()){
            Toast.makeText(this,"Please specify server url", Toast.LENGTH_SHORT).show();
            return;
        }

        addBtnAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AnimatorListenerSupport(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        application().createServerConfiguration(serverName, serverUrl, new ApplicationSupport.ValueObserver<Representations.Server>() {
                            @Override
                            public void onSuccess(Representations.Server server) {
                                hideSubContent();
                                application().data_serverConfigurations().invalidate();
                            }

                            @Override
                            public void onFail(int errorCode) {
                                addBtnAC.show();
                                String reason = "";
                                switch (errorCode) {
                                    case 101:
                                        reason = "Not valid URL";
                                        break;
                                    case 102:
                                        reason = "No route to host";
                                        break;
                                    case 103:
                                        reason = "Can not fetch data";
                                        break;
                                    case 104:
                                        reason = "Server sends bad validation data";
                                        break;
                                    default:
                                        reason = "Unknown error ( code = " + errorCode + ")";
                                        break;
                                }

                                Toast.makeText(application(), "Connection test fails. Because of: " + reason, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private boolean isSubContentVisible() {
        return view(R.id.sd_sub_body).getVisibility() == View.VISIBLE;
    }

    private void showSubContent() {
        bodyAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AnimatorListenerSupport(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        subBodyAC.show();
                        addBtnAC.show();
                        view(R.id.main_list, MyListView.class).layoutUpdatingEnabled = true;
                        view(R.id.main_list, MyListView.class).requestLayout();
                    }
                });
            }
        });
    }

    private void hideSubContent() {
        if (isSubContentVisible()){
            addBtnAC.hide();
            subBodyAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            bodyAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                                @Override
                                public void customize(Animator a) {
                                    a.addListener(new AnimatorListenerSupport(){
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            view(R.id.main_list, MyListView.class).layoutUpdatingEnabled = true;
                                            view(R.id.main_list, MyListView.class).requestLayout();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }else {
            bodyAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view(R.id.main_list, MyListView.class).layoutUpdatingEnabled = true;
                            view(R.id.main_list, MyListView.class).requestLayout();
                        }
                    });
                }
            });

        }
    }


    private void fetchServers() {
        application().data_serverConfigurations().fetch(true,new DataProvider.FetchObserver<ArrayList>() {
            @Override
            public void onFetch(ArrayList arrayList) {
                serverListAdapter.clear();
                serverListAdapter.addAll(arrayList);
                serverListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                forceCloseWithErrorCode(201);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchServers();
        Event.subscribeOnEvent(this,this,DataProvider.INVALID_DATA,new Closure<Class, Void>() {
            @Override
            public Void execute(Class arg) {
                if (arg == ArrayList.class){
                    fetchServers();
                }
                return null;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Event.unSubscribeFromEvents(this,this);
        serverListAdapter.clear();
        serverListAdapter.notifyDataSetChanged();
    }


    private class ServerViewHolder implements GetViewImplementation.ViewHolder<Representations.Server> {

        private final TextView titleText;
        private final TextView descriptionText;
        private final View root;
        private Representations.Server myServer;
        private Timer refreshTimer;
        private TextView showTaskBtn;
        private ImageView imageView;


        private ServerViewHolder(View rootView) {
            root = rootView;
            titleText = (TextView) rootView.findViewById(R.id.item_title);
            descriptionText = (TextView) rootView.findViewById(R.id.item_description);
            showTaskBtn = (TextView) rootView.findViewById(R.id.item_task_button);
            imageView = (ImageView) rootView.findViewById(R.id.item_icon);
        }

        @Override
        public void update(final Representations.Server server, int position) {
            if (server == myServer){
                refreshTimer = new Timer(true);
                updateServerTasks(server);
                return;
            }
            myServer = server;
            titleText.setText(server.serverAlias);
            descriptionText.setText(server.hostDescription);
            showTaskBtn.setVisibility(View.INVISIBLE);
            refreshTimer = new Timer(true);
            imageView.setImageResource(R.drawable.comp_offline);
            updateServerTasks(server);
        }

        @Override
        public void cleanup() {
            if (refreshTimer != null){
                refreshTimer.cancel();
                refreshTimer.purge();
            }
            showTaskBtn.setOnClickListener(null);
        }

        private void updateServerTasks(final Representations.Server server) {
            application().getServerTasks(server.id, new ApplicationSupport.ValueObserver<List<TaskIdentifier>>() {

                @Override
                public void onSuccess(List<TaskIdentifier> taskIdentifierList) {
                    if (server == myServer) {
                        renewTaskComponents(taskIdentifierList);
                        updateServerStatusAndRequest(true, server);
                    }
                }

                @Override
                public void onFail(int errorCode) {
                    if (server == myServer) {
                        renewTaskComponents(Collections.EMPTY_LIST);
                        updateServerStatusAndRequest(false, server);
                    }
                }

            });
        }

        private void renewTaskComponents(final List<TaskIdentifier> taskIdentifierList) {
            showTaskBtn.setText(taskIdentifierList.size()+" task(s)");
            showTaskBtn.setVisibility(taskIdentifierList.isEmpty()?View.INVISIBLE:View.VISIBLE);
            if (taskIdentifierList.isEmpty()){
                showTaskBtn.setOnClickListener(null);
            }else{
                showTaskBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(application(), ServerTaskActivity.class);
                        intent.putExtra("server_id", myServer.id);
                        startActivity(intent);
                    }
                });
            }
        }

        private void updateServerStatusAndRequest(Boolean value, final Representations.Server assertInstance) {
              imageView.setImageResource(value ? R.drawable.comp_online : R.drawable.comp_offline);
              refreshTimer.schedule(new TimerTask() {
                  @Override
                  public void run() {
                      //double check as it`s not UI thread and server might be different
                      if (assertInstance != myServer) return;

                      if (-1 != serverListAdapter.getPosition(assertInstance)) {
                          updateServerTasks(assertInstance);
                          return;
                      } else {
                          runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  cleanup();
                              }
                          });
                      }
                  }
              }, 1000);

        }

    }

    //   showDetails(myServer, new PointF(event.getRawX(), event.getRawY()));

    @Override
    public void onBackPressed() {
        if (isSubContentVisible()){
            hideSubContent();
        }else {
            super.onBackPressed();
        }
    }

    private void showDetails(Representations.Server server, PointF pointF) {
        final Intent intent = new Intent(application(), ServerActivity.class);
        int[] root_location = new int[2];
        view(R.id.header).getLocationOnScreen(root_location);
        pointF.offset(-root_location[0], -root_location[1]);
        intent.putExtra("position", pointF);
        intent.putExtra("server",server);
        startActivity(intent);
    }
}
