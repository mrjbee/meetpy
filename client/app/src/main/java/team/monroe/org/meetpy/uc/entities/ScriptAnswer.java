package team.monroe.org.meetpy.uc.entities;

import java.util.Collections;
import java.util.List;

public class ScriptAnswer {

    public final boolean success;
    public final List<Result> resultList;

    public ScriptAnswer(boolean success, List<Result> resultList) {
        this.success = success;
        this.resultList = Collections.unmodifiableList(resultList);
    }

    public static class Result {

        public final Type type;

        public Result(Type type) {
            this.type = type;
        }

        public static enum Type{
            unknown, message
        }
    }

    public static class Message extends Result {

        public final String title;
        public final String text;

        public Message(String title, String text) {
            super(Type.message);
            this.title = title;
            this.text = text;
        }
    }

}
