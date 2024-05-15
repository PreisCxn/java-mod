package de.alive.preiscxn.api.interfaces;

public interface IInventory {
    String getTitle();

    int getSize();

    IItemStack getMainHandStack();
}
