package team.monroe.org.meetpy;
import org.monroe.team.android.box.app.ApplicationSupport;
import team.monroe.org.meetpy.uc.CreateServerConfiguration;

public class AppMeetPy extends ApplicationSupport<ModelMeetPy> {

    @Override
    protected ModelMeetPy createModel() {
        return new ModelMeetPy(getApplicationContext());
    }

    public void createServerConfiguration(String name, String url, final ValueObserver<Representations.Server> observer) {
        fetchValue(CreateServerConfiguration.class,
            new CreateServerConfiguration.CreateRequest(name, url),
                Representations.ADAPTER_SERVER,
                observer);
    }

}
