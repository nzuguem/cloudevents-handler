package me.nzuguem.cloudevents;

import java.net.URI;
import java.util.UUID;

import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.inject.Instance;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@RunOnVirtualThread
public class Handler {

    private final Validator validator;
    
    private final ObjectMapper mapper;

    private final Client client;

    public Handler(Validator validator, ObjectMapper mapper, Instance<Client> client) {
        this.validator = validator;
        this.mapper = mapper;
        this.client = client.get();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, JsonFormat.CONTENT_TYPE})
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(CloudEvent event, @Context HttpHeaders httpHeaders, @RestHeader String contentType) {

        try {

            Log.infof("Revcieve Event ID : %s", event.getId());

            this.validateEvent(event);

            this.logHeaders(httpHeaders, contentType);

            Log.infof("Revcieve Event : %s", event);

            var replyEvent = this.toReplyEvent(event);

            this.client.send(replyEvent, this.isStructured(contentType));

            Log.infof("Reply Event : %s", replyEvent);

            return Response.ok(replyEvent).build();

        } catch (Exception e) {
            // https://quarkus.io/guides/rest#exception-mapping
            throw new InternalServerErrorException(e.getMessage(), e);
        }

    }

    private void validateEvent(CloudEvent event) {

        var violations = validator.validate(event);

        if (!violations.isEmpty()) {

            Log.warnf("Validation error %s", violations);

            try {

                // https://quarkus.io/guides/rest#exception-mapping
                throw new BadRequestException(mapper.writeValueAsString(violations));

            } catch (JsonProcessingException e) {

                // https://quarkus.io/guides/rest#exception-mapping
                throw new InternalServerErrorException(e.getMessage(), e);

            }
        }
    }

    private void logHeaders(HttpHeaders httpHeaders, String contentType) {

        if (this.isStructured(contentType)) {
            
            Log.info("CloudEvents : Structured Content mode");

            return;
        }
        
        Log.info("CloudEvents : Binary Content mode");

        Log.infof("ce-id=%s", httpHeaders.getHeaderString("ce-id"));
        Log.infof("ce-source=%s", httpHeaders.getHeaderString("ce-source"));
        Log.infof("ce-specversion=%s", httpHeaders.getHeaderString("ce-specversion"));
        Log.infof("ce-type=%s", httpHeaders.getHeaderString("ce-type"));
        Log.infof("Content-Type=%s", contentType);
    }

    private boolean isStructured(String contentType) {

        return JsonFormat.CONTENT_TYPE.equalsIgnoreCase(contentType);
    }


    private CloudEvent toReplyEvent(CloudEvent baseEvent) {

        return CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("/events/example/reply"))
            .withSubject("reply")
            .withDataContentType(MediaType.APPLICATION_JSON)
            .withType("me.nzuguem.cloudevents.example.reply")
            .withData(baseEvent.getData())
            .build();
    }

    @ServerExceptionMapper
    public RestResponse<String> handleException(Exception exception) {

        return switch (exception) {
            case InternalServerErrorException isee -> RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, isee.getMessage());
            case BadRequestException bre -> RestResponse.status(Response.Status.BAD_REQUEST, bre.getMessage());
            default -> RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        };
    }

}
