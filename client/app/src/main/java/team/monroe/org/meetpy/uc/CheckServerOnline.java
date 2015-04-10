package team.monroe.org.meetpy.uc;

import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;

import java.io.IOException;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

public class CheckServerOnline extends UserCaseSupport<String, Boolean>{

    public CheckServerOnline(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Boolean executeImpl(String request) {
        ServerConfiguration serverConfiguration = using(ServerConfigurationProvider.class).get(request);
        //TODO: because of debug don`t want to raise an exception here
        if (serverConfiguration == null)return false;
        String url = serverConfiguration.append("version");
        try {
            HttpManager.Response<Json> jsonResponse = using(HttpManager.class).get(url,
                    HttpManager.details(),
                    HttpManager.response_json());

            String version = jsonResponse.body.asObject().value("version", String.class);
            return true;
        } catch (HttpManager.BadUrlException e) {
            throw new UserCaseSupport.FailExecutionException(e,101);
        } catch (HttpManager.NoRouteToHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (HttpManager.InvalidBodyFormatException e) {
            throw new UserCaseSupport.FailExecutionException(e,104);
        }
    }
}
