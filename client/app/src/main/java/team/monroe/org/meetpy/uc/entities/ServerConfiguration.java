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

    public String buildUrl(String uri) {
        return url+uri;
    }

    public String append(String... postfixes) {
        StringBuilder builder = new StringBuilder(url);
        for (String postfix : postfixes) {
            builder.append("/").append(postfix);
        }
        return builder.toString();
    }
}
