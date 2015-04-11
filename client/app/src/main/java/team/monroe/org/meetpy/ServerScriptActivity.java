package team.monroe.org.meetpy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ServerScriptActivity extends ActivitySupport<AppMeetPy> {

    private String serverId;
    private GenericListViewAdapter<Representations.Script,GetViewImplementation.ViewHolder<Representations.Script>> scriptListAdapter;
    private ViewGroup taskContentPanel;
    private Timer refreshTimer;
    private ListView scriptsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_script);
        serverId = getIntent().getStringExtra("server_id");
        scriptsListView = view_list(R.id.ss2_script_list);
        scriptListAdapter = new GenericListViewAdapter<Representations.Script, GetViewImplementation.ViewHolder<Representations.Script>>(getApplicationContext(),new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Representations.Script>>() {
            @Override
            public GetViewImplementation.ViewHolder<Representations.Script> create(final View convertView) {
                return new GetViewImplementation.ViewHolder<Representations.Script>() {

                    TextView valueText = (TextView) convertView.findViewById(R.id.item_value_text);
                    TextView subValueText = (TextView) convertView.findViewById(R.id.item_sub_value_text);

                    @Override
                    public void update(Representations.Script script, int position) {
                        valueText.setText(script.scriptTitle);
                        subValueText.setText(script.scriptDescription);
                    }

                    @Override
                    public void cleanup() {}
                };
            }
        },R.layout.item_dropdown_list);
        scriptsListView.setAdapter(scriptListAdapter);
        scriptsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Representations.Script script = (Representations.Script) parent.getItemAtPosition(position);
                requestScriptForm(script);
            }
        });
    }

    private void requestScriptForm(Representations.Script script) {
        Intent intent = new Intent(this, ScriptExecutionActivity.class);
        intent.putExtra("script",script);
        startActivity(intent);
    }

    private void fetchServerScripts() {
        application().getScriptListForServer(serverId, new ApplicationSupport.ValueObserver<List<Representations.Script>>() {
            @Override
            public void onSuccess(List<Representations.Script> value) {
                scriptListAdapter.clear();
                scriptListAdapter.addAll(value);
                scriptListAdapter.notifyDataSetChanged();
                scheduleNextFetch();
            }

            @Override
            public void onFail(int errorCode) {
                onSuccess(Collections.EMPTY_LIST);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTimer = new Timer(true);
        fetchServerScripts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    private synchronized void stopTimer() {
        refreshTimer.cancel();
        refreshTimer.purge();
        refreshTimer = null;
    }

    private synchronized void  scheduleNextFetch() {
        if (refreshTimer == null)return;
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchServerScripts();
            }
        }, 5000);
    }
}
