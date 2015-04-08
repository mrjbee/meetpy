package team.monroe.org.meetpy.uc.entities;

import java.io.Serializable;

public class ServerConfiguration implements Serializable {

    public final String id;
    public final String alias;
    public final String url;

    public ServerConfiguration(String id, String alias, String url) {
        this.id = id;
        this.alias = alias;
        this.url = url;
    }
}
