/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  com.mojang.authlib.Agent
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
 *  org.lwjgl.input.Keyboard
 */
package tech.imxianyu.gui.alt;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Formatting;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.utils.oauth.OAuth;

import java.io.IOException;
import java.util.Random;

public class GuiAddAlt
        extends GuiScreen {
    private final GuiAltManager manager;
    private String status = "§eWaiting...";
    private GuiTextField username;

    public GuiAddAlt(GuiAltManager manager) {
        this.manager = manager;
    }

    static void setStatus(GuiAddAlt guiAddAlt, String string) {
        guiAddAlt.status = string;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0 -> {
                Alt alt = new Alt(this.username.getText());
                AltManager.getAlts().add(alt);
                this.status = "§Added. (" + this.username.getText() + " - offline name)";
            }
            case 1 -> {
                this.mc.displayGuiScreen(this.manager);
            }
            case 2 -> {
                new Thread(() -> {
                    setStatus(this, Formatting.GRAY + "Processing Requests, Please Wait...");
                    OAuth oAuth = new OAuth();
                    oAuth.logIn(
                            new OAuth.LoginCallback() {
                                @Override
                                public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                                    Alt alt = new Alt(userName, refreshToken, token, uuid);
                                    alt.setLastRefreshedTime(System.currentTimeMillis() / 1000L);
                                    AltManager.getAlts().add(alt);
                                    GuiAddAlt.setStatus(GuiAddAlt.this, Formatting.GREEN + "Alt added. (" + userName + ")");
                                }

                                @Override
                                public void onFailed(Exception e) {

                                }

                                @Override
                                public void setStatus(String status) {
                                    GuiAddAlt.setStatus(GuiAddAlt.this, status);
                                }
                            }
                    );
                }).start();
            }
            case 3 -> {
                String s = this.genRandomCrackedName();
                AltManager.getAlts().add(new Alt(s));
                setStatus(this, "§aAlt added. (" + s + ")");
            }
        }
    }

    private String genRandomCrackedName() {
        return "Zephyr" + this.getRandomString(10);
    }

    private String getRandomString(int length) {
        String dict = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(dict.charAt(random.nextInt(dict.length())));
        }

        return sb.toString();
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        mc.fontRendererObj.drawCenteredString("Add Alt", this.width / 2.0f, 20, -1);
        this.username.drawTextBox();
        if (this.username.getText().isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow("Username / E-Mail", this.width / 2.0f - 96, 66.0f, -7829368);
        }

        mc.fontRendererObj.drawCenteredString(this.status, this.width / 2.0f, 30, -1);
        super.drawScreen(i, j, f);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 92 + 12, "Login"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 116 + 12, "Back"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 140 + 12, "Microsoft Login"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 4 + 164 + 12, "Random Cracked Name"));
        this.username = new GuiTextField(1, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.username.textboxKeyTyped(par1, par2);

        if (par1 == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username.mouseClicked(par1, par2, par3);
    }

    private class AddAltThread
            extends Thread {
        private final String password;
        private final String username;

        public AddAltThread(String username, String password) {
            this.username = username;
            this.password = password;
            GuiAddAlt.setStatus(GuiAddAlt.this, "§7Waiting...");
        }

        @Override
        public void run() {
            if (this.password.equals("")) {
                AltManager.getAlts().add(new Alt(this.username));
                GuiAddAlt.setStatus(GuiAddAlt.this, "§aAlt added. (" + this.username + " - offline name)");
            }
        }
    }
}

