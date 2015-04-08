package team.monroe.org.meetpy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.monroe.team.android.box.app.ActivitySupport;


public class ServerConfigurationActivity extends ActivitySupport<AppMeetPy> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_configuration);
    }
}
