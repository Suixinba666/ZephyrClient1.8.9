package tech.imxianyu.management;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Formatting;
import tech.imxianyu.command.Command;
import tech.imxianyu.command.impl.*;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.ChatEvent;
import tech.imxianyu.interfaces.AbstractManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ImXianyu
 * @since 6/16/2023 3:22 PM
 */
public class CommandManager extends AbstractManager {

    @Getter
    private static final List<Command> commands = new ArrayList<>();
    @Handler
    public void onChat(ChatEvent event) {
        String unformatted = event.getMsg();

        if (!unformatted.startsWith("."))
            return;

        if (ModuleManager.noCommand.isEnabled())
            return;

        event.setCancelled();

        unformatted = unformatted.substring(1);


        String[] split = unformatted.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("\"", "");
        }

        String commandName = split[0];

        boolean foundCommand = false;
        for (Command command : commands) {
            if (commandName.equalsIgnoreCase(command.getName()) || Arrays.asList(command.getAlias()).contains(commandName.toLowerCase())) {
                foundCommand = true;
                String[] args = new String[split.length - 1];
                System.arraycopy(split, 1, args, 0, split.length - 1);

                try {
                    command.execute(args);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();

                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();

                    Minecraft.getMinecraft().thePlayer.addChatMessage(Formatting.RED + sw.toString());
                }
            }
        }

        if (!foundCommand) {
            this.print(String.format("%sCommand %s%s%s not found!", Formatting.RED, Formatting.GOLD, commandName, Formatting.RED));
        }
    };
    Bind bind = new Bind();

    Rot rot = new Rot();
    Toggle toggle = new Toggle();
    Reload reload = new Reload();
    Set set = new Set();
    Config config = new Config();
    NoteBot noteBot = new NoteBot();

    public void print(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(Formatting.AQUA + "[Zephyr] " + Formatting.RESET + message);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }
}
