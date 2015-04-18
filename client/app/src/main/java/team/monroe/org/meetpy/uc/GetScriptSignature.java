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
import team.monroe.org.meetpy.uc.entities.ScriptArgument;
import team.monroe.org.meetpy.uc.entities.ScriptIdentifier;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

public class GetScriptSignature extends UserCaseSupport<ScriptIdentifier,GetScriptSignature.ScriptSignature> {

    public GetScriptSignature(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected ScriptSignature executeImpl(ScriptIdentifier request) {
        ServerConfiguration serverConfiguration = using(ServerConfigurationProvider.class).get(request.serverId);
        if (serverConfiguration == null){
            throw new IllegalStateException("Couldn`t find server by id = "+request.serverId);
        }
        try {
            HttpManager.Response<Json> jsonResponse = using(HttpManager.class).get(
                    serverConfiguration.buildUrl("/command/" + request.scriptId),
                    HttpManager.details(),
                    HttpManager.response_json());
            List<ScriptArgument> argumentList = new ArrayList<>();

            for (int position = 0; jsonResponse.body.asObject().asArray("args").exists(position); position++){
                Json.JsonObject argumentJson = jsonResponse.body.asObject().asArray("args").asObject(position);
                ScriptArgument argument = parseArgument(argumentJson);
                argumentList.add(argument);
            }

            String actionName = "Submit";
            if (jsonResponse.body.asObject().exists("actionName")){
                actionName = jsonResponse.body.asObject().asString("actionName");
                if (actionName == null || actionName.isEmpty()){
                    actionName = "Submit";
                }
            }
            return new ScriptSignature(actionName, argumentList);
        } catch (IOException e) {
            throw new FailExecutionException(e, 100);
        } catch (HttpManager.InvalidBodyFormatException e) {
            throw new FailExecutionException(e, 101);
        }
    }

    private ScriptArgument parseArgument(Json.JsonObject argumentJson) {
        String argId = argumentJson.asString("id");
        ScriptArgument.Type argType = ScriptArgument.Type.unknown;
        try {
            argType = ScriptArgument.Type.valueOf(argumentJson.asString("type"));
        }catch (IllegalArgumentException e){}

        String argTitle = argumentJson.value("title", "No name");
        String argAbout = argumentJson.value("about", null);
        boolean argRequired = argumentJson.value("required", Boolean.class);
        switch (argType){
            case text:
                return new ScriptArgument.TextArgument(
                        argId,argType,argTitle,argAbout,argRequired,
                        argumentJson.asString("example")
                );
            case flag:
                return new ScriptArgument.FlagArgument(
                        argId,argType,argTitle,argAbout,argRequired,
                        argumentJson.value("selected", Boolean.TRUE)
                );
            default:
                return new ScriptArgument.UnknownTypeArgument(
                        argId,argType,argTitle,argAbout,argRequired
                );
        }
    }

    public static class ScriptSignature{

        public final String actionName;
        public final List<ScriptArgument> arguments;

        public ScriptSignature(String actionName, List<ScriptArgument> arguments) {
            this.actionName = actionName;
            this.arguments = Collections.unmodifiableList(arguments);
        }
    }
}
