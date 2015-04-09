package team.monroe.org.meetpy.uc;


import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.Script;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

public class GetScriptList extends UserCaseSupport<String, List<Script>> {


    public GetScriptList(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected List<Script> executeImpl(String serverId) {
        ServerConfiguration serverConfiguration = using(ServerConfigurationProvider.class).get(serverId);
        if (serverConfiguration == null){
            return Collections.EMPTY_LIST;
        }
        try {
            HttpManager.Response<Json> response = using(HttpManager.class).get(serverConfiguration.buildUrl("/commands"), HttpManager.details(), HttpManager.json());
            List<Script> answer =new ArrayList<>();
            for (int position = 0; response.body.asArray().exists(position); position++){
               Json.JsonObject object = response.body.asArray().asObject(position);
               answer.add(new Script(
                       serverId, object.asString("id"),
                       object.asString("title"),
                       object.asString("about")
               ));
            }
            return answer;
        } catch (HttpManager.BadUrlException e) {
            throw new IllegalStateException(e);
        } catch (HttpManager.NoRouteToHostException e) {
            throw new FailExecutionException(e,100);
        } catch (HttpManager.InvalidBodyFormatException invalidBodyFormatException) {
            throw new IllegalStateException(invalidBodyFormatException);
        } catch (IOException e) {
            throw new FailExecutionException(e,101);
        }
    }
}
