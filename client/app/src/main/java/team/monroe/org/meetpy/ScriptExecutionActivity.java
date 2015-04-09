package team.monroe.org.meetpy;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;

import java.util.Map;

import team.monroe.org.meetpy.ui.ArgumentFormComponent;


public class ScriptExecutionActivity extends ActivitySupport<AppMeetPy> {

    private Representations.Script script;
    private ArgumentFormComponent argumentFormComponent;

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
                Toast.makeText(application(),"Upps something goes wrong! Error code = "+errorCode, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void installArgumentForm(ArgumentFormComponent formComponent) {
        final ViewGroup content = view(R.id.se_content_panel, ViewGroup.class);
        formComponent.addUI(content, getLayoutInflater(), this);
        argumentFormComponent = formComponent;
        argumentFormComponent.setSubmitListener(new ArgumentFormComponent.SubmitListener(){
            @Override
            public void onValues(Map<String, Object> data) {
                argumentFormComponent.setProgress(true);
            }

            @Override
            public void onValueNotSet(String fieldTitle, String fieldId) {
                Toast.makeText(application(),"Value for '"+fieldTitle+"' not set",Toast.LENGTH_LONG).show();
                argumentFormComponent.highlightComponent(fieldId);
            }
        });
    }

}
