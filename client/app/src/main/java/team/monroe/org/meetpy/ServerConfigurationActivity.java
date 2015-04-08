package team.monroe.org.meetpy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.utils.DisplayUtils;


public class ServerConfigurationActivity extends ActivitySupport<AppMeetPy> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_configuration);

        view_text(R.id.sc_url_value).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    apply_configuration();
                    handled = true;
                }
                return handled;
            }
        });

        view_button(R.id.sc_apply_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply_configuration();
            }
        });

    }

    private void apply_configuration() {

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view_text(R.id.sc_alias_value).getWindowToken(), 0);

        String serverName = view_text(R.id.sc_alias_value).getText().toString();
        String serverUrl = view_text(R.id.sc_url_value).getText().toString();
        if (serverName.isEmpty()){
            Toast.makeText(this,"Please specify server name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (serverUrl.isEmpty()){
            Toast.makeText(this,"Please specify server url", Toast.LENGTH_SHORT).show();
            return;
        }

        application().createServerConfiguration(serverName, serverUrl, new ApplicationSupport.ValueObserver<Representations.Server>() {
            @Override
            public void onSuccess(Representations.Server server) {
                finishWithResult(server);
            }

            @Override
            public void onFail(int errorCode) {
                String reason = "";
                switch (errorCode){
                    case 101:
                        reason = "Not valid URL";
                        break;
                    case 102:
                        reason = "No route to host";
                        break;
                    case 103:
                        reason = "Can not fetch data";
                        break;
                    case 104:
                        reason = "Server sends bad validation data";
                        break;
                    default:
                        reason = "Unknown error ( code = "+errorCode+")";
                        break;
                }

                Toast.makeText(application(), "Connection test fails. Because of: "+reason, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishWithResult(Representations.Server server) {
        Intent intent = new Intent();
        intent.putExtra("server_id",server.id);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
