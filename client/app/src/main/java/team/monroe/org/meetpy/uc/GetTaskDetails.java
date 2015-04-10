package team.monroe.org.meetpy.uc;

import org.monroe.team.android.box.json.Json;
import org.monroe.team.android.box.services.HttpManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;

import java.io.IOException;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;
import team.monroe.org.meetpy.uc.entities.ServerConfiguration;
import team.monroe.org.meetpy.uc.entities.Task;
import team.monroe.org.meetpy.uc.entities.TaskIdentifier;

public class GetTaskDetails extends UserCaseSupport<TaskIdentifier, Task> {

    public GetTaskDetails(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Task executeImpl(TaskIdentifier request) {
        ServerConfiguration server = using(ServerConfigurationProvider.class).get(request.serverId);
        try {
            HttpManager.Response<Json> response = using(HttpManager.class).get(server.append("task", request.taskId), HttpManager.details(), HttpManager.response_json());
            String title = response.body.asObject().asString("title");
            String description = response.body.asObject().asString("description");
            float progress = response.body.asObject().value("progress", Float.class);
            Task.Status status = Task.Status.unknown;
            try {
                status = Task.Status.valueOf(response.body.asObject().asString("status"));
            }catch (Exception e){}

            return new Task(request,title, description, status, progress);
        } catch (HttpManager.InvalidBodyFormatException e) {
            throw new FailExecutionException(e,100);
        } catch (IOException e) {
            throw new FailExecutionException(e,101);
        }

    }
}
