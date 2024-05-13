package me.nzuguem.cloudevents;

import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.logging.Log;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@RegisterRestClient(configKey = "client")
public interface Client {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response sendBinary(CloudEvent event);

    @POST
    @Consumes(JsonFormat.CONTENT_TYPE)
    Response sendStructured(CloudEvent event);


    default CloudEvent send(CloudEventData eventData, boolean isStructured) {

        var event = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("/events/example/reply"))
            .withSubject("reply")
            .withDataContentType(MediaType.APPLICATION_JSON)
            .withType("me.nzuguem.cloudevents.example.reply")
            .withData(eventData)
            .build();
        
        var baseUri = ConfigProvider.getConfig().getValue("quarkus.rest-client.client.url", String.class);

        if (baseUri.contains("ignored")) {

            Log.infof("Sending CloudEvents Ignored: %s", event);
            
            
        } else {

            if (isStructured) {

                this.sendStructured(event);
            } else {
                
                this.sendBinary(event);
            }
        }

        return event;
    }
}
