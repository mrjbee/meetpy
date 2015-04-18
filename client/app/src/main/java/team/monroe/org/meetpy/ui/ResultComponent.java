package team.monroe.org.meetpy.ui;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.zip.Inflater;

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
    public abstract void onCreate(View view);
    public abstract View inflate(LayoutInflater inflater, ViewGroup parentView);

    public static class Message extends ResultComponent<ScriptAnswer.Message>{

        public Message(ScriptAnswer.Message result) {
            super(result);
        }

        @Override
        public View inflate(LayoutInflater inflater, ViewGroup parentView) {
            return inflater.inflate(R.layout.component_result_type_message,parentView,false);
        }

        @Override
        public void onCreate(View view) {
            view(view,R.id.component_title_text, TextView.class).setText(result.title);
            view(view,R.id.component_value_text, TextView.class).setText(result.text);
        }
    }

    public static class MessageList extends ResultComponent<ScriptAnswer.MessageList>{

        public MessageList(ScriptAnswer.MessageList result) {
            super(result);
        }

        public Button moreButton;
        public ViewGroup listPreviewPanel;

        @Override
        public View inflate(LayoutInflater inflater, ViewGroup parentView) {
            View answer = inflater.inflate(R.layout.component_result_type_list,parentView,false);
            moreButton = view(answer,R.id.component_more_button,Button.class);
            moreButton.setText("Show all "+result.valueList.size()+" ...");
            listPreviewPanel = view(answer,R.id.component_list_preview, ViewGroup.class);
            if (result.valueList.size() > 5){
                moreButton.setVisibility(View.VISIBLE);
            }else{
                moreButton.setVisibility(View.GONE);
            }
            if (result.valueList.isEmpty()){
                View item = inflater.inflate(R.layout.item_result_list,listPreviewPanel,false);
                view(item,R.id.item_title,TextView.class).setText("No data");
                view(item,R.id.item_description,TextView.class).setVisibility(View.GONE);
                listPreviewPanel.addView(item);
            }else{
                for (int i=0;i<result.valueList.size() && i < 6; i++){
                    View item = inflater.inflate(R.layout.item_result_list,listPreviewPanel,false);
                    view(item,R.id.item_title,TextView.class).setText(result.valueList.get(i).first);
                    if (result.valueList.get(i).second == null || result.valueList.get(i).second.isEmpty()) {
                        view(item, R.id.item_description, TextView.class).setVisibility(View.GONE);
                    }else {
                        view(item, R.id.item_description, TextView.class).setText(result.valueList.get(i).second);
                    }
                    listPreviewPanel.addView(item);
                }
            }

            return answer;
        }

        @Override
        public void onCreate(View view) {
            view(view,R.id.component_title_text, TextView.class).setText(result.title);
        }
    }



}
