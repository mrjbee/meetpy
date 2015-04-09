package team.monroe.org.meetpy;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.data.DataProvider;

import java.util.ArrayList;
import java.util.List;


public class ScriptSelectionActivity extends ActivitySupport<AppMeetPy> {

    private final int REQUEST_CODE_NEW_SERVER = 100;

    private final static Representations.Server SERVER_NO_SELECTED = new Representations.Server("no_server","No server","Please select or create new one");
    private final static Representations.Server SERVER_CREATE_NEW = new Representations.Server("new_server","New server","Create new configuration ...");

    private final static Representations.Script SCRIPT_SERVER_NOT_SELECTED = new Representations.Script("no_server", "no_server", "Server not selected","Please select server first");
    private final static Representations.Script SCRIPT_SERVER_FETCHING = new Representations.Script("fetching", "fetching", "Loading script list","Please wait while loading script list");

    private GenericListViewAdapter<Representations.Server,GetViewImplementation.ViewHolder<Representations.Server>> serverListAdapter;
    private GenericListViewAdapter<Representations.Script,GetViewImplementation.ViewHolder<Representations.Script>> scriptListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_selection);
        initServerConfigurationSpinner();

        fetchAndSelectServerConfig(null);

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
        scriptListAdapter.add(SCRIPT_SERVER_NOT_SELECTED);
        view_list(R.id.ss_script_list).setAdapter(scriptListAdapter);
        view_list(R.id.ss_script_list).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Representations.Script script = (Representations.Script) parent.getItemAtPosition(position);
                if (script != SCRIPT_SERVER_FETCHING && script != SCRIPT_SERVER_NOT_SELECTED){
                    requestScriptForm(script);
                }
            }
        });
    }

    private void requestScriptForm(Representations.Script script) {
        Intent intent = new Intent(this, ScriptExecutionActivity.class);
        intent.putExtra("script",script);
        startActivity(intent);
    }

    private void initServerConfigurationSpinner() {
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

    private void fetchAndSelectServerConfig(final String serverId) {
        application().data_serverConfigurations().fetch(true,new DataProvider.FetchObserver<ArrayList>() {
            @Override
            public void onFetch(ArrayList arrayList) {
                serverListAdapter.clear();
                serverListAdapter.add(SERVER_NO_SELECTED);
                serverListAdapter.add(SERVER_CREATE_NEW);
                serverListAdapter.addAll(arrayList);
                serverListAdapter.notifyDataSetChanged();
                if (serverId != null){
                    select_server(serverId);
                }
            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                forceCloseWithErrorCode(101);
            }
        });
    }

    private void select_server(String serverId) {
        int position = 0;
        for(;position<serverListAdapter.getCount();position++){
            if (serverListAdapter.getItem(position).id.equals(serverId)){
                view(R.id.ss_server_select_spinner, Spinner.class).setSelection(position);
                return;
            }
        }
    }

    private void server_select(Representations.Server selectedServer) {

        scriptListAdapter.clear();
        scriptListAdapter.add(SCRIPT_SERVER_FETCHING);
        scriptListAdapter.notifyDataSetChanged();

        application().getScriptListForServer(selectedServer.id, new ApplicationSupport.ValueObserver<List<Representations.Script>>() {
            @Override
            public void onSuccess(List<Representations.Script> list) {
                scriptListAdapter.clear();
                scriptListAdapter.addAll(list);
                scriptListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errorCode) {
                String msg = "";
                switch (errorCode){
                    case 100:
                        msg = "Sorry no route to host. Try again later";
                        break;
                    case 101:
                        msg = "Upps, fails during fetch. Try again later";
                        break;
                    default:
                        msg = "Unknown error. Error code = "+errorCode;
                        break;
                }
                Toast.makeText(application(), msg, Toast.LENGTH_LONG).show();
                select_server(SERVER_NO_SELECTED.id);
            }
        });
    }

    private void server_select_create_new() {
        Intent intent = new Intent(getApplicationContext(), ServerConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_SERVER);
    }

    private void server_select_nothing() {
        scriptListAdapter.clear();
        scriptListAdapter.add(SCRIPT_SERVER_NOT_SELECTED);
        scriptListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_NEW_SERVER && data != null){
            fetchAndSelectServerConfig(data.getStringExtra("server_id"));
        } else if(resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_CODE_NEW_SERVER){
            fetchAndSelectServerConfig(SERVER_NO_SELECTED.id);
        }
    }

}
