package tech.imxianyu.command.impl;

import net.minecraft.util.Formatting;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.Zephyr;
import tech.imxianyu.command.Command;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;

/**
 * @author ImXianyu
 * @since 6/16/2023 3:49 PM
 */
public class Bind extends Command {

    public Bind() {
        super("Bind", "Binds a module to a key.", "bind <module> <key>", "bind");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {

            String option = args[0];

            // list all binds
            if ("list".equals(option)) {
                for (Module module : ModuleManager.getModules()) {
                    if (module.getKeyBind() != 0) {
                        this.print(Formatting.GOLD + module.getName() + Formatting.GREEN + ": " + Formatting.RESET + Keyboard.getKeyName(module.getKeyBind()));
                    }
                }
            } else {
                // else we're going to print the bounded key of the specified module
                String moduleName = args[0];

                Module m = Zephyr.getInstance().getModuleManager().getModuleByName(moduleName);

                if (m == null) {
                    this.print(Formatting.RED + "Module " + Formatting.GOLD + moduleName + Formatting.RED + " not found!");
                    return;
                }

                this.print(Formatting.GOLD + m.getName() + Formatting.GREEN + ": " + Formatting.RESET + Keyboard.getKeyName(m.getKeyBind()));

            }

            return;
        } else if (args.length < 2) {
            this.printUsage();
            return;
        }

        // bind the specified module to the specified key
        String moduleName = args[0];

        Module m = Zephyr.getInstance().getModuleManager().getModuleByName(moduleName);

        if (m == null) {
            this.print(Formatting.RED + "Module " + Formatting.GOLD + moduleName + Formatting.RED + " not found!");
            return;
        }

        String keyName = args[1];
        m.setKeyBind(Keyboard.getKeyIndex(keyName.toUpperCase()));
        this.print(Formatting.GREEN + "Successfully bound module " + Formatting.GOLD + m.getName() + Formatting.GREEN + " to key " + Formatting.RESET + Keyboard.getKeyName(Keyboard.getKeyIndex(keyName.toUpperCase())) + ".");
    }
}
