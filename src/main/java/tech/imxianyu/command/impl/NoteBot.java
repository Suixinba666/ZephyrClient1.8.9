package tech.imxianyu.command.impl;

import tech.imxianyu.Zephyr;
import tech.imxianyu.command.Command;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.player.notebot.Playing;
import tech.imxianyu.module.submodule.SubModule;

import java.io.File;

public class NoteBot extends Command {

    public NoteBot() {
        super("NoteBot", "Automatically plays .mid file.", "notebot play <file> or notebot stop", "nb");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            this.printUsage();
            return;
        }

        if (args[0].equalsIgnoreCase("play")) {
            if (args.length == 1) {
                this.printUsage();
                return;
            }

            String file = args[1];

            File noteBotDir = new File(Zephyr.getInstance().getConfigManager().configDir, "NoteBot");
            File midFile = new File(noteBotDir, file);

            if (!midFile.exists()) {
                this.print("File %s doesn't exist!", file);
                return;
            }

            if (!midFile.getName().endsWith(".mid")) {
                this.print("%s isn't a valid file!", file);
                return;
            }

            Playing playing = (Playing) ModuleManager.noteBot.getSubModuleByName("Playing");

            playing.play(midFile);
            this.print("ok");
        } else if (args[0].equalsIgnoreCase("stop")) {
            Playing playing = (Playing) ModuleManager.noteBot.getSubModuleByName("Playing");
            playing.stop();
            this.print("ok");
        }
    }
}
