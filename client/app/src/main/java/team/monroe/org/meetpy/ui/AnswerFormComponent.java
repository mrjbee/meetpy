package team.monroe.org.meetpy.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import team.monroe.org.meetpy.AppMeetPy;
import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.entities.ScriptAnswer;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;

public class AnswerFormComponent {

    private final ScriptAnswer answer;
    private final List<ResultComponent> componentList = new ArrayList<>();
    private final List<TaskComponent> taskComponentList = new ArrayList<>();
    private ViewGroup childContainerView;
    private ViewGroup rootView;

    public AnswerFormComponent(ScriptAnswer answer, AppMeetPy app) {
        this.answer = answer;
        for (ScriptAnswer.Result result : answer.resultList) {
            switch (result.type){
                case message:
                    componentList.add(new ResultComponent.Message((ScriptAnswer.Message) result));
                    break;
                case message_list:
                    componentList.add(new ResultComponent.MessageList((ScriptAnswer.MessageList) result));
                    break;
            }
        }

        for (TaskIdentifier taskIdentifier : answer.taskIdentifierList) {
            taskComponentList.add(new TaskComponent(taskIdentifier, app));
        }

    }

    public void addUI(ViewGroup parentView, LayoutInflater inflater, Context context) {
        rootView = (ViewGroup) inflater.inflate(R.layout.item_answer_form, parentView, false);
        parentView.addView(rootView,0);

        childContainerView = (ViewGroup) rootView.findViewById(R.id.form_child_content_panel);
        for (ResultComponent subComponent : componentList) {
            View subComponentView = subComponent.inflate(inflater, childContainerView);
            subComponent.onCreate(subComponentView);
            childContainerView.addView(subComponentView);
        }

        TextView status = (TextView) rootView.findViewById(R.id.form_status_value);
        status.setText(answer.success ? "Success":"Fail");

        ViewGroup taskContainerView = (ViewGroup) rootView.findViewById(R.id.form_child_task_content_panel);
        if (taskComponentList.isEmpty()){
            rootView.findViewById(R.id.form_task_caption).setVisibility(View.GONE);
            taskContainerView.setVisibility(View.GONE);
        }else{
            for (TaskComponent taskComponent : taskComponentList) {
                View taskView = taskComponent.createView(inflater, taskContainerView);
                taskComponent.onCreate();
                taskContainerView.addView(taskView);
            }
        }
    }

    public void releaseUI(ViewGroup parentView) {
        parentView.removeView(rootView);
        for (TaskComponent taskComponent : taskComponentList) {
            taskComponent.onDestroy();
        }

    }
}
