package team.monroe.org.meetpy;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.corebox.app.Model;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import team.monroe.org.meetpy.uc.CreateServerConfiguration;
import team.monroe.org.meetpy.uc.ExecuteScript;
import team.monroe.org.meetpy.uc.GetScriptSignature;
import team.monroe.org.meetpy.uc.GetScriptList;
import team.monroe.org.meetpy.uc.GetTaskDetails;
import team.monroe.org.meetpy.uc.entities.Script;
import team.monroe.org.meetpy.uc.entities.ScriptAnswer;
import team.monroe.org.meetpy.uc.entities.ScriptArgument;
import team.monroe.org.meetpy.uc.entities.ScriptIdentifier;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;
import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.Task;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;
import team.monroe.org.meetpy.ui.AnswerFormComponent;
import team.monroe.org.meetpy.ui.ArgumentFormComponent;

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


    public void getScriptListForServer(final String serverId, final ValueObserver<List<Representations.Script>> observer){
        fetchValue(GetScriptList.class,serverId,new ValueAdapter<List<Script>, List<Representations.Script>>() {
            @Override
            public List<Representations.Script> adapt(List<Script> value) {
                return Lists.collect(value, new Closure<Script, Representations.Script>() {
                    @Override
                    public Representations.Script execute(Script arg) {
                        return new Representations.Script(arg.id.scriptId, arg.id.serverId, arg.title ,arg.description);
                    }
                });
            }
        },observer);
    }

    public void getScriptSignature(Representations.Script script, ValueObserver<ArgumentFormComponent> observer) {
        fetchValue(GetScriptSignature.class,new ScriptIdentifier(script.serverId,script.id), new ValueAdapter<List<ScriptArgument>, ArgumentFormComponent>() {
            @Override
            public ArgumentFormComponent adapt(List<ScriptArgument> arguments) {
                return ArgumentFormComponent.createFor(arguments);
            }
        }, observer);
    }

    public DataProvider<ArrayList> data_serverConfigurations() {
        return serverConfigurationDataProvider;
    }

    public void executeScript(Representations.Script script, Map<String, Object> data, ValueObserver<AnswerFormComponent> observer) {
        fetchValue(ExecuteScript.class,
                new ExecuteScript.ExecutionRequest(new ScriptIdentifier(script.serverId, script.id), data), new ValueAdapter<ScriptAnswer, AnswerFormComponent>() {
                    @Override
                    public AnswerFormComponent adapt(ScriptAnswer value) {
                        return new AnswerFormComponent(value, AppMeetPy.this);
                    }
                }, observer);
    }

    public void getTaskData(TaskIdentifier taskIdentifier, ValueObserver<Task> observer) {
        fetchValue(GetTaskDetails.class, taskIdentifier,new NoOpValueAdapter<Task>(),observer);
    }
}
