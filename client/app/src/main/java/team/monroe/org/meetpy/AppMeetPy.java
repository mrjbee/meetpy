package team.monroe.org.meetpy;

import org.monroe.team.android.box.app.ApplicationSupport;

public class AppMeetPy extends ApplicationSupport<ModelMeetPy> {

    @Override
    protected ModelMeetPy createModel() {
        return new ModelMeetPy(getApplicationContext());
    }
}
