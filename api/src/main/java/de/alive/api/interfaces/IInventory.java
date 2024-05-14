package de.alive.api.interfaces;

public interface IInventory {
    String getTitle();

    int getSize();

    IItemStack getMainHandStack();
}
