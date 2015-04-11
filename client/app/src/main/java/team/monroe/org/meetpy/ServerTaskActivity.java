package team.monroe.org.meetpy;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import team.monroe.org.meetpy.uc.entities.TaskIdentifier;
import team.monroe.org.meetpy.ui.TaskComponent;


public class ServerTaskActivity extends ActivitySupport<AppMeetPy> {

    private String serverId;
    private List<TaskComponent> taskComponentList = new ArrayList<>();
    private ViewGroup taskContentPanel;
    private Timer refreshTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_task);
        serverId = getIntent().getStringExtra("server_id");
        taskContentPanel = view(R.id.st_tasks_content, ViewGroup.class);
    }

    private void fetchServerTasks() {
        application().getServerTasks(serverId, new ApplicationSupport.ValueObserver<List<TaskIdentifier>>() {
            @Override
            public void onSuccess(final List<TaskIdentifier> taskIdentifierList) {

                Lists.iterateAndRemove(taskComponentList, new Closure<Iterator<TaskComponent>, Boolean>() {
                    @Override
                    public Boolean execute(Iterator<TaskComponent> iterator) {
                        TaskComponent component = iterator.next();
                        if (taskIdentifierList.indexOf(component.getId()) == -1){
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
                    TaskComponent component = new TaskComponent(taskIdentifier,application());
                    View taskView = component.createView(getLayoutInflater(),taskContentPanel);
                    taskContentPanel.addView(taskView);
                    component.onCreate();
                    taskComponentList.add(component);
                }

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
        refreshTimer = new Timer(true);
        fetchServerTasks();
        for (TaskComponent component : taskComponentList) {
            component.onResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        for (TaskComponent component : taskComponentList) {
            component.onPause();
        }
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
                fetchServerTasks();
            }
        }, 2000);
    }
}
