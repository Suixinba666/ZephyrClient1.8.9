package tech.imxianyu.gui.multiplayerdark.dialog.dialogs;

import lombok.AllArgsConstructor;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.multiplayerdark.dialog.Dialog;
import tech.imxianyu.gui.multiplayerdark.ZephyrMultiPlayerUIDark;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.TextField;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ServerInfoDialogDark extends Dialog {

    private double openCloseScale = 1.1;

    private final TextField name = new TextField(-1, 0, 0, 0, 0);
    private final TextField address = new TextField(-2, 0, 0, 0,  0);

    private int resourcePackMode = 0;

    private final int index;

    public ServerInfoDialogDark() {
        name.setDrawLineUnder(false);
        name.setPlaceholder("Name");
        name.setText("Minecraft Server");
        name.setFontRenderer(FontManager.pf20);
        name.setDisabledTextColour(RenderSystem.hexColor(159, 159, 159));
        name.enabledColor = RenderSystem.hexColor(255, 255, 255);
        address.setDrawLineUnder(false);
        address.setPlaceholder("Address");
        address.setFontRenderer(FontManager.pf20);
        address.setDisabledTextColour(RenderSystem.hexColor(159, 159, 159));
        address.enabledColor = RenderSystem.hexColor(255, 255, 255);

        this.index = -1;
    }

    public ServerInfoDialogDark(ServerData data, int index) {
        name.setDrawLineUnder(false);
        name.setPlaceholder("Name");
        name.setFontRenderer(FontManager.pf20);
        name.setDisabledTextColour(RenderSystem.hexColor(159, 159, 159));
        name.enabledColor = RenderSystem.hexColor(255, 255, 255);
        name.setText(data.serverName);
        address.setDrawLineUnder(false);
        address.setPlaceholder("Address");
        address.setFontRenderer(FontManager.pf20);
        address.setDisabledTextColour(RenderSystem.hexColor(159, 159, 159));
        address.enabledColor = RenderSystem.hexColor(255, 255, 255);
        address.setText(data.serverIP);

        this.index = index;
        this.resourcePackMode = data.getResourceMode().ordinal();
    }

    @Override
    public void render(double mouseX, double mouseY, ZephyrMultiPlayerUIDark inst) {
        super.drawBackgroundMask(inst);

        this.openCloseScale = AnimationSystem.interpolate(this.openCloseScale, this.isClosing() ? 1.1 : 1, 0.3);
        ZFontRenderer titleRenderer = FontManager.pf26;
        ZFontRenderer contentRenderer = FontManager.pf14;

        int intAlpha = (int) (this.alpha * 255);

        double width = 400;
        double height = 226;
        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        this.doGlPreTransforms(this.openCloseScale);

        RoundedRect.drawRound(x, y, width, height, 16, new Color(0, 0, 0, intAlpha));

        titleRenderer.drawString("Server Name", x + 32, y + 25, RenderSystem.hexColor(255, 255, 255, intAlpha));

        RoundedRect.drawRound(x + 30, y + 25 + 7 + titleRenderer.getHeight(), width - 60, 24, 8, new Color(10, 10, 10, intAlpha));

        name.xPosition = (float) (x + 40);
        name.yPosition = (float) (y + 37 + titleRenderer.getHeight());
        if (name.isFocused())
            name.onTick();
        name.width = (float) (width - 80);
        name.height = 14;
        name.setTextColor(RenderSystem.hexColor(159, 159, 159));
        name.drawTextBox((int) mouseX, (int) mouseY);

        titleRenderer.drawString("Server Address", x + 32, y + 32 + 24 + 15 + titleRenderer.getHeight(), RenderSystem.hexColor(255, 255, 255, intAlpha));

        RoundedRect.drawRound(x + 30, y + 32 + 24 + 15 + 7 + titleRenderer.getHeight() * 2, width - 60, 24, 8, new Color(10, 10, 10, intAlpha));

        address.xPosition = (float) (x + 40);
        address.yPosition = (float) (y + 32 + 24 + 15 + 7 + 5 + titleRenderer.getHeight() * 2);
        if (address.isFocused())
            address.onTick();
        address.width = (float) (width - 80);
        address.height = 14;
        address.setTextColor(RenderSystem.hexColor(159, 159, 159));
        address.drawTextBox((int) mouseX, (int) mouseY);

        titleRenderer.drawString("Server Resource Pack", x + 32, y + 32 + 24 + 15 + 46 + 14 + titleRenderer.getHeight(), RenderSystem.hexColor(255, 255, 255, intAlpha));

        List<ResourceMode> options = Arrays.asList(new ResourceMode("Prompt", 2), new ResourceMode("Enabled", 0), new ResourceMode("Disabled", 1));

        ZFontRenderer optionsRenderer = FontManager.pf16;

        double selectorWidth = 8;

        for (ResourceMode option : options) {
            selectorWidth += 9 + optionsRenderer.getStringWidth(option.name);
        }

        double selectorX = x + width - 30 - selectorWidth;
        double selectorY = y + 32 + 24 + 15 + 46 + 14 + titleRenderer.getHeight();
        double selectorHeight = 18;

        RoundedRect.drawRound(selectorX - 2, selectorY, selectorWidth + 1, selectorHeight, 5, new Color(10, 10, 10, intAlpha));

        double offsetX = selectorX + 4;
        for (ResourceMode option : options) {

            int indexOf = option.ordinary;

            if (indexOf == resourcePackMode) {
                RoundedRect.drawRound(offsetX - 3, selectorY + 2, 6 + optionsRenderer.getStringWidth(option.name), 14, 3, new Color(0, 0, 0, intAlpha));
            } else {
                if (RenderSystem.isHovered(mouseX, mouseY, offsetX - 3, selectorY + 1, 4 + optionsRenderer.getStringWidth(option.name), 14) && Mouse.isButtonDown(0) && !previousMouse) {
                    resourcePackMode = option.ordinary;
                    previousMouse = true;
                }
            }

            if (indexOf == 0 || indexOf == 2) {
                optionsRenderer.drawString("|", offsetX + 5 + optionsRenderer.getStringWidth(option.name), selectorY + selectorHeight * 0.5 - optionsRenderer.getHeight() * 0.5, RenderSystem.hexColor(78, 78, 78, intAlpha));
            }

            optionsRenderer.drawString(option.name, offsetX - 0.5, selectorY + selectorHeight * 0.5 - optionsRenderer.getHeight() * 0.5, RenderSystem.hexColor(255, 255, 255));

            offsetX += 12 + optionsRenderer.getStringWidth(option.name);
        }

        //Done button

        RoundedRect.drawRound(x + 30, y + height - 46, (width - 68) * 0.5, 20, 3, (!name.getText().isEmpty() && !address.getText().isEmpty()) ? new Color(94, 169, 255, intAlpha) : new Color(65, 65, 65, intAlpha));

        FontManager.pf20.drawCenteredString("Done", x + 30 + (width - 68) * 0.25, y + height - 36 - FontManager.pf20.getHeight() * 0.5, RenderSystem.hexColor(0, 0, 0, intAlpha));

        if (RenderSystem.isHovered(mouseX, mouseY, x + 30, y + height - 46, (width - 68) * 0.5, 20) && Mouse.isButtonDown(0) && !previousMouse) {
            previousMouse = true;

            if (!name.getText().isEmpty() && !address.getText().isEmpty()) {
                ServerData data = new ServerData(name.getText(), address.getText(), false);
                data.setResourceMode(ServerData.ServerResourceMode.values()[resourcePackMode]);

                if (index == -1)
                    inst.serverList.addServerData(data);
                else
                    inst.serverList.setServer(this.index, data);
                inst.serverList.saveServerList();
                inst.addServers();

                this.close();
            }

        }

        //Cancel button

        RoundedRect.drawRound(x + (width - 68) * 0.5 + 38, y + height - 46, (width - 68) * 0.5, 20, 3, new Color(10, 10, 10, intAlpha));

        FontManager.pf20.drawCenteredString("Cancel", x + 34 + (width - 60) * 0.75, y + height - 36 - FontManager.pf20.getHeight() * 0.5, RenderSystem.hexColor(255, 255, 255, intAlpha));

        if (RenderSystem.isHovered(mouseX, mouseY, x + (width - 68) * 0.5 + 38, y + height - 46, (width - 68) * 0.5, 20) && Mouse.isButtonDown(0) && !previousMouse) {
            previousMouse = true;
            this.close();
        }

        this.disposeTransforms();

        if (!Mouse.isButtonDown(0) && previousMouse) {
            previousMouse = false;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
            this.close();
    }

    @AllArgsConstructor
    private static class ResourceMode {
        String name;
        int ordinary;
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        this.name.textboxKeyTyped(typedChar, keyCode);
        this.address.textboxKeyTyped(typedChar, keyCode);
        return false;
    }

    @Override
    public void mouseClicked(int mX, int mY, int mouseButton) {
        double mouseX = mX * RenderSystem.getScaleFactor();
        double mouseY = mY * RenderSystem.getScaleFactor();
        this.name.mouseClicked(mouseX, mouseY, mouseButton);
        this.address.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mX, int mY, int state) {
        double mouseX = mX * RenderSystem.getScaleFactor();
        double mouseY = mY * RenderSystem.getScaleFactor();
        this.name.mouseReleased(mouseX, mouseY, state);
        this.address.mouseReleased(mouseX, mouseY, state);
    }
}
