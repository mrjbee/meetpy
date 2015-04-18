package team.monroe.org.meetpy.uc.entities;

import android.util.Pair;

import java.util.Collections;
import java.util.List;

public class ScriptAnswer {

    public final boolean success;
    public final List<Result> resultList;
    public final List<TaskIdentifier> taskIdentifierList;

    public ScriptAnswer(boolean success, List<Result> resultList, List<TaskIdentifier> taskIdentifierList) {
        this.success = success;
        this.taskIdentifierList = Collections.unmodifiableList(taskIdentifierList);
        this.resultList = Collections.unmodifiableList(resultList);
    }

    public static class Result {

        public final Type type;

        public Result(Type type) {
            this.type = type;
        }

        public static enum Type{
            unknown, message, message_list
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

    public static class MessageList extends Result {

        public final String title;
        public final List<Pair<String, String>> valueList;

        public MessageList(String title, List<Pair<String,String>> values) {
            super(Type.message_list);
            this.title = title;
            this.valueList = values;
        }
    }
}
