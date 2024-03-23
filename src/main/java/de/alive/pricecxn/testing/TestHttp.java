package de.alive.pricecxn.testing;

import de.alive.pricecxn.networking.Http;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;

public class TestHttp {

    private static final @NotNull HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("test");

        Http.POST("/datahandler/auctionhouse", Http.jsonObjectConverter("{\"test\":true}")).doOnSuccess(aVoid -> {
            System.out.println("test");
        });

        System.out.println("test end");
        Thread.sleep(3000);
    }

}
