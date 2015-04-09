package team.monroe.org.meetpy.ui;

import team.monroe.org.meetpy.uc.entities.ScriptArgument;

final public class ArgumentViewBuilder {

    private ArgumentViewBuilder() {}

    public static ArgumentComponent buildFor(ScriptArgument argument) {
        switch (argument.type){
            case text:
                return new ArgumentComponent.Text((ScriptArgument.TextArgument) argument);
            case unknown:
                return new ArgumentComponent.Unknown((ScriptArgument.UnknownTypeArgument) argument);
        }
        throw new IllegalArgumentException("Unsupported type = "+argument.type.name());
    }

}
