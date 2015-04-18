package team.monroe.org.meetpy.ui;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import team.monroe.org.meetpy.R;
import team.monroe.org.meetpy.uc.entities.ScriptArgument;

public abstract class ArgumentComponent<ArgumentType extends ScriptArgument> {

    protected final ArgumentType arg;

    public ArgumentComponent(ArgumentType scriptArgument) {
        this.arg = scriptArgument;
    }


    protected <ViewType extends View> ViewType view(View view, int id, Class<ViewType> requestedClass) {
        return (ViewType) view.findViewById(id);
    }

    protected ValueNotSetException generateValueNotSetException() {
        return new ValueNotSetException(arg.id,arg.title);
    }

    public abstract int getLayoutId() ;

    public abstract void onCreate(View view);

    public abstract Object getValue() throws ValueNotSetException;

    public String getId() {
        return arg.id;
    }

    public abstract void userInput(boolean enabled);


    public static class Text extends ArgumentComponent<ScriptArgument.TextArgument> {

        EditText valueEdit;

        public Text(ScriptArgument.TextArgument scriptArgument) {
            super(scriptArgument);
        }


        @Override
        public int getLayoutId() {
            return R.layout.component_type_text;
        }

        @Override
        public void onCreate(View view) {
            valueEdit = view(view, R.id.component_value_edit, EditText.class);
            if (arg.example != null && !arg.example.isEmpty()){
                String text = arg.title + ", e.g. "+arg.example;
                SpannableString spannableString = new SpannableString(text);

                /*
                int startIndex = arg.title.length();
                int endIndex = text.length();
                spannableString.setSpan(new StyleSpan(Typeface.ITALIC), startIndex,endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                */
                int startIndex = text.length()- arg.example.length();
                int endIndex = startIndex + arg.example.length();
                spannableString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), startIndex,endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                valueEdit.setHint(spannableString);
            }else{
                valueEdit.setHint(arg.title);
            }
            view(view, R.id.component_description_text, TextView.class).setText(arg.about);
        }

        @Override
        public Object getValue() throws ValueNotSetException {
            if (arg.required && valueEdit.getText().toString().isEmpty())
                throw generateValueNotSetException();
            return valueEdit.getText().toString();
        }

        @Override
        public void userInput(boolean enabled) {
            valueEdit.setEnabled(enabled);
        }
    }

    public static class Unknown extends ArgumentComponent<ScriptArgument.UnknownTypeArgument> {

        public Unknown(ScriptArgument.UnknownTypeArgument scriptArgument) {
            super(scriptArgument);
        }

        @Override
        public int getLayoutId() {
            return R.layout.component_unknown_arg_type;
        }

        @Override
        public void onCreate(View view) {
            view(view, R.id.component_title_text, TextView.class).setText(arg.title);
            view(view, R.id.component_description_text, TextView.class).setText(arg.about);
        }

        @Override
        public Object getValue() throws ValueNotSetException {
            if (arg.required) throw generateValueNotSetException();
            return null;
        }

        @Override
        public void userInput(boolean enabled) {}

    }

    public static class ValueNotSetException extends Exception{

        public final String id;
        public final String title;

        public ValueNotSetException(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }

}
