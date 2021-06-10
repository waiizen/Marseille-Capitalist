package com.example.MarseilleCapitalist;


import org.apache.tomcat.util.http.parser.MediaType;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.Response;

@Path("generic")
public class Webservices {
    Services services;

    public void Webservice(){
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces(MediaType.APPLICATION_XML)
    public Response getWorld(){
        return Response.ok(Services.getWorld()).build();
    }

}
