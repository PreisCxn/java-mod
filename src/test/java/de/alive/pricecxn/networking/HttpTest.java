package de.alive.pricecxn.networking;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpTest {
    private static class HttpMocker extends Http {
        private final HttpResponse<String> mockResponse;

        public HttpMocker(HttpResponse<String> mockResponse) {
            super();
            this.mockResponse = mockResponse;
        }

        @Override
        protected Mono<HttpResponse<String>> sendAsync(HttpRequest request) {
            return Mono.just(mockResponse);
        }
    }

    @Test
    public void getShouldReturnExpectedResultWhenStatusCodeIsSuccessful() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.GET("https://api.preiscxn.de/api", "/test", stringFunction);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void getShouldReturnExpectedResultWhenStatusCodeIsSuccessfulWithHeaders() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.GET("https://api.preiscxn.de/api", "/test", stringFunction,"header1", "value1");

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void getShouldReturnErrorWhenStatusCodeIsNotSuccessful() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Error");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.GET("https://api.preiscxn.de/api", "/test", stringFunction);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void postShouldReturnExpectedResultWhenStatusCodeIsSuccessfulAndBodyIsNotEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void postShouldReturnExpectedResultWhenStatusCodeIsSuccessfulAndBodyIsEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction);

        StepVerifier.create(result)
                .expectNext("")
                .verifyComplete();
    }

    @Test
    public void postShouldReturnErrorWhenStatusCodeIsNotSuccessfulAndBodyIsNotEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Error");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void postShouldReturnErrorWhenStatusCodeIsNotSuccessfulAndBodyIsEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void testGetInstance() {
        Http instance = Http.getInstance();
        assertEquals(Http.DEFAULT_API_URL, instance.getApiUrl());
    }

    @Test
    public void testHttpConstructor() {
        String apiUrl = "https://test.api.url";
        Http http = new Http(apiUrl);
        assertEquals(apiUrl, http.getApiUrl());
    }

    //this test may fail due to connection issues
    @Test
    public void testSendAsync() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://example.com/")).GET().build();
        Http http = Http.getInstance();
        Mono<HttpResponse<String>> result = http.sendAsync(request);
        StepVerifier.create(result).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testGetWithoutBaseUrl() {
        // Mocking HttpResponse
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        // Mocking Http
        Http http = Mockito.spy(Http.getInstance());
        Mockito.doReturn(Mono.just(mockResponse)).when(http).sendAsync(Mockito.any(HttpRequest.class));

        // Test GET
        Mono<String> result = http.GET("/test", Function.identity());
        StepVerifier.create(result).expectNext("Success").verifyComplete();
    }

    @Test
    public void testPostWithoutCallbacks() {
        // Mocking HttpResponse
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        // Mocking Http
        Http http = Mockito.spy(Http.getInstance());
        Mockito.doReturn(Mono.just(mockResponse)).when(http).sendAsync(Mockito.any(HttpRequest.class));

        // Test POST
        Mono<Void> result = http.POST("/test", new JsonObject());
        StepVerifier.create(result).verifyComplete();
    }
}