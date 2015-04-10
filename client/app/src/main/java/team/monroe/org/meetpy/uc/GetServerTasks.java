package team.monroe.org.meetpy.uc;

import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.corebox.utils.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;

public class GetServerTasks extends UserCaseSupport<String,List<TaskIdentifier>> {

    public GetServerTasks(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected List<TaskIdentifier> executeImpl(String request) {
        ServerConfiguration serverConfiguration =  using(ServerConfigurationProvider.class).get(request);
        if (serverConfiguration == null)
            throw new FailExecutionException(102);
        try {
           HttpManager.Response<Json> response = using(HttpManager.class).get(
                    serverConfiguration.append("tasks"),
                    HttpManager.details(),
                    HttpManager.response_json());
           List<TaskIdentifier> answer = new ArrayList<>();
           for(int position=0;response.body.asArray().exists(position);position++){
               answer.add(new TaskIdentifier(request,
                       response.body.asArray().asString(position)));
           }
           return answer;
        } catch (HttpManager.InvalidBodyFormatException e) {
           throw new FailExecutionException(e,100);
        } catch (IOException e) {
            throw new FailExecutionException(e,101);
        }
    }
}
