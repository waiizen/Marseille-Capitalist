package com.example.MarseilleCapitalist;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/Marseille-Capitalist")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(){
        register(Webservices.class);
        register(RequestFilter.CORSResponseFilter.class);
    }

}
