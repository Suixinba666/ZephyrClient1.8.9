package tech.imxianyu.command.impl;

import lombok.SneakyThrows;
import tech.imxianyu.Zephyr;
import tech.imxianyu.command.Command;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.Setting;

import java.io.File;
import java.io.FileWriter;

public class Config extends Command {


    public Config() {
        super("Config", "Save or reload config.", "config <save/reload/load <profile name>>", "c", "config");
    }

    @Override
    @SneakyThrows
    public void execute(String[] args) {
        if (args.length == 0) {
            this.printUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "create": {
                // creates a new config.
                if (args.length < 2) {
                    this.printUsage();
                    return;
                }

                String config = args[1];

                File configsFile = new File(Zephyr.getInstance().getConfigManager().configDir, "Profiles");
                File configFile = new File(configsFile, config + ".json");
                if (configFile.exists()) {
                    this.print("That profile is already exist!");
                    return;
                } else {
                    configFile.createNewFile();

                    FileWriter writer = new FileWriter(configFile);

                    // dummy config
                    writer.write("{\n" +
                            "\t\"Modules\": {\n" +
                            "\n" +
                            "\t},\n" +
                            "\t\"Widgets\": {\n" +
                            "\n" +
                            "\t},\n" +
                            "\t\"Settings\": {\n" +
                            "\t\n" +
                            "\t}\n" +
                            "}");
                    writer.flush();
                    writer.close();
                }

                //
                Zephyr.getInstance().getConfigManager().saveConfig();
                for (Module module : ModuleManager.getModules()) {
                    if (module.isEnabled())
                        module.setEnabled(false);
                    module.setKeyBind(0);
                    for (Setting<?> setting : module.getSettings()) {
                        setting.reset();
                    }
                }
                Zephyr.getInstance().getConfigManager().currentConfig = config;
                Zephyr.getInstance().getConfigManager().loadConfig();
                Zephyr.getInstance().getConfigManager().loadAlts();
                Zephyr.getInstance().getConfigManager().loadFriends();

                this.print("Config created: " + config);
                break;
            }

            case "save": {
                if (args.length > 1) {
                    String config = args[1];

                    Zephyr.getInstance().getConfigManager().saveConfig();
                    Zephyr.getInstance().getConfigManager().currentConfig = config;
                    Zephyr.getInstance().getConfigManager().saveConfig();
                    Zephyr.getInstance().getConfigManager().saveAlts();
                    Zephyr.getInstance().getConfigManager().saveFriends();
                    this.print("Config saved: " + config);
                } else {
                    Zephyr.getInstance().getConfigManager().saveConfig();
                    this.print("Config saved: " + Zephyr.getInstance().getConfigManager().currentConfig);
                }
                break;
            }

            case "load": {
                if (args.length < 2) {
                    this.printUsage();
                    return;
                }

                String config = args[1];

                File configsFile = new File(Zephyr.getInstance().getConfigManager().configDir, "Profiles");
                File configFile = new File(configsFile, config + ".json");
                if (!configFile.exists()) {
                    this.print("That profile isn't exist!");
                    return;
                }

                Zephyr.getInstance().getConfigManager().saveConfig();
                for (Module module : ModuleManager.getModules()) {
                    if (module.isEnabled())
                        module.setEnabled(false);
                    module.setKeyBind(0);
                    for (Setting<?> setting : module.getSettings()) {
                        setting.reset();
                    }
                }
                Zephyr.getInstance().getConfigManager().currentConfig = config;
                Zephyr.getInstance().getConfigManager().loadConfig();
                Zephyr.getInstance().getConfigManager().loadAlts();
                Zephyr.getInstance().getConfigManager().loadFriends();
                this.print("Config loaded: " + config);
                break;
            }

            case "reload": {
                for (Module module : ModuleManager.getModules()) {
                    if (module.isEnabled())
                        module.setEnabled(false);
                    module.setKeyBind(0);
                    for (Setting<?> setting : module.getSettings()) {
                        setting.reset();
                    }
                }
                Zephyr.getInstance().getConfigManager().onStart();
                this.print("Config reloaded: " + Zephyr.getInstance().getConfigManager().currentConfig);
                break;
            }
        }
    }
}
