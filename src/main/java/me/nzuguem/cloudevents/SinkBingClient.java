package me.nzuguem.cloudevents;


import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.arc.lookup.LookupUnlessProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@LookupUnlessProperty(name = "target.service.knative.sinkbinding.mode", stringValue = "false")
public class SinkBingClient extends Client {

    @Override
    protected String baseUrl() {

        // https://knative.dev/docs/eventing/custom-event-source/sinkbinding/
        return ConfigProvider.getConfig()
                .getValue("K_SINK", String.class);
    }
    
    
}
