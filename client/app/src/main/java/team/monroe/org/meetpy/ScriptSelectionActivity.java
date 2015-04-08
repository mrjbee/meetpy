package team.monroe.org.meetpy;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;


public class ScriptSelectionActivity extends ActivitySupport<AppMeetPy> {

    private final int REQUEST_CODE_NEW_SERVER = 100;

    private final static Representations.Server SERVER_NO_SELECTED = new Representations.Server("no_server","No server","Please select or create new one");
    private final static Representations.Server SERVER_CREATE_NEW = new Representations.Server("no_server","New server","Create new configuration ...");

    private GenericListViewAdapter<Representations.Server,GetViewImplementation.ViewHolder<Representations.Server>> serverListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_selection);
        serverListAdapter = new GenericListViewAdapter<Representations.Server, GetViewImplementation.ViewHolder<Representations.Server>>(getApplicationContext(),new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Representations.Server>>() {
            @Override
            public GetViewImplementation.ViewHolder<Representations.Server> create(final View convertView) {
                return new GetViewImplementation.ViewHolder<Representations.Server>() {

                    TextView valueText = (TextView) convertView.findViewById(R.id.item_value_text);
                    TextView subValueText = (TextView) convertView.findViewById(R.id.item_sub_value_text);

                    @Override
                    public void update(Representations.Server server, int position) {
                        valueText.setText(server.serverAlias);
                        subValueText.setText(server.hostDescription);
                    }

                    @Override
                    public void cleanup() {}
                };
            }
        },R.layout.item_dropdown_list);
        serverListAdapter.add(SERVER_NO_SELECTED);
        serverListAdapter.add(SERVER_CREATE_NEW);
        view(R.id.ss_server_select_spinner, Spinner.class).setAdapter(serverListAdapter);
        view(R.id.ss_server_select_spinner,Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Representations.Server selectedServer = (Representations.Server) parent.getItemAtPosition(position);
                if (SERVER_NO_SELECTED.equals(selectedServer)) {
                    server_select_nothing();
                } else if (SERVER_CREATE_NEW.equals(selectedServer)){
                    server_select_create_new();
                } else {
                    server_select(selectedServer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void server_select(Representations.Server selectedServer) {

    }

    private void server_select_create_new() {
        Intent intent = new Intent(getApplicationContext(), ServerConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_SERVER);
    }

    private void server_select_nothing() {

    }

}
