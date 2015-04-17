package team.monroe.org.meetpy;

import org.monroe.team.android.box.app.ApplicationSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

final public class Representations {

    private Representations() {}

    public static final ApplicationSupport.ValueAdapter<ServerConfiguration,Server> ADAPTER_SERVER = new ApplicationSupport.ValueAdapter<ServerConfiguration, Server>() {
        @Override
        public Server adapt(ServerConfiguration value) {
            return new Server(value.id,value.alias, value.url);
        }
    };

    public static class Server implements Serializable {

        public final String id;
        public final String serverAlias;
        public final String hostDescription;

        public Server(String id, String serverAlias, String hostDescription) {
            this.id = id;
            this.serverAlias = serverAlias;
            this.hostDescription = hostDescription;
        }
    }

    public static class Script implements Serializable {

        public final String id;
        public final String serverId;
        public final String scriptTitle;
        public final String scriptDescription;

        public Script(String id, String serverId, String scriptTitle, String scriptDescription) {
            this.id = id;
            this.serverId = serverId;
            this.scriptTitle = scriptTitle;
            this.scriptDescription = scriptDescription;
        }
    }

    public static class Scripts implements Serializable{

        public final String serverId;
        public final List<Script> scriptList;

        public Scripts(String serverId, List<Script> scriptList) {
            this.serverId = serverId;
            this.scriptList = scriptList;
        }
    }


}
