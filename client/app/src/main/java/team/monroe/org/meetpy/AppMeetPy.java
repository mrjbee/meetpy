package team.monroe.org.meetpy;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.Lists;

import java.util.ArrayList;
import java.util.List;

import team.monroe.org.meetpy.uc.CreateServerConfiguration;
import team.monroe.org.meetpy.uc.GetScriptList;
import team.monroe.org.meetpy.uc.entities.Script;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;
import team.monroe.org.meetpy.services.ServerConfigurationProvider;

import static team.monroe.org.meetpy.Representations.Server;

public class AppMeetPy extends ApplicationSupport<ModelMeetPy> {

    private DataProvider<ArrayList> serverConfigurationDataProvider;

    @Override
    protected ModelMeetPy createModel() {
        return new ModelMeetPy(getApplicationContext());
    }

    @Override
    protected void onPostCreate() {
        serverConfigurationDataProvider = new DataProvider<ArrayList>(ArrayList.class, model(), getApplicationContext()) {
            @Override
            protected ArrayList provideData() {
                List<ServerConfiguration> serverConfigurationList = model().usingService(ServerConfigurationProvider.class).getAll();
                ArrayList<Server> result = new ArrayList<>(serverConfigurationList.size());
                for (ServerConfiguration serverConfiguration : serverConfigurationList) {
                    result.add(new Server(serverConfiguration.id,serverConfiguration.alias, serverConfiguration.url));
                }
                return result;
            }
        };
    }

    public void createServerConfiguration(String name, String url, final ValueObserver<Server> observer) {
        fetchValue(CreateServerConfiguration.class,
            new CreateServerConfiguration.CreateRequest(name, url),
                Representations.ADAPTER_SERVER,
                observer);
    }

    public void getScriptListForServer(String serverId, final ValueObserver<List<Representations.Script>> observer){
        fetchValue(GetScriptList.class,serverId,new ValueAdapter<List<Script>, List<Representations.Script>>() {
            @Override
            public List<Representations.Script> adapt(List<Script> value) {
                return Lists.collect(value, new Closure<Script, Representations.Script>() {
                    @Override
                    public Representations.Script execute(Script arg) {
                        return new Representations.Script(arg.id, arg.title ,arg.description);
                    }
                });
            }
        },observer);
    }

    public DataProvider<ArrayList> data_serverConfigurations() {
        return serverConfigurationDataProvider;
    }
}
