package team.monroe.org.meetpy.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.entities.ScriptAnswer;

public class AnswerFormComponent {

    private final ScriptAnswer answer;
    private final List<ResultComponent> componentList = new ArrayList<>();
    private ViewGroup childContainerView;

    public AnswerFormComponent(ScriptAnswer answer) {
        this.answer = answer;
        for (ScriptAnswer.Result result : answer.resultList) {
            switch (result.type){
                case message:
                    componentList.add(new ResultComponent.Message((ScriptAnswer.Message) result));
            }
        }

    }

    public void addUI(ViewGroup parentView, LayoutInflater inflater, Context context) {
        ViewGroup formView = (ViewGroup) inflater.inflate(R.layout.item_answer_form, parentView, false);
        parentView.addView(formView,0);

        childContainerView = (ViewGroup) formView.findViewById(R.id.form_child_content_panel);
        for (ResultComponent subComponent : componentList) {
            int layoutId = subComponent.getLayoutId();
            View subComponentView = inflater.inflate(layoutId, childContainerView, false);
            subComponent.onCreate(subComponentView);
            childContainerView.addView(subComponentView);
        }

        TextView status = (TextView) formView.findViewById(R.id.form_status_value);
        status.setText(answer.success ? "Success":"Fail");
    }
}
