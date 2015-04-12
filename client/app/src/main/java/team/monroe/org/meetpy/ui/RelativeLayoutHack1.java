package team.monroe.org.meetpy.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RelativeLayoutHack1 extends RelativeLayout {

    public TranslationListener hackListener;

    public RelativeLayoutHack1(Context context) {
        super(context);
    }

    public RelativeLayoutHack1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutHack1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RelativeLayoutHack1(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        if (hackListener != null){
            hackListener.onX(translationX);
        }
    }


    public static interface TranslationListener{
        public void onX(float value);
    }
}
