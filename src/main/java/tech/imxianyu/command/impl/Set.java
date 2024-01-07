package tech.imxianyu.command.impl;

import net.minecraft.util.Formatting;
import tech.imxianyu.command.Command;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.settings.Setting;

/**
 * @author ImXianyu
 * @since 6/24/2023 10:21 PM
 */
public class Set extends Command {

    public Set() {
        super("Set", "set <module> <setting> <value>", "set <module> <setting> <value>", "s", "set");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            this.print(Formatting.RED + "Wrong usage! usage: " + this.getUsage());
        } else {
            String moduleName = args[0]/*.replaceAll("-", " ")*/;
            boolean moduleFound = false;
            for (Module module : ModuleManager.getModules()) {
                if (module.getName().equalsIgnoreCase(moduleName)) {
                    moduleFound = true;
                    String settingName = args[1]/*.replaceAll("\\^", " ")*/;
                    boolean settingFound = false;
                    for (Setting<?> setting : module.getSettings()) {
                        if (setting.getName().equalsIgnoreCase(settingName)) {

                            settingFound = true;
                            if (setting instanceof BooleanSetting) {
                                ((BooleanSetting) setting).setValue(Boolean.parseBoolean(args[2]));
                                this.print("Set setting " + setting.getName() + "'s value to " + setting.getValue());
                            } else if (setting instanceof ModeSetting) {
                                ((ModeSetting<?>) setting).setMode(args[2]);
                                this.print("Set setting " + setting.getName() + "'s value to " + setting.getValue());
                            } else if (setting instanceof NumberSetting) {
                                setting.loadValue(args[2]);
                                this.print("Set setting " + setting.getName() + "'s value to " + setting.getValue());
                            } else {
                                setting.loadValue(args[2]);
                                this.print("Set setting " + setting.getName() + "'s value to " + setting.getValue());
                            }

                        }
                    }

                    if (!settingFound) {
                        this.print(Formatting.RED + "Setting " + settingName + " not found!");
                    }
                }
            }

            if (!moduleFound) {
                this.print(Formatting.RED + "Module " + moduleName + " not found!");
            }
        }
    }
}
