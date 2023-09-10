package de.alive.pricecxn.cytooxien;

import net.minecraft.item.ItemStack;

import java.util.List;

public class PriceCxnItemStack {
    private static final List<String> TIMESTAMP_SEARCH = List.of( "Ende: " );
    private static final List<String> SELLER_SEARCH = List.of( "Verkäufer: " );
    private static final List<String> BID_SEARCH = List.of( "Gebotsbetrag: " );
    private static final List<String> BUY_SEARCH = List.of( "Sofortkauf: ");

    public PriceCxnItemStack(ItemStack item) {

    }
}
