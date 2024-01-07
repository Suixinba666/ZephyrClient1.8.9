package tech.imxianyu.command.impl;

import tech.imxianyu.command.Command;

/**
 * @author ImXianyu
 * @since 6/16/2023 4:33 PM
 */
public class Reload extends Command {

    public Reload() {
        super("Reload", "Reload some stuff.", "reload", "r");
    }

    @Override
    public void execute(String[] args) {


    }
}
