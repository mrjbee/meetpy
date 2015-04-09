package team.monroe.org.meetpy.uc;

import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;

import java.io.IOException;

import team.monroe.org.meetpy.uc.entities.ServerConfiguration;
import team.monroe.org.meetpy.services.ServerConfigurationProvider;

public class CreateServerConfiguration extends UserCaseSupport<CreateServerConfiguration.CreateRequest, ServerConfiguration>{

    public CreateServerConfiguration(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected ServerConfiguration executeImpl(CreateRequest request) {
        String url = request.url+"/version";
        try {
            HttpManager.Response<Json> jsonResponse = using(HttpManager.class).get(url,
                    HttpManager.details(),
                    HttpManager.response_json());

            String version = jsonResponse.body.asObject().value("version", String.class);

            ServerConfiguration serverConfiguration = using(ServerConfigurationProvider.class).store(request.alias, request.url);
            return serverConfiguration;
        } catch (HttpManager.BadUrlException e) {
            throw new FailExecutionException(e,101);
        } catch (HttpManager.NoRouteToHostException e) {
            throw new FailExecutionException(e,102);
        } catch (IOException e) {
            throw new FailExecutionException(e,103);
        } catch (HttpManager.InvalidBodyFormatException e) {
            throw new FailExecutionException(e,104);
        }
    }

    public static class CreateRequest {

        private final String alias;
        private final String url;

        public CreateRequest(String alias, String url) {
            this.alias = alias;
            this.url = url;
        }
    }
}
