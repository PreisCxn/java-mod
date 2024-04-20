package de.alive.pricecxn.networking.cdn;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.alive.pricecxn.networking.Http;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ModDeliveryAgentTest {

    @Mock
    private Http http;

    private ModDeliveryAgent modDeliveryAgent;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        modDeliveryAgent = new ModDeliveryAgent(http);
    }

    @Test
    public void getModVersions_returnsExpectedVersions() {
        JsonArray jsonArray = JsonParser.parseString("[\"1.0\", \"1.1\", \"1.2\"]").getAsJsonArray();
        when(http.GET(any(), any(), any(), any())).thenReturn(Mono.just(jsonArray));

        StepVerifier.create(modDeliveryAgent.getModVersions())
                .expectNext(List.of("1.0", "1.1", "1.2"))
                .verifyComplete();
    }

    @Test
    public void getModVersions_returnsEmptyListWhenNoVersions() {
        when(http.GET(any(), any(), any(), any())).thenReturn(Mono.just(new JsonArray()));

        StepVerifier.create(modDeliveryAgent.getModVersions())
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    public void getNewestVersion_returnsExpectedVersion() {
        when(http.GET(any(), any(), any(), any())).thenReturn(Mono.just("1.2"));

        StepVerifier.create(modDeliveryAgent.getNewestVersion())
                .expectNext("1.2")
                .verifyComplete();
    }

    @Test
    public void getNewestVersion_returnsEmptyStringWhenNoVersion() {
        when(http.GET(any(), any(), any(), any())).thenReturn(Mono.just(""));

        StepVerifier.create(modDeliveryAgent.getNewestVersion())
                .expectNext("")
                .verifyComplete();
    }
}