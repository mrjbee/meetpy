package team.monroe.org.meetpy;

import android.os.Bundle;

import org.monroe.team.android.box.app.ActivitySupport;


public class ScriptExecutionActivity extends ActivitySupport<AppMeetPy> {

    private Representations.Script script;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_execution);
        script = (Representations.Script) getIntent().getSerializableExtra("script");
        view_text(R.id.se_caption).setText(script.scriptTitle);
        view_text(R.id.se_description).setText(script.scriptDescription);
        application().getScriptArguments(script);
    }

}
