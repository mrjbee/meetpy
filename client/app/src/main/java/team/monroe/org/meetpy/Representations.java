package team.monroe.org.meetpy;

final public class Representations {

    private Representations() {}

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
