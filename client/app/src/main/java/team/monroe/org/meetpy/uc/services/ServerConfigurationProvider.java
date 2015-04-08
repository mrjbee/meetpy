package team.monroe.org.meetpy.uc.services;

import android.content.Context;

import org.monroe.team.android.box.utils.SerializationMap;

import java.util.UUID;

import team.monroe.org.meetpy.uc.entities.ServerConfiguration;

public class ServerConfigurationProvider {

    private final Context context;
    private final SerializationMap<String,ServerConfiguration> storage;

    public ServerConfigurationProvider(Context context) {
        this.context = context;
        storage = new SerializationMap<>("configurations.map",context);
    }

    public ServerConfiguration store(String alias, String url) {
        String id = UUID.randomUUID().toString();
        ServerConfiguration configuration = new ServerConfiguration(id,alias,url);
        storage.put(configuration.id, configuration);
        storage.persistAll();
        return configuration;
    }


}
