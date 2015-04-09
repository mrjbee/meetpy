package team.monroe.org.meetpy.uc;

import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.ScriptArgument;
import team.monroe.org.meetpy.uc.entities.ScriptIdentifier;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

public class GetScriptInitialArgList extends UserCaseSupport<ScriptIdentifier, List<ScriptArgument>> {

    public GetScriptInitialArgList(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected List<ScriptArgument> executeImpl(ScriptIdentifier request) {
        ServerConfiguration serverConfiguration = using(ServerConfigurationProvider.class).get(request.serverId);
        if (serverConfiguration == null){
            throw new IllegalStateException("Couldn`t find server by id = "+request.serverId);
        }
        try {
            HttpManager.Response<Json> jsonResponse = using(HttpManager.class).get(
                    serverConfiguration.buildUrl("/command/" + request.scriptId),
                    HttpManager.details(),
                    HttpManager.json());
            List<ScriptArgument> argumentList = new ArrayList<>();

            for (int position = 0; jsonResponse.body.asObject().asArray("args").exists(position); position++){
                Json.JsonObject argumentJson = jsonResponse.body.asObject().asArray("args").asObject(position);
                ScriptArgument argument = parseArgument(argumentJson);
                argumentList.add(argument);
            }

            return argumentList;
        } catch (IOException e) {
            throw new FailExecutionException(e, 100);
        } catch (HttpManager.InvalidBodyFormatException e) {
            throw new FailExecutionException(e, 101);
        }
    }

    private ScriptArgument parseArgument(Json.JsonObject argumentJson) {
        String argId = argumentJson.asString("id");
        String argType = argumentJson.asString("type");
        String argTitle = argumentJson.value("title", "No name");
        String argAbout = argumentJson.value("about", null);
        boolean argRequired = argumentJson.value("required", Boolean.class);
        if ("text".equals(argType)){
            return new ScriptArgument.TextArgument(
                    argId,argType,argTitle,argAbout,argRequired,
                    argumentJson.asString("example")
            );
        }
        return new ScriptArgument.UnknownTypeArgument(
                argId,argType,argTitle,argAbout,argRequired
        );
    }

}
