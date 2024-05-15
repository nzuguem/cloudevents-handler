package me.nzuguem.cloudevents;


import java.net.URI;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public abstract class Client {

    private final Interface clientInterface;

    protected Client() {
        
        this.clientInterface = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(this.baseUrl()))
            .build(Interface.class);
    }


    protected Response sendBinary(CloudEvent event) {
        return this.clientInterface.sendBinary(event);
    }

    protected Response sendStructured(CloudEvent event) {
        return this.clientInterface.sendStructured(event);
    }

    protected abstract String baseUrl();

    public void send(CloudEvent event , boolean isStructured) {

        if (isStructured) {

            this.sendStructured(event);
        } else {
            
            this.sendBinary(event);
        }
    }

    @Path("/")
    private interface Interface {

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        Response sendBinary(CloudEvent event);

        @POST
        @Consumes(JsonFormat.CONTENT_TYPE)
        Response sendStructured(CloudEvent event);
    
    }
}
