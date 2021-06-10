package com.example.MarseilleCapitalist;

import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/Marseille-Capitalist")
public class JerseyConfig extends ResourceConfig{

    public JerseyConfig(){
        register(Webservice.class);
    }
}
