package team.monroe.org.meetpy.uc.entities;

public class TaskIdentifier {
    public final String serverId;
    public final String taskId;

    public TaskIdentifier(String serverId, String taskId) {
        this.serverId = serverId;
        this.taskId = taskId;
    }
}
