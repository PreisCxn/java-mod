package de.alive.scanner.inventory.keybinds;

import de.alive.api.interfaces.IInventory;
import de.alive.api.interfaces.IItemStack;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.keybinds.KeybindExecutor;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.alive.api.LogPrinter.LOGGER;

public class ScanInventoryKeybind implements KeybindExecutor {
    @Override
    public void onKeybindPressed(IMinecraftClient client, IItemStack itemStack) {
        IInventory inventory = client.getInventory();

        if (inventory == null || inventory.getSize() == 1) {
            return;
        }

        scanInventory(inventory, itemStack);
    }

    private void scanInventory(IInventory inventory, IItemStack itemStack) {
        LOGGER.info("Scanning inventory");

        String template = getTemplate();

        String readableName = inventory.getTitle()
                .replaceAll("\\W", "");

        String shortName = readableName.replaceAll("\\s+", "");

        List<Tuple2<String, String>> itemDataAccesses = getItemDataAccesses(shortName, itemStack);

        StringBuilder dataAccesses = new StringBuilder();
        StringBuilder enumValues = new StringBuilder(
                "    "
                + shortName.toUpperCase()
                + "(\"cxnprice.translation."
                + shortName.toLowerCase()
                + ".title\", List.of(\""
                + inventory.getTitle().replaceAll("\"", "\\\"") + "\")),"
                + System.lineSeparator()
        );

        for (Tuple2<String, String> itemDataAccess : itemDataAccesses) {
            String enumName = itemDataAccess.getT1().toUpperCase();
            String s = itemDataAccess.getT1().toLowerCase().replace("_", ".");
            String firstArg = "\"cxnprice.translation." + s + "\"";
            String secondArg = "List.of(\"" + itemDataAccess.getT2() + "\")";

            enumValues
                    .append("    ")
                    .append(enumName)
                    .append("(").append(firstArg)
                    .append(", ")
                    .append(secondArg)
                    .append("),")
                    .append(System.lineSeparator());

            dataAccesses
                    .append("        searchData.put(\"")
                    .append(s)
                    .append("\", ")
                    .append(enumName)
                    .append(");")
                    .append(System.lineSeparator());
        }

        template = template
                .replaceAll("\\{\\{ENUM_VALUES}}", enumValues.toString())
                .replaceAll("\\{\\{DATA_ACCESSES}}", dataAccesses.toString())
                .replaceAll("\\{\\{CLASSNAME}}", shortName)
                .replaceAll("\\{\\{DATA_ACCESS_TITLE}}", shortName.toUpperCase());

        Path path = Path.of("./ScannedInventories/" + shortName + ".java");
        Path parent = path.getParent();
        try {
            if (!Files.exists(parent))
                Files.createDirectories(parent);

            Files.write(path, template.getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write file", e);
        }

    }

    private String getTemplate() {
        try (InputStream resource = this.getClass().getClassLoader().getResourceAsStream("InventoryListenerTemplate.java.example")) {
            if (resource != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            throw new RuntimeException("Template not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Tuple2<String, String>> getItemDataAccesses(String shortName, IItemStack itemStack) {
        List<Tuple2<String, String>> itemDataAccesses = new ArrayList<>();

        List<String> lore = itemStack.getLore();
        for (int i = 0; i < lore.size(); i++) {
            itemDataAccesses.add(Tuples.of(shortName.toUpperCase() + "_ITEM_LORE_" + i, lore.get(i)));
        }

        return itemDataAccesses;
    }
}
