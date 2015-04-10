package team.monroe.org.meetpy.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import team.monroe.org.meetpy.AppMeetPy;
import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;

public class TaskComponent {

    private final TaskIdentifier taskIdentifier;
    private final AppMeetPy taskDataProvider;

    private ViewGroup root;
    private TextView titleText;

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
        titleText.setText(taskIdentifier.taskId);
    }

    public void onDestroy() {

    }
}
