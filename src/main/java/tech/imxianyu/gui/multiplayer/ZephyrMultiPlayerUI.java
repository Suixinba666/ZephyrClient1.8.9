package tech.imxianyu.gui.multiplayer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.ZephyrScreen;
import tech.imxianyu.gui.multiplayer.dialog.Dialog;
import tech.imxianyu.gui.multiplayer.dialog.dialogs.ActionsDialog;
import tech.imxianyu.gui.multiplayer.dialog.dialogs.ServerInfoDialog;
import tech.imxianyu.gui.multiplayerdark.ServerBeanDark;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.BloomShader;
import tech.imxianyu.rendering.shader.StencilShader;
import tech.imxianyu.rendering.transition.TransitionAnimation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ZephyrMultiPlayerUI extends ZephyrScreen {

    private final GuiScreen parentScreen;
    public final List<ServerBean> serverBeans = new ArrayList<>();
    public final ThreadPoolExecutor pingers = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    public final OldServerPinger oldServerPinger = new OldServerPinger();
    public Framebuffer behindBloomBuffer, topBloomBuffer, stencilContentBuffer, stencilCoverBuffer;
    public ServerList serverList;

    public final List<TexturedButton> buttons = new ArrayList<>();

    public boolean deleteMode = false;

    public Dialog dialog = null;

    public ZephyrMultiPlayerUI(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    private double scrollOffset = 0, scrollSmooth = 0;

    public void addServers() {
        serverList = new ServerList(mc);
        serverList.loadServerList();

        this.serverBeans.clear();

        for (ServerData data : serverList.getServers()) {
            this.serverBeans.add(new ServerBean(data));
        }
    }

    @Override
    public void initGui() {
        this.buttons.clear();

        this.addServers();

        List<Tuple<ResourceLocation, Runnable>> b = new ArrayList<>();

        b.add(new Tuple<>(new ResourceLocation("Zephyr/textures/multiplayer/add.png"), () -> {
            this.dialog = new ServerInfoDialog();
        }));

        b.add(new Tuple<>(new ResourceLocation("Zephyr/textures/multiplayer/remove.png"), () -> {
            if (!deleteMode) {
                deleteMode = true;

                for (ServerBean serverBean : this.serverBeans) {
                    serverBean.selected = false;
                }
            } else {

                boolean isAtLeastOneServerSelected = this.serverBeans.stream().anyMatch(s -> s.selected);

                if (isAtLeastOneServerSelected) {

                    Tuple<String, Runnable> yes = ActionsDialog.buildAction("Yes", () -> {
                            deleteMode = false;

                            for (ServerBean serverBean : this.serverBeans.stream().filter(s -> s.selected).collect(Collectors.toList())) {
                                int index = this.serverList.getServers().indexOf(serverBean.getServer());

                                this.serverList.removeServerData(index);
                                this.serverList.saveServerList();
                                this.addServers();

                            }
                    });

                    Tuple<String, Runnable> no = ActionsDialog.buildAction("No", () -> {
                        deleteMode = false;

                        for (ServerBean serverBean : this.serverBeans) {
                            serverBean.selected = false;
                        }

                    });


                    this.dialog = new ActionsDialog("Do you really want to delete these servers?", "Once deleted, it cannot be recalled", Arrays.asList(yes, no));
                } else {
                    deleteMode = false;
                }

            }
        }));

        b.add(new Tuple<>(new ResourceLocation("Zephyr/textures/multiplayer/refresh.png"), this::addServers));
        b.add(new Tuple<>(new ResourceLocation("Zephyr/textures/multiplayer/back.png"), () -> {
            deleteMode = false;
        }));


        double imgSize = 60, spacing = -15;
        double startX = RenderSystem.getWidth() * 0.5 - imgSize * 1.5 - spacing;
        long delay = 0;

        for (Tuple<ResourceLocation, Runnable> tuple : b) {

            if (tuple.getFirst().getResourcePath().substring(tuple.getFirst().getResourcePath().lastIndexOf("/") + 1, tuple.getFirst().getResourcePath().lastIndexOf(".")).equals("back")) {
                this.buttons.add(new TexturedButton(tuple.getFirst(), RenderSystem.getWidth() * 0.5 - imgSize * 1.5 - spacing, 0, imgSize, imgSize, 60, tuple.getSecond()));
                continue;
            }

            this.buttons.add(new TexturedButton(tuple.getFirst(), startX, 0, imgSize, imgSize, delay, tuple.getSecond()));

            startX += imgSize + spacing;
            delay += 60;
        }

        scrollOffset = scrollSmooth = 0;

        behindBloomBuffer = RenderSystem.createFrameBuffer(behindBloomBuffer);
        topBloomBuffer = RenderSystem.createFrameBuffer(topBloomBuffer);
        stencilContentBuffer = RenderSystem.createFrameBuffer(stencilContentBuffer);
        stencilCoverBuffer = RenderSystem.createFrameBuffer(stencilCoverBuffer);
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(240, 240, 240), Rect.RectType.EXPAND);

        BloomShader.render(behindBloomBuffer.framebufferTexture, 15, 1, 0.0f, 0.3f, true);

        behindBloomBuffer.framebufferClear();
        mc.getFramebuffer().bindFramebuffer(true);

        FontManager.segoe38.drawString("Zephyr", 16, 9, RenderSystem.hexColor(33, 33, 33));
        FontManager.segoe18.drawString("Server List", 82, 19, RenderSystem.hexColor(70, 70, 70));

        int dWheel = Mouse.getDWheel();

        double yAddAdd = 2.5;

        if (dWheel > 0)
            scrollSmooth -= yAddAdd;
        else if (dWheel != 0)
            scrollSmooth += yAddAdd;

        scrollSmooth = AnimationSystem.interpolate(scrollSmooth, 0, 0.1f);
        scrollOffset += scrollSmooth;

        if (scrollOffset < 0)
            scrollOffset = AnimationSystem.interpolate(scrollOffset, 0, 0.3f);

        int maskX = 10, maskY = 38;

        RenderSystem.doScissor(maskX, maskY, (int) RenderSystem.getWidth(), (int) RenderSystem.getHeight());

        double offsetX = 15, offsetY = 42 - scrollOffset;
        double width = 210, height = 80;
        double xSpace = 12, ySpace = 15;

        int count = 0;
        int horizontalLength = (int) ((RenderSystem.getWidth()) / (width + xSpace)) - 1;

        int lines = 0;


        for (int i = 0; i < this.serverBeans.size(); i++) {
            ServerBean serverBean = this.serverBeans.get(i);
            serverBean.draw(offsetX, offsetY, width, height, mouseX, mouseY, this);

            offsetX += width + xSpace;


            if (count == horizontalLength && i != this.serverBeans.size() - 1) {
                count = 0;
                offsetX = 15;
                offsetY += ySpace + height;
                lines ++;
            } else {
                ++count;
            }
        }

        double max = (lines) * (height + ySpace);

        if (scrollOffset > max)
            scrollOffset = AnimationSystem.interpolate(scrollOffset, max, 0.3f);

        RenderSystem.endScissor();

        StencilShader.render(stencilContentBuffer.framebufferTexture, stencilCoverBuffer.framebufferTexture);
        stencilContentBuffer.framebufferClear();
        stencilCoverBuffer.framebufferClear();
        mc.getFramebuffer().bindFramebuffer(true);

        BloomShader.render(topBloomBuffer.framebufferTexture, 10, 1, 0.0f, 0.4f, true);

        topBloomBuffer.framebufferClear();
        mc.getFramebuffer().bindFramebuffer(true);

        for (TexturedButton button : this.buttons) {
            button.draw(mouseX, mouseY, this);
        }

        if (this.dialog != null) {
            this.dialog.render(mouseX, mouseY, this);

            if (this.dialog.canClose()) {
                this.dialog = null;
            }
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {

            if (this.dialog != null) {
                return;
            }

            TransitionAnimation.task(() -> mc.displayGuiScreen(this.parentScreen));


        }

        if (this.dialog != null)
            this.dialog.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.dialog != null)
            this.dialog.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.dialog != null)
            this.dialog.mouseReleased(mouseX, mouseY, state);
    }
}
