package de.alive.pricecxn.networking;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

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
        public Mono<HttpResponse<String>> sendAsync(HttpRequest request) {
            return Mono.just(mockResponse);
        }
    }
    @Test
    public void getShouldReturnExpectedResultWhenStatusCodeIsSuccessful() {
        Function<String, String> stringFunction = Function.identity();
        Function<String, String> callback = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.GET("https://api.preiscxn.de/api", "/test", stringFunction, callback);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void getShouldReturnErrorWhenStatusCodeIsNotSuccessful() {
        Function<String, String> stringFunction = Function.identity();
        Function<String, String> callback = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Error");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.GET("https://api.preiscxn.de/api", "/test", stringFunction, callback);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void postShouldReturnExpectedResultWhenStatusCodeIsSuccessfulAndBodyIsNotEmpty() {
        Function<String, String> stringFunction = Function.identity();
        Function<String, String> callback = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction, callback);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void postShouldReturnExpectedResultWhenStatusCodeIsSuccessfulAndBodyIsEmpty() {
        Function<String, String> stringFunction = Function.identity();
        Function<String, String> callback = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction, callback);

        StepVerifier.create(result)
                .expectNext("")
                .verifyComplete();
    }

    @Test
    public void postShouldReturnErrorWhenStatusCodeIsNotSuccessfulAndBodyIsNotEmpty() {
        Function<String, String> stringFunction = Function.identity();
        Function<String, String> callback = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Error");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction, callback);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void postShouldReturnErrorWhenStatusCodeIsNotSuccessfulAndBodyIsEmpty() {
        Function<String, String> stringFunction = Function.identity();
        Function<String, String> callback = Function.identity();

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("");

        Http http = new HttpMocker(mockResponse);

        Mono<String> result = http.POST("/test", new JsonObject(), stringFunction, callback);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }}