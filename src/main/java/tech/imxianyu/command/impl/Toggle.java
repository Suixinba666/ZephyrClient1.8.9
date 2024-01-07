package tech.imxianyu.command.impl;

import net.minecraft.util.Formatting;
import tech.imxianyu.Zephyr;
import tech.imxianyu.command.Command;
import tech.imxianyu.module.Module;

/**
 * @author ImXianyu
 * @since 6/16/2023 3:43 PM
 */
public class Toggle extends Command {

    public Toggle() {
        super("Toggle", "Toggles the module.", "toggle <module>", "t");
    }

    @Override
    public void execute(String[] args) {

        if (args.length < 1) {
            this.printUsage();
            return;
        }

        String moduleName = args[0];

        Module m = Zephyr.getInstance().getModuleManager().getModuleByName(moduleName);

        if (m == null) {
            this.print(Formatting.RED + "Module " + Formatting.GOLD + moduleName + Formatting.RED + " not found!");
            return;
        }

        m.toggle();
        this.print((m.isEnabled() ? (Formatting.GREEN + "Enabled ") : (Formatting.RED + "Disabled ")) + Formatting.RESET + "module " + Formatting.GOLD + m.getName() + Formatting.RESET + ".");
    }
}
