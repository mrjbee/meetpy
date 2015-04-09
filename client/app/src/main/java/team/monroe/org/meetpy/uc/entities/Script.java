package team.monroe.org.meetpy.uc.entities;

public class Script {

    public final ScriptIdentifier id;
    public final String title;
    public final String description;

    public Script(String serverId, String id, String title, String description) {
        this.id = new ScriptIdentifier(serverId, id);
        this.title = title;
        this.description = description;
    }
}
