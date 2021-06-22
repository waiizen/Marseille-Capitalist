package com.example.MarseilleCapitalist;

import com.example.MarseilleCapitalist.generated.PallierType;
import com.example.MarseilleCapitalist.generated.ProductType;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.Response;

@Path("generic")
public class Webservice {
    Service service;

    public Webservice(){
        service = new Service();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        return Response.ok(service.getWorld(username)).build();
    }

    @PUT
    @Path("product")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void putProduct(@Context HttpServletRequest request, @RequestBody ProductType p) {
        String username = request.getHeader("X-user");
        service.updateProduct(username, p);
    }

    @PUT
    @Path("manager")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void putManager(@Context HttpServletRequest request, @RequestBody PallierType newManager) {
        String username = request.getHeader("X-user");
        this.service.updateManager(username, newManager);
    }

}
