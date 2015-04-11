package team.monroe.org.meetpy.uc.entities;

public class TaskIdentifier {
    public final String serverId;
    public final String taskId;

    public TaskIdentifier(String serverId, String taskId) {
        this.serverId = serverId;
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskIdentifier that = (TaskIdentifier) o;

        if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null)
            return false;
        if (taskId != null ? !taskId.equals(that.taskId) : that.taskId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serverId != null ? serverId.hashCode() : 0;
        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
        return result;
    }
}
