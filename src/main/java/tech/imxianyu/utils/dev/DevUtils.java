package tech.imxianyu.utils.dev;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ImXianyu
 * @since 6/19/2023 10:36 AM
 */
public class DevUtils {

    public static List<Character> missingCharacters = new ArrayList<>();

    public static void addCharacter(char c) {
        missingCharacters.add(c);
        if (Minecraft.getMinecraft().thePlayer != null)
            Minecraft.getMinecraft().thePlayer.addChatMessage("Missing Character: " + c);
    }

    public static void printCurrentInvokeStack() {
        System.out.println("Current Invoke Stack From Thread " + Thread.currentThread().getName() + ":... ");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement s : elements) {
            System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

    public static String getCurrentInvokeStack() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Invoke Stack From Thread ").append(Thread.currentThread().getName()).append(":... ").append("\n");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement s : elements) {
            sb.append("\tat ").append(s.getClassName()).append(".").append(s.getMethodName()).append("(").append(s.getFileName()).append(":").append(s.getLineNumber()).append(")").append("\n");
        }

        return sb.toString();
    }

}
