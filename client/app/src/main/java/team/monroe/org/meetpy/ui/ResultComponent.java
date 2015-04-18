package team.monroe.org.meetpy.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;

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

        private AlertDialog dialog;

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
                view(item, R.id.item_separator,View.class).setVisibility(View.VISIBLE);
                view(item, R.id.item_title, TextView.class).setText("No data");
                view(item,R.id.item_description,TextView.class).setVisibility(View.GONE);
                listPreviewPanel.addView(item);
            }else{
                for (int i=0;i<result.valueList.size() && i < 6; i++){
                    View item = inflater.inflate(R.layout.item_result_list,listPreviewPanel,false);
                    view(item, R.id.item_separator,View.class).setVisibility(View.VISIBLE);
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

            if (moreButton.getVisibility() == View.GONE) return;
            Context context = view.getContext();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            GenericListViewAdapter<Pair<String,String>,GetViewImplementation.ViewHolder<Pair<String,String>>> adapter =
                    new GenericListViewAdapter<Pair<String, String>, GetViewImplementation.ViewHolder<Pair<String, String>>>(
                            context,
                            new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Pair<String, String>>>() {
                                @Override
                                public GetViewImplementation.ViewHolder<Pair<String, String>> create(final View convertView) {
                                    return new GetViewImplementation.ViewHolder<Pair<String, String>>() {

                                        TextView title = (TextView) convertView.findViewById(R.id.item_title);
                                        TextView description = (TextView) convertView.findViewById(R.id.item_description);

                                        @Override
                                        public void update(Pair<String, String> valuePair, int position) {
                                                title.setText(valuePair.first);
                                                if (valuePair.second == null || valuePair.second.isEmpty()){
                                                    description.setVisibility(View.GONE);
                                                } else{
                                                    description.setVisibility(View.VISIBLE);
                                                }
                                                description.setText(valuePair.second);
                                        }

                                        @Override
                                        public void cleanup() {}
                                    };
                                }
                            }
                            ,R.layout.item_result_list);

            adapter.addAll(result.valueList);
            dialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialogBuilder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            dialogBuilder.setTitle(result.title);
            dialogBuilder.setCancelable(true);
            dialog = dialogBuilder.create();
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                }
            });
        }
    }



}
