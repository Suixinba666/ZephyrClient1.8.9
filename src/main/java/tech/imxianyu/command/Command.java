package tech.imxianyu.command;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Formatting;
import tech.imxianyu.management.CommandManager;

/**
 * @author ImXianyu
 * @since 6/16/2023 3:18 PM
 */
public abstract class Command {
    /**
     * Minecraft instance.
     */
    public final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private final String name, description, usage;

    /**
     * another name for the command
     */
    @Getter
    private final String[] alias;

    public Command(String name, String description, String usage, String... alias) {
        this.name = name;
        this.description = description;
        this.usage = usage;

        this.alias = alias;

        // automatically adds the command's instance to the command manager
        CommandManager.getCommands().add(this);
    }

    /**
     * execute the command
     * @param args args
     */
    public abstract void execute(String[] args);

    /**
     * prints a string to player's chat hud
     * @param format string format
     * @param args format arguments
     */
    public void print(String format, Object... args) {
        mc.thePlayer.addChatMessage(Formatting.AQUA + "[Zephyr] " + Formatting.RESET + String.format(format, args));
    }

    /**
     * prints the usage to the player's chat hud
     */
    public void printUsage() {
        print(Formatting.RED + "Wrong Usage! Usage: " + Formatting.RESET + this.getUsage());
    }

}
