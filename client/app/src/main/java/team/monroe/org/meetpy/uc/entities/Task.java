package team.monroe.org.meetpy.uc.entities;

public class Task {

    public final TaskIdentifier id;
    public final String title;
    public final String description;
    public final Status status;
    public final float progress;

    public Task(TaskIdentifier id, String title, String description, Status status, float progress) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.progress = progress;
    }

    public static enum Status{
        done, running, unknown, error
    }

}
