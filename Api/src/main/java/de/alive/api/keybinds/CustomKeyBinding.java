package de.alive.api.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@SuppressWarnings("unused")
public class CustomKeyBinding {
    /**
     * Printable keys.
     */
    public static final int
            GLFW_KEY_SPACE = 32,
            GLFW_KEY_APOSTROPHE = 39,
            GLFW_KEY_COMMA = 44,
            GLFW_KEY_MINUS = 45,
            GLFW_KEY_PERIOD = 46,
            GLFW_KEY_SLASH = 47,
            GLFW_KEY_0 = 48,
            GLFW_KEY_1 = 49,
            GLFW_KEY_2 = 50,
            GLFW_KEY_3 = 51,
            GLFW_KEY_4 = 52,
            GLFW_KEY_5 = 53,
            GLFW_KEY_6 = 54,
            GLFW_KEY_7 = 55,
            GLFW_KEY_8 = 56,
            GLFW_KEY_9 = 57,
            GLFW_KEY_SEMICOLON = 59,
            GLFW_KEY_EQUAL = 61,
            GLFW_KEY_A = 65,
            GLFW_KEY_B = 66,
            GLFW_KEY_C = 67,
            GLFW_KEY_D = 68,
            GLFW_KEY_E = 69,
            GLFW_KEY_F = 70,
            GLFW_KEY_G = 71,
            GLFW_KEY_H = 72,
            GLFW_KEY_I = 73,
            GLFW_KEY_J = 74,
            GLFW_KEY_K = 75,
            GLFW_KEY_L = 76,
            GLFW_KEY_M = 77,
            GLFW_KEY_N = 78,
            GLFW_KEY_O = 79,
            GLFW_KEY_P = 80,
            GLFW_KEY_Q = 81,
            GLFW_KEY_R = 82,
            GLFW_KEY_S = 83,
            GLFW_KEY_T = 84,
            GLFW_KEY_U = 85,
            GLFW_KEY_V = 86,
            GLFW_KEY_W = 87,
            GLFW_KEY_X = 88,
            GLFW_KEY_Y = 89,
            GLFW_KEY_Z = 90,
            GLFW_KEY_LEFT_BRACKET = 91,
            GLFW_KEY_BACKSLASH = 92,
            GLFW_KEY_RIGHT_BRACKET = 93,
            GLFW_KEY_GRAVE_ACCENT = 96,
            GLFW_KEY_WORLD_1 = 161,
            GLFW_KEY_WORLD_2 = 162;
    private final String translationKey;
    private final int code;
    private final String category;

    public CustomKeyBinding(String translationKey, int code, String category) {
        this.translationKey = translationKey;
        this.code = code;
        this.category = category;
    }

    public KeyBinding getKeybinding() {
        return new KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, category);
    }

    public KeyBinding registerKeybinding(){
        return KeyBindingHelper.registerKeyBinding(getKeybinding());
    }


}
