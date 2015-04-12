package team.monroe.org.meetpy.ui;

import android.view.View;
import android.widget.TextView;

import team.monroe.org.meetpy.R;

public class PanelUtils {
    public static void pageHeader(View view, String caption, String description){
        ((TextView)view.findViewById(R.id.page_caption)).setText(caption);
        ((TextView)view.findViewById(R.id.page_description)).setText(description);
    }
}
