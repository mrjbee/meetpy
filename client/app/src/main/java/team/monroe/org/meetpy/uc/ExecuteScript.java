package team.monroe.org.meetpy.uc;

import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.json.JsonBuilder;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.ScriptAnswer;
import team.monroe.org.meetpy.uc.entities.ScriptIdentifier;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;

public class ExecuteScript extends UserCaseSupport<ExecuteScript.ExecutionRequest, ScriptAnswer>{

    public ExecuteScript(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected ScriptAnswer executeImpl(ExecutionRequest request) {
        JsonBuilder.Object argObject = JsonBuilder.object();
        for (Map.Entry<String, Object> entry : request.arguments.entrySet()) {
            argObject.field(entry.getKey(), entry.getValue());
        }

        Json json = JsonBuilder.build(JsonBuilder
                .object()
                    .field("command_id",request.identifier.scriptId)
                    .field("args", argObject));
        ServerConfiguration serverConfiguration = using(ServerConfigurationProvider.class).get(request.identifier.serverId);
        try {
          HttpManager.Response<Json> response = using(HttpManager.class).post(serverConfiguration.buildUrl("/tasks"),
                    HttpManager.request_json(json),
                    HttpManager.details(),
                    HttpManager.response_json());
          return extractResult(request.identifier.serverId, response.body);
        } catch (HttpManager.InvalidBodyFormatException e) {
            throw new FailExecutionException(e,100);
        } catch (IOException e) {
            throw new FailExecutionException(e,101);
        }
    }

    private ScriptAnswer extractResult(String serverId, Json json) {
        boolean success = json.asObject().value("success", Boolean.class);
        List<ScriptAnswer.Result> resultList = new ArrayList<>();
        if(json.asObject().exists("results")) {
            for (int position = 0; json.asObject().asArray("results").exists(position); position++) {
                Json.JsonObject resultObject = json.asObject().asArray("results").asObject(position);
                String typeString = resultObject.asString("type");
                ScriptAnswer.Result.Type type = ScriptAnswer.Result.Type.unknown;
                try {
                    type = ScriptAnswer.Result.Type.valueOf(typeString);
                }catch (Exception e){}
                switch (type){
                    case message:
                        resultList.add(new ScriptAnswer.Message(
                                resultObject.asString("title"),
                                resultObject.asString("value")));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported type = "+typeString);
                }
            }

        }
        List<TaskIdentifier> taskIdentifierList = new ArrayList<>();
        if(json.asObject().exists("sub_tasks")) {
            for (int position = 0; json.asObject().asArray("sub_tasks").exists(position); position++) {
                String taskId = json.asObject().asArray("sub_tasks").asString(position);
                taskIdentifierList.add(new TaskIdentifier(serverId,taskId));
            }
        }
        return new ScriptAnswer(success, resultList, taskIdentifierList);
    }

    public static class ExecutionRequest{

        private final ScriptIdentifier identifier;
        private final Map<String,Object> arguments;

        public ExecutionRequest(ScriptIdentifier identifier, Map<String, Object> arguments) {
            this.identifier = identifier;
            this.arguments = arguments;
        }
    }

}
