package team.monroe.org.meetpy;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.app.ui.GenericListViewAdapter;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.data.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import team.monroe.org.meetpy.uc.entities.TaskIdentifier;
import team.monroe.org.meetpy.ui.TaskComponent;


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
        private Button showTaskBtn;


        private ServerViewHolder(View rootView) {
            root = rootView;
            titleText = (TextView) rootView.findViewById(R.id.item_title);
            descriptionText = (TextView) rootView.findViewById(R.id.item_description);
            statusText = (TextView) rootView.findViewById(R.id.item_status_value);
            showTaskBtn = (Button) rootView.findViewById(R.id.item_task_button);
        }

        @Override
        public void update(final Representations.Server server, int position) {
            myServer = server;
            titleText.setText(server.serverAlias);
            descriptionText.setText(server.hostDescription);
            statusText.setText("");
            showTaskBtn.setVisibility(View.INVISIBLE);

            refreshTimer = new Timer(true);
            updateServerTasks(server);
        }

        @Override
        public void cleanup() {
            if (refreshTimer != null){
                refreshTimer.cancel();
                refreshTimer.purge();
            }
        }

        private void updateServerTasks(final Representations.Server server) {
            application().getServerTasks(server.id, new ApplicationSupport.ValueObserver<List<TaskIdentifier>>() {

                @Override
                public void onSuccess(List<TaskIdentifier> taskIdentifierList) {
                    if (server == myServer) {
                        renewTaskComponents(taskIdentifierList);
                        updateServerStatusAndRequest(true, server);
                    }
                }

                @Override
                public void onFail(int errorCode) {
                    if (server == myServer) {
                        renewTaskComponents(Collections.EMPTY_LIST);
                        updateServerStatusAndRequest(false, server);
                    }
                }

            });
        }

        private void renewTaskComponents(List<TaskIdentifier> taskIdentifierList) {
            showTaskBtn.setText(taskIdentifierList.size()+" task(s)");
            showTaskBtn.setVisibility(taskIdentifierList.isEmpty()?View.INVISIBLE:View.VISIBLE);
        }

        private void updateServerStatusAndRequest(Boolean value, final Representations.Server assertInstance) {
              statusText.setText(value?"Online":"Offline");
              refreshTimer.schedule(new TimerTask() {
                  @Override
                  public void run() {
                      //double check as it`s not UI thread and server might be different
                      if (assertInstance != myServer) return;

                      if (-1 != serverListAdapter.getPosition(assertInstance)) {
                          updateServerTasks(assertInstance);
                          return;
                      } else {
                          cleanup();
                      }
                  }
              }, 1000);
        }

    }

}
