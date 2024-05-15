package me.nzuguem.cloudevents;

import org.eclipse.microprofile.config.ConfigProvider;

import io.cloudevents.CloudEvent;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@LookupIfProperty(name = "target.service.knative.sinkbinding.mode", stringValue = "false")
public class DefaultClient extends Client {

    @Override
    protected String baseUrl() {

        return ConfigProvider.getConfig()
                .getOptionalValue("target.service.url", String.class)
                .orElse("http://ignored");
    }

    @Override
    public void send(CloudEvent event , boolean isStructured) {

        if (this.baseUrl().contains("ignored")) {

            Log.info("Sending Reply To Other Service is ignored");
            
            return;
        }

        super.send(event, isStructured);
    }
    
}
