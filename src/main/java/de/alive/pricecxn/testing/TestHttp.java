package de.alive.pricecxn.testing;

import com.google.gson.JsonParser;
import de.alive.pricecxn.networking.Http;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestHttp {

    private static final @NotNull HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("test");

        Http.POST("/datahandler/auctionhouse", Http.JsonObjectConverter("{\"test\":true}")).thenAccept(aVoid -> {
            System.out.println("test");
        });

        System.out.println("test end");
        Thread.sleep(3000);
    }

}
