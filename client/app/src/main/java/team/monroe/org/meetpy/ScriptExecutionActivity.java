package team.monroe.org.meetpy;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;

import java.util.Map;

import team.monroe.org.meetpy.ui.AnswerFormComponent;
import team.monroe.org.meetpy.ui.ArgumentFormComponent;


public class ScriptExecutionActivity extends ActivitySupport<AppMeetPy> {

    private Representations.Script script;
    private ArgumentFormComponent argumentFormComponent;
    private AnswerFormComponent answerFormComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_execution);
        script = (Representations.Script) getIntent().getSerializableExtra("script");
        view_text(R.id.se_caption).setText(script.scriptTitle);
        view_text(R.id.se_description).setText(script.scriptDescription);
        fetchScriptSignature();
    }

    private void fetchScriptSignature() {
        application().getScriptSignature(script, new ApplicationSupport.ValueObserver<ArgumentFormComponent>() {
            @Override
            public void onSuccess(ArgumentFormComponent argFormView) {
                installArgumentForm(argFormView);
            }

            @Override
            public void onFail(int errorCode) {
                toast_UnsupportedErrorCode(errorCode);
            }
        });
    }

    private void toast_UnsupportedErrorCode(int errorCode) {
        Toast.makeText(application(), "Upps something goes wrong! Error code = " + errorCode, Toast.LENGTH_LONG).show();
    }

    private void installArgumentForm(ArgumentFormComponent formComponent) {
        final ViewGroup content = view(R.id.script_content_panel, ViewGroup.class);
        formComponent.addUI(content, getLayoutInflater(), this);
        content.requestLayout();
        argumentFormComponent = formComponent;
        argumentFormComponent.setSubmitListener(new ArgumentFormComponent.SubmitListener(){
            @Override
            public void onValues(Map<String, Object> data) {
                argumentFormComponent.progress(true);
                argumentFormComponent.userInput(false);
                releasePreviousAnswers();
                executeScript(data);
            }

            @Override
            public void onValueNotSet(String fieldTitle, String fieldId) {
                releasePreviousAnswers();
                Toast.makeText(application(),"Value for '"+fieldTitle+"' not set",Toast.LENGTH_LONG).show();
                argumentFormComponent.highlightComponent(fieldId);
            }
        });
    }

    private void releasePreviousAnswers() {
        if (answerFormComponent != null) {
            answerFormComponent.releaseUI(view(R.id.script_content_panel, ViewGroup.class));
            answerFormComponent = null;
        }
    }

    private void executeScript(Map<String, Object> data) {
        application().executeScript(script, data, new ApplicationSupport.ValueObserver<AnswerFormComponent>() {
            @Override
            public void onSuccess(AnswerFormComponent formComponent) {
                answerFormComponent = formComponent;
                final ViewGroup content = view(R.id.script_content_panel, ViewGroup.class);
                answerFormComponent.addUI(content, getLayoutInflater(), application());
                argumentFormComponent.progress(false);
                argumentFormComponent.userInput(true);
                content.requestLayout();
            }

            @Override
            public void onFail(int errorCode) {
                argumentFormComponent.progress(false);
                argumentFormComponent.userInput(true);
                toast_UnsupportedErrorCode(errorCode);
            }
        });
    }

}
