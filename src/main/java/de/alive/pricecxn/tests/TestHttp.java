package de.alive.pricecxn.tests;

import de.alive.pricecxn.utils.Http;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestHttp {

    private static final @NotNull HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        System.out.println("test");

        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/datahandler"))
                .GET();
        try {
            client.sendAsync(get.build(), HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                System.out.println(response.body());
            }).join();
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("test end");
    }

}
