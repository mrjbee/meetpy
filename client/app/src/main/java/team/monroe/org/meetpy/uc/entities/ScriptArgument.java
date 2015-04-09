package team.monroe.org.meetpy.uc.entities;

public abstract class ScriptArgument {

    public final String id;
    public final String type;
    public final String title;
    public final String about;
    public final boolean required;

    public ScriptArgument(String id, String type, String title, String about, boolean required) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.about = about;
        this.required = required;
    }


    public static class UnknownTypeArgument extends ScriptArgument{
        public UnknownTypeArgument(String id, String type, String title, String about, boolean required) {
            super(id, type, title, about, required);
        }
    }

    public static class TextArgument extends ScriptArgument{

        private final String example;

        public TextArgument(String id, String type, String title, String about, boolean required, String example) {
            super(id, type, title, about, required);
            this.example = example;
        }
    }
}

