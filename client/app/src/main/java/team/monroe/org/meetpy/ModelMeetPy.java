package team.monroe.org.meetpy;

import android.content.Context;

import org.monroe.team.android.box.app.AndroidModel;
import org.monroe.team.corebox.services.ServiceRegistry;

import team.monroe.org.meetpy.services.ServerConfigurationProvider;

public class ModelMeetPy extends AndroidModel{

    public ModelMeetPy(Context context) {
        super("meetpy", context);
    }

    @Override
    protected void constructor(String appName, Context context, ServiceRegistry serviceRegistry) {
        super.constructor(appName, context, serviceRegistry);
        serviceRegistry.registrate(ServerConfigurationProvider.class, new ServerConfigurationProvider(context));
    }
}
