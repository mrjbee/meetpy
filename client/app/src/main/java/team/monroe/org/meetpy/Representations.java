package team.monroe.org.meetpy;

import org.monroe.team.android.box.app.ApplicationSupport;

import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

final public class Representations {

    private Representations() {}

    public static final ApplicationSupport.ValueAdapter<ServerConfiguration,Server> ADAPTER_SERVER = new ApplicationSupport.ValueAdapter<ServerConfiguration, Server>() {
        @Override
        public Server adapt(ServerConfiguration value) {
            return new Server(value.id,value.alias, value.url);
        }
    };

    public static class Server {

        public final String id;
        public final String serverAlias;
        public final String hostDescription;

        public Server(String id, String serverAlias, String hostDescription) {
            this.id = id;
            this.serverAlias = serverAlias;
            this.hostDescription = hostDescription;
        }
    }
}
