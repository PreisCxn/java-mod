package de.alive.pricecxn;

import de.alive.pricecxn.cytooxien.CxnListener;
import net.fabricmc.api.ClientModInitializer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PriceCxnModClient implements ClientModInitializer {

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitializeClient() {

        CxnListener cxnListener = new CxnListener();

    }
}