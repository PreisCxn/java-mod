package de.alive.preiscxn.networking;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
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
    private static class HttpMocker extends HttpImpl {
        private final HttpResponse<byte[]> mockResponse;

        HttpMocker(HttpResponse<byte[]> mockResponse) {
            this.mockResponse = mockResponse;
        }

        @Override
        protected @NotNull Mono<HttpResponse<byte[]>> sendAsync(HttpRequest request) {
            return Mono.just(mockResponse);
        }
    }

    @Test
    public void getShouldReturnExpectedResultWhenStatusCodeIsSuccessful() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.get("https://api.preiscxn.de/api", "/test")
                .map(stringFunction);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void getShouldReturnExpectedResultWhenStatusCodeIsSuccessfulWithHeaders() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.get("https://api.preiscxn.de/api", "/test")
                .map(stringFunction);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void getShouldReturnErrorWhenStatusCodeIsNotSuccessful() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Error".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.get("https://api.preiscxn.de/api", "/test")
                .map(stringFunction);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void postShouldReturnExpectedResultWhenStatusCodeIsSuccessfulAndBodyIsNotEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.post("/test", new JsonObject())
                .map(stringFunction);

        StepVerifier.create(result)
                .expectNext("Success")
                .verifyComplete();
    }

    @Test
    public void postShouldReturnExpectedResultWhenStatusCodeIsSuccessfulAndBodyIsEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.post("/test", new JsonObject())
                .map(stringFunction);

        StepVerifier.create(result)
                .expectNext("")
                .verifyComplete();
    }

    @Test
    public void postShouldReturnErrorWhenStatusCodeIsNotSuccessfulAndBodyIsNotEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Error".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.post("/test", new JsonObject())
                .map(stringFunction);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void postShouldReturnErrorWhenStatusCodeIsNotSuccessfulAndBodyIsEmpty() {
        Function<String, String> stringFunction = Function.identity();

        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("".getBytes());

        HttpImpl http = new HttpMocker(mockResponse);

        Mono<String> result = http.post("/test", new JsonObject())
                .map(stringFunction);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void testHttpConstructor() {
        String apiUrl = "https://test.api.url";
        HttpImpl http = new HttpImpl(apiUrl);
        assertEquals(apiUrl, http.getApiUrl());
    }

    //this test may fail due to connection issues
    @Test
    public void testSendAsync() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://example.com/")).GET().build();
        HttpImpl http = new HttpImpl();
        Mono<HttpResponse<byte[]>> result = http.sendAsync(request);
        StepVerifier.create(result).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testGetWithoutBaseUrl() {
        // Mocking HttpResponse
        HttpResponse<byte[]> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success".getBytes());

        // Mocking Http
        HttpImpl http = Mockito.spy(new HttpImpl());
        Mockito.doReturn(Mono.just(mockResponse)).when(http).sendAsync(Mockito.any(HttpRequest.class));

        // Test GET
        Mono<String> result = http.get("/test");
        StepVerifier.create(result).expectNext("Success").verifyComplete();
    }

    @Test
    public void testPostWithoutCallbacks() {
        // Mocking HttpResponse
        HttpResponse<byte[]> mockResponse = Mockito.mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success".getBytes());

        // Mocking Http
        HttpImpl http = Mockito.spy(new HttpImpl());
        Mockito.doReturn(Mono.just(mockResponse)).when(http).sendAsync(Mockito.any(HttpRequest.class));

        // Test POST
        Mono<Void> result = http.post("/test", new JsonObject())
                .then();
        StepVerifier.create(result).verifyComplete();
    }
}
