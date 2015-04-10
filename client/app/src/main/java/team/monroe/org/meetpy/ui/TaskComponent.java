package team.monroe.org.meetpy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.app.ApplicationSupport;

import java.util.Timer;
import java.util.TimerTask;

import team.monroe.org.meetpy.AppMeetPy;
import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.entities.Task;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;

public class TaskComponent {

    private final TaskIdentifier taskIdentifier;
    private final AppMeetPy taskDataProvider;

    private ViewGroup root;
    private TextView titleText;
    private Timer refreshTimer;
    private TextView descriptionText;
    private TextView statusText;
    private ProgressBar progressBar;

    public TaskComponent(TaskIdentifier taskIdentifier, AppMeetPy taskDataProvider) {
        this.taskIdentifier = taskIdentifier;
        this.taskDataProvider = taskDataProvider;
    }

    public View createView(LayoutInflater inflater, ViewGroup parent) {
        root = (ViewGroup) inflater.inflate(R.layout.component_task,parent,false);
        return root;
    }

    public void onCreate() {

        titleText = (TextView) root.findViewById(R.id.task_title);
        descriptionText = (TextView) root.findViewById(R.id.task_description);
        statusText = (TextView) root.findViewById(R.id.task_status);
        progressBar = (ProgressBar) root.findViewById(R.id.task_progress);
        progressBar.setProgress(0);
        refreshTimer = new Timer();
        //fetchTask();
    }

    private void fetchTask() {
        taskDataProvider.getTaskData(taskIdentifier, new ApplicationSupport.ValueObserver<Task>() {
            @Override
            public void onSuccess(Task value) {
                titleText.setText(value.title);
                descriptionText.setText(value.description);
                if (value.status == Task.Status.done) {
                    statusText.setVisibility(View.GONE);
                }
                statusText.setText(statusAsString(value));
                progressBar.setProgress(Math.min((int) (value.progress * 100), 100));
                if (value.status != Task.Status.done && value.status != Task.Status.error) {
                    scheduleTaskFetching();
                }
            }

            @Override
            public void onFail(int errorCode) {
                Toast.makeText(taskDataProvider, "Uppps task fetch failed. Error = "+errorCode, Toast.LENGTH_LONG);
                scheduleTaskFetching();
            }
        });
    }

    private String statusAsString(Task value) {
        switch (value.status){
            case done: return "Success";
            case running: return "In progress";
            case error: return "Fail";
            default: return "Unknown";
        }
    }

    private void scheduleTaskFetching() {
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchTask();
            }
        }, 1000);
    }

    public void onDestroy() {
        if (refreshTimer != null){
            refreshTimer.cancel();
            refreshTimer.purge();
        }
    }
}
