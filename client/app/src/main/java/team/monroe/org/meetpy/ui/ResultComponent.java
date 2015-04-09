package team.monroe.org.meetpy.ui;


import android.view.View;
import android.widget.TextView;

import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.entities.ScriptAnswer;

public abstract class ResultComponent <ResultType extends ScriptAnswer.Result> {

    protected final ResultType result;

    public ResultComponent(ResultType result) {
        this.result = result;
    }

    protected <ViewType extends View> ViewType view(View view, int id, Class<ViewType> requestedClass) {
        return (ViewType) view.findViewById(id);
    }

    public abstract int getLayoutId() ;

    public abstract void onCreate(View view);

    public static class Message extends ResultComponent<ScriptAnswer.Message>{

        public Message(ScriptAnswer.Message result) {
            super(result);
        }

        @Override
        public int getLayoutId() {
            return R.layout.component_result_type_message;
        }

        @Override
        public void onCreate(View view) {
            view(view,R.id.component_title_text, TextView.class).setText(result.title);
            view(view,R.id.component_value_text, TextView.class).setText(result.text);
        }
    }
}
