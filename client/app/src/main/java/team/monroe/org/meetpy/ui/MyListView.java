package team.monroe.org.meetpy.ui;

import android.content.Context;
import android.util.AttributeSet;

import org.monroe.team.android.box.app.ui.PushToListView;

public class MyListView extends PushToListView{

    public boolean layoutUpdatingEnabled = true;

    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (layoutUpdatingEnabled) {
            super.onLayout(changed, l, t, r, b);
        }
    }


}
