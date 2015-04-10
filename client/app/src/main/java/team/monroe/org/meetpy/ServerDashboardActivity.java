package team.monroe.org.meetpy;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.data.DataProvider;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class ServerDashboardActivity extends ActivitySupport<AppMeetPy> {

    private GenericListViewAdapter<Representations.Server, GetViewImplementation.ViewHolder<Representations.Server>> serverListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_dashboard);
        serverListAdapter =
                new GenericListViewAdapter<Representations.Server, GetViewImplementation.ViewHolder<Representations.Server>>(
                        this,
                        new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Representations.Server>>() {
                            @Override
                            public GetViewImplementation.ViewHolder<Representations.Server> create(View convertView) {
                                return new ServerViewHolder(convertView);
                            }
                        },R.layout.item_server
                );
        view_list(R.id.sd_main_list).setAdapter(serverListAdapter);
    }


    private void fetchServers() {
        application().data_serverConfigurations().fetch(true,new DataProvider.FetchObserver<ArrayList>() {
            @Override
            public void onFetch(ArrayList arrayList) {
                serverListAdapter.clear();
                serverListAdapter.addAll(arrayList);
                Representations.Server server = (Representations.Server) arrayList.get(0);
                for (int i=0;i<20;i++){
                    boolean useRealUUID = Math.random()>0.5f;
                    serverListAdapter.add(new Representations.Server(
                            useRealUUID?server.id:"debug"+"_"+i,
                            useRealUUID?server.serverAlias+"_REAL":server.serverAlias+"_DEBUG",
                            server.hostDescription+"_"+i
                    ));
                }
                serverListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                forceCloseWithErrorCode(201);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchServers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverListAdapter.clear();
        serverListAdapter.notifyDataSetChanged();
    }

    private class ServerViewHolder implements GetViewImplementation.ViewHolder<Representations.Server> {

        private final TextView titleText;
        private final TextView descriptionText;
        private final TextView statusText;
        private final View root;
        private Representations.Server myServer;
        private Timer refreshTimer;


        private ServerViewHolder(View rootView) {
            root = rootView;
            titleText = (TextView) rootView.findViewById(R.id.item_title);
            descriptionText = (TextView) rootView.findViewById(R.id.item_description);
            statusText = (TextView) rootView.findViewById(R.id.item_status_value);
        }

        @Override
        public void update(final Representations.Server server, int position) {
            myServer = server;
            titleText.setText(server.serverAlias);
            descriptionText.setText(server.hostDescription);
            statusText.setText("");
            refreshTimer = new Timer(true);
            updateServerDetails(server);
        }

        @Override
        public void cleanup() {
            if (refreshTimer != null){
                refreshTimer.cancel();
                refreshTimer.purge();
            }
        }

        private void updateServerDetails(final Representations.Server server) {
            //TODO: Change this on get tasks
            application().isServerOnline(server.id, new ApplicationSupport.ValueObserver<Boolean>(){
                private final Representations.Server requestedServer = server;
                @Override
                public void onSuccess(Boolean value) {
                    updateServerStatusAndRequest(value,server);
                }
                @Override
                public void onFail(int errorCode) {
                   updateServerStatusAndRequest(false,server);
                }
            });
        }

        private void updateServerStatusAndRequest(Boolean value, final Representations.Server checkInstance) {
            if ( checkInstance == myServer){
                  statusText.setText(value?"Online":"Offline");
                  refreshTimer.schedule(new TimerTask() {
                      @Override
                      public void run() {
                          if (checkInstance != myServer) return;
                          if (-1 != serverListAdapter.getPosition(checkInstance)) {
                              updateServerDetails(checkInstance);
                              return;
                          } else {
                              cleanup();
                          }
                      }
                  }, 1000);
            }
        }

    }

}
