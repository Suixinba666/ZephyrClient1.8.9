package tech.imxianyu.gui.clickgui.panel.impl;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.imxianyu.Zephyr;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.gui.clickgui.panel.Panel;
import tech.imxianyu.gui.clickgui.panel.impl.musicpanel.LoginRenderer;
import tech.imxianyu.management.ConfigManager;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.WidgetsManager;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.music.IMusic;
import tech.imxianyu.music.dto.PlayList;
import tech.imxianyu.music.dto.UserProfile;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.TextField;
import tech.imxianyu.rendering.TexturedShadow;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.clickable.ClickableImage;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.multithreading.AsyncGLContentLoader;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.multithreading.MultiThreadingUtil;
import tech.imxianyu.utils.network.HttpClient;
import tech.imxianyu.utils.timing.Timer;
import tech.imxianyu.widget.impl.MusicLyrics;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.*;

/**
 * @author ImXianyu
 * @since 6/16/2023 9:32 AM
 */
public class MusicPanel extends Panel {

    public double listSelectorY1, listSelectorY2;
    public Map<IMusic, MusicEntity> map = new HashMap<>();
    public TextField searchBox = new TextField(0, 0, 0, 0, 0);
    LoginRenderer loginRenderer = null;

    public static UserProfile profile;
    public static ResourceLocation avatar = new ResourceLocation("Zephyr/textures/CloudMusicAvatar.png");
    public static List<PlayList> playLists;
    public static PlayList selectedList;
    double yScrollPlayList = 0, yScrollSmoothPlayList = 0;
    String lastSearch = "";
    float hoverInfoAlpha = 0;
    ClickableImage back = new ClickableImage(new ResourceLocation("Zephyr/textures/musicgui/back.png"), 0, 0, 0, 0, Image.Type.Normal,
            () -> {
                if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                    CloudMusic.back();
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            });
    ClickableImage next = new ClickableImage(new ResourceLocation("Zephyr/textures/musicgui/next.png"), 0, 0, 0, 0, Image.Type.Normal,
            () -> {
                if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                    CloudMusic.next();
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            });
    ClickableImage pausePlay = new ClickableImage(new ResourceLocation("Zephyr/textures/musicgui/play.png"), 0, 0, 0, 0, Image.Type.NoColor,
            () -> {
                if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) {
                    if (CloudMusic.player.isPausing())
                        CloudMusic.player.unpause();
                    else
                        CloudMusic.player.pause();
                }
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            });
    Framebuffer buffer = new Framebuffer(1, 1, false);


    public MusicPanel() {
        super("Music");
    }

    @Override
    public void init() {
        searchBox.setPlaceholder("Search");
    }

    @Override
    public void draw(double posX, double posY, double width, double height, double mouseX, double mouseY, int dWheel) {
        ZFontRenderer unicodeRenderer = FontManager.pf40;
        unicodeRenderer.drawString("Music", posX, posY, -1);

        if (RenderSystem.isHovered(mouseX, mouseY, posX, posY, unicodeRenderer.getStringWidth("Music"), unicodeRenderer.getHeight()) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
            ZephyrClickGui.getInstance().lmbPressed = true;

            Zephyr.getInstance().getConfigManager().refreshNCM();
        }

        searchBox.xPosition = (float) (posX + 12 + unicodeRenderer.getStringWidth("Music"));
        searchBox.yPosition = (float) (posY + 10);
        if (searchBox.isFocused())
            searchBox.onTick();
        searchBox.width = 100;
        searchBox.height = 10;
        searchBox.setTextColor(Color.WHITE.getRGB());
        searchBox.setDisabledTextColour(Color.GRAY.getRGB());
        searchBox.drawTextBox((int) mouseX, (int) mouseY);


        boolean loggedIn = CloudMusic.api.hasCookie();

        if (!loggedIn && this.loginRenderer == null) {
            this.loginRenderer = new LoginRenderer();
        }

        if (this.loginRenderer != null) {
            this.doRenderLoginRenderer(mouseX, mouseY, posX, posY, width, height);
        }

        if (!loggedIn || this.loginRenderer != null)
            return;

        ZFontRenderer shs18 = FontManager.pf18;

        String nickname = profile.getProfile().getNickname();
        double nickNameX = posX + width - shs18.getStringWidth(nickname) - 4;
        shs18.drawString(nickname, nickNameX, posY + 8 - shs18.getHeight() / 2.0, -1);

        double avatarWidth = 16, avatarHeight = 16, avatarX = nickNameX - 4 - avatarWidth, avatarY = posY;
        TexturedShadow.drawShadow(avatarX, avatarY, avatarWidth, avatarHeight, 1, 4);

        Image.draw(avatar, avatarX, avatarY, avatarWidth, avatarHeight, Image.Type.Normal);

        double downBarheight = 50;
        RoundedRect.drawRound(posX, posY + height - downBarheight, width, downBarheight, 3, new Color(0, 0, 0, 100));

        if (playLists != null) {

            FontManager.pf16.drawString("Playlists", posX, posY + 30, -1);

            RoundedRect.drawRound(posX, posY + 43, 108, height - downBarheight - 48, 3, new Color(0, 0, 0, 100));

            RenderSystem.doScissor((int) posX, (int) (posY + 46), 108, (int) (height - downBarheight - 52));


            double listOffsetX = posX + 4;
            double listOffsetY = posY + 47;
            double listWidth = 100, listHeight = 18;
            double listSpacing = 4;

            double yAdd = 5;
            if (RenderSystem.isHovered(mouseX, mouseY, posX, posY + 43, 108, height - downBarheight - 48) && dWheel != 0) {
                if (dWheel > 0)
                    yScrollSmoothPlayList -= yAdd;
                else
                    yScrollSmoothPlayList += yAdd;
            }

            yScrollSmoothPlayList = AnimationSystem.interpolate(yScrollSmoothPlayList, 0, 0.1f);
            yScrollPlayList = AnimationSystem.interpolate(yScrollPlayList, yScrollPlayList + yScrollSmoothPlayList, 0.6f);
            /*listSelectorY1 += yScrollSmoothPlayList;
            listSelectorY2 -= yScrollSmoothPlayList;*/

            if (yScrollPlayList < 0)
                yScrollPlayList = AnimationSystem.interpolate(yScrollPlayList, 0, 0.2f);

            listOffsetY -= yScrollPlayList;

            int currentListIndex = this.playLists.indexOf(this.selectedList);

            double y1Dest = listOffsetY + currentListIndex * (listHeight + listSpacing) + 4;
            double y2Dest = listOffsetY + currentListIndex * (listHeight + listSpacing) + listHeight - 4;
            float selectorSpeed = 0.7f;

            if (yScrollPlayList == 0) {
                if (y2Dest > listSelectorY2) {
                    listSelectorY2 = AnimationSystem.interpolate(listSelectorY2, y2Dest, selectorSpeed);

                    if (listSelectorY2 >= y2Dest - 0.2) {
                        listSelectorY1 = AnimationSystem.interpolate(listSelectorY1, y1Dest, selectorSpeed);
                    }
                } else {
                    listSelectorY1 = AnimationSystem.interpolate(listSelectorY1, y1Dest, selectorSpeed);


                    if (listSelectorY1 <= y1Dest + 0.2) {
                        listSelectorY2 = AnimationSystem.interpolate(listSelectorY2, y2Dest, selectorSpeed);

                    }
                }
            } else {
                listSelectorY1 = y1Dest;
                listSelectorY2 = y2Dest;
            }


            RoundedRect.drawRound(listOffsetX, listOffsetY + currentListIndex * (listHeight + listSpacing), listWidth, listHeight, 2, new Color(0, 0, 0, 100));

            RoundedRect.drawRound(listOffsetX, listSelectorY1, 2, listSelectorY2 - listSelectorY1, 1, new Color(0xff0090ff));

            for (PlayList list : this.playLists) {

                PlayList.RenderValues renderValues = list.getRenderValues();

                boolean hovered = RenderSystem.isHovered(mouseX, mouseY, listOffsetX, listOffsetY, listWidth, listHeight);

                if (this.selectedList != list && hovered && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                    ZephyrClickGui.getInstance().lmbPressed = true;
                    this.selectedList = list;
                    ResourceLocation coverForList = this.getCoverForList(selectedList);

                    ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(coverForList);

                    if (texture == null || texture == TextureUtil.missingTexture) {
                        AsyncGLContentLoader.loadGLContentAsync(new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {

                                BufferedImage img = ImageIO.read(HttpClient.downloadStream(selectedList.cover));

                                Minecraft.getMinecraft().getTextureManager().loadTexture(coverForList, new DynamicTexture(img));
                            }
                        });
                    }
                }

                if (hovered && this.selectedList != list) {

                    renderValues.hoveredAlpha = AnimationSystem.interpolate(renderValues.hoveredAlpha * 255, 100, 0.2f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;

                } else {
                    renderValues.hoveredAlpha = AnimationSystem.interpolate(renderValues.hoveredAlpha * 255, 0, 0.3f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;
                }

                RoundedRect.drawRound(listOffsetX, listOffsetY, listWidth, listHeight, 2, new Color(19, 27, 31, (int) (renderValues.hoveredAlpha * 255)));


                Stencil.write();
                Rect.draw(listOffsetX, listOffsetY, listWidth - 2, listHeight, -1, Rect.RectType.EXPAND);
                Stencil.erase(true);

                FontManager.pf16.drawString(list.name, listOffsetX + 4, listOffsetY + listHeight * 0.5 - FontManager.pf16.getHeight() * 0.5, ColorUtils.getColor(ColorUtils.ColorType.Text));
                Stencil.dispose();

                listOffsetY += listHeight + listSpacing;
            }

            RenderSystem.endScissor();


        }

        IMusic hovering = null;

        if (selectedList != null) {

            double selectedX = posX + 112, selectedY = posY + 43, selectedWidth = width - 112, selectedHeight = height - downBarheight - 48;
            RoundedRect.drawRound(selectedX, selectedY, selectedWidth, selectedHeight, 3, new Color(0, 0, 0, 100));

            double coverSize = 64;
            double coverX = selectedX + 4, coverY = selectedY + 4;
            double coverInfoX = coverX + coverSize + 4;


            if (!selectedList.searchMode) {
                ResourceLocation coverForList = this.getCoverForList(selectedList);

                Rect.draw(coverX, coverY, coverSize, coverSize, Color.GRAY.getRGB(), Rect.RectType.EXPAND);

                if (Minecraft.getMinecraft().getTextureManager().getTexture(coverForList) != null) {
                    TexturedShadow.drawShadow(coverX, coverY, coverSize, coverSize, 1, 10);
                    Image.draw(coverForList, coverX, coverY, coverSize, coverSize, Image.Type.Normal);
                }

                FontManager.pf25.drawString(selectedList.name, coverInfoX, coverY, -1);
                FontManager.pf18.drawString("Created By: " + selectedList.creator.get("nickname").getAsString(), coverInfoX, coverY + FontManager.pf25.getHeight() + 4, Color.GRAY.getRGB());

            } else {
                FontManager.pf25.drawString("Search - " + lastSearch, coverX, coverY, -1);
            }


            List<IMusic> musics = selectedList.getMusics();

            if (selectedList.searchMode)
                coverInfoX = coverX + 12;

            double playAllWidth = FontManager.pf18.getStringWidth("Play All") + 8;
            double buttonHeight = 16;
            RoundedRect.drawRound(coverInfoX, coverY + coverSize - buttonHeight, playAllWidth, buttonHeight, 5, new Color(0, 0, 0, 100));
            FontManager.pf18.drawString("Play All", coverInfoX + 4, coverY + coverSize - buttonHeight / 2.0 - FontManager.pf18.getHeight() / 2.0, -1);

            if (RenderSystem.isHovered(mouseX, mouseY, coverInfoX, coverY + coverSize - buttonHeight, playAllWidth, buttonHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                ZephyrClickGui.getInstance().lmbPressed = true;
                CloudMusic.play(musics);
            }

            RoundedRect.drawRound(coverInfoX + playAllWidth + 6, coverY + coverSize - buttonHeight, playAllWidth, buttonHeight, 5, new Color(0, 0, 0, 100));
            FontManager.pf18.drawString("Random", coverInfoX + playAllWidth + 8, coverY + coverSize - buttonHeight / 2.0 - FontManager.pf18.getHeight() / 2.0, -1);

            if (RenderSystem.isHovered(mouseX, mouseY, coverInfoX + playAllWidth + 6, coverY + coverSize - buttonHeight, playAllWidth, buttonHeight) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                ZephyrClickGui.getInstance().lmbPressed = true;
                List<IMusic> copy = new ArrayList<>(musics);
                Collections.shuffle(copy);
                CloudMusic.play(copy);
            }

            if (!selectedList.texturesLoaded && !musics.isEmpty()) {
                selectedList.texturesLoaded = true;
                this.loadCoversInList(musics);
            }

            double xSpacing = 16, ySpacing = 30;
            double panelX = coverX + 5;
            double panelY = coverY + coverSize + 4;
            double panelWidth = selectedWidth - 8;
            double panelHeight = selectedHeight - coverSize - 12;
            double pX = panelX + xSpacing, pY = panelY + xSpacing + selectedList.scrollOffset;
            double eWidth = 80, eHeight = 80;
            int lengthHorizontal = (int) ((panelWidth - xSpacing) / (eWidth + xSpacing));

            Stencil.write();
            Rect.draw(coverX, coverY + coverSize + 4, selectedWidth - 8, selectedHeight - coverSize - 12, -1, Rect.RectType.EXPAND);
            Stencil.erase(true);


            if (RenderSystem.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight)) {
                if (dWheel < 0) {
                    selectedList.scrollSmooth = 15;
                }
                if (dWheel > 0) {
                    selectedList.scrollSmooth = -15;
                }
            }

            if (selectedList.scrollSmooth != 0) {
                selectedList.scrollSmooth = AnimationSystem.interpolate(selectedList.scrollSmooth, 0, 0.15f);
                selectedList.scrollOffset = AnimationSystem.interpolate(selectedList.scrollOffset, selectedList.scrollOffset - selectedList.scrollSmooth, 0.6f);
            }

            int i = musics.size() / lengthHorizontal;
            double j = (double) musics.size() / lengthHorizontal;

            if (i != j)
                i += 1;

            if (selectedList.scrollOffset > 0) {
                selectedList.scrollOffset = AnimationSystem.interpolate(selectedList.scrollOffset, 0, 0.15f);
            } else if (selectedList.scrollOffset < -(i) * (xSpacing + eHeight)) {
                selectedList.scrollOffset = (AnimationSystem.interpolate(selectedList.scrollOffset, -(i) * (xSpacing + eHeight), 0.4f));
            }

            int count = 0;
            for (IMusic music : musics) {
                MusicEntity entity = this.getEntity(music);


                if (count == lengthHorizontal) {
                    count = 0;
                    pX = panelX + xSpacing;

                    pY += ySpacing + eHeight;
                }

                if (pY < coverY + coverSize + 4 - eHeight - ySpacing) {
                    pX += xSpacing + eWidth;
                    ++count;
                    continue;
                }
                if (pY > coverY + coverSize + 4 + selectedHeight - coverSize - 12) break;


                GlStateManager.pushMatrix();
                GlStateManager.translate(pX + eWidth / 2.0, pY + eHeight / 2.0, 0);
                GlStateManager.scale(entity.scale, entity.scale, 0);
                GlStateManager.translate(-(pX + eWidth / 2.0), -(pY + eHeight / 2.0), 0);

                Rect.draw(pX, pY, eWidth, eHeight, Color.GRAY.getRGB(), Rect.RectType.EXPAND);
//                RenderSystem.s(pX, pY, eWidth, eHeight, 1.0f, 4f);

                if (Minecraft.getMinecraft().getTextureManager().getTexture(this.getMusicCover(music)) != null) {
                    TexturedShadow.drawShadow(pX, pY, eWidth, eHeight, 1, 10);
                    Image.draw(this.getMusicCover(music), pX, pY, eWidth, eHeight, Image.Type.Normal);
                }

                GlStateManager.popMatrix();

                if (RenderSystem.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight) && RenderSystem.isHovered(mouseX, mouseY, pX, pY, eWidth, eHeight)) {
                    hovering = music;
                    if (Mouse.isButtonDown(0)) {
                        entity.mousePressed = true;
                        entity.scale = AnimationSystem.interpolate(entity.scale, 0.9, 0.2f);

                    } else {
                        if (entity.mousePressed) {
                            entity.mousePressed = false;
                            CloudMusic.play(Collections.singletonList(music));
                        }

                        entity.scale = AnimationSystem.interpolate(entity.scale, 1.1, 0.2f);
                    }

                } else {
                    entity.scale = AnimationSystem.interpolate(entity.scale, 1, 0.2f);

                    if (entity.mousePressed) {
                        entity.mousePressed = false;
                    }
                }


                ZFontRenderer nameRenderer = FontManager.pf16;

                String[] strings = nameRenderer.fitWidth(music.getName(), eWidth);

                double yOffset = pY + eHeight + 2 + 4 * ((entity.scale - 1) / 0.1);

                for (String string : strings) {
                    nameRenderer.drawCenteredString(string, pX + eWidth / 2.0, yOffset, RenderSystem.hexColor(245, 245, 245, 255));
                    yOffset += nameRenderer.getHeight() + 1;
                }

                pX += xSpacing + eWidth;
                ++count;
            }

            Stencil.dispose();

        }

        String musicName = "Not playing";
        String artistName = "";
        String currentTime = "00:00";
        String totalTime = "00:00";

        double length = 200;

        Minecraft mc = Minecraft.getMinecraft();

        Rect.draw(posX + width / 2.0d - length / 2.0d, posY + height - 9, length, 1, RenderSystem.hexColor(224, 224, 224, 255), Rect.RectType.EXPAND);

        double coverSize = downBarheight - 8;
        double coverY = posY + height - downBarheight + 4;


        IMusic playing = CloudMusic.currentlyPlaying;

        if (playing != null) {
            musicName = playing.getName();
            artistName = playing.getArtists();

            ResourceLocation cover = this.getMusicCover(playing);


            if (mc.getTextureManager().getTexture(cover) != null) {
                TexturedShadow.drawShadow(posX + 4, coverY, coverSize, coverSize, 1, 10);
                Image.draw(cover, posX + 4, coverY, coverSize, coverSize, Image.Type.Normal);
            }

            if (CloudMusic.player != null && CloudMusic.player.player != null) {
                Rect.draw(posX + width / 2.0d - length / 2.0d, posY + height - 9, length * (CloudMusic.player.getCurrentTimeMillis() / (CloudMusic.player.getTotalTimeMillis() + 0.01)), 1, 0xff0090ff, Rect.RectType.EXPAND);

                boolean hovered = RenderSystem.isHovered(mouseX, mouseY, posX + width / 2.0d - length / 2.0d, posY + height - 12, length, 6);
                if (hovered && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {
                    double mouseDelta = mouseX - (posX + width / 2.0d - length / 2.0d);
                    double perc = mouseDelta / length;
                    perc = Math.min(Math.max(0, perc), 1);
                    CloudMusic.player.setProgress((long) (perc * (CloudMusic.player.getTotalTimeMillis() / 1000L)));
                    MusicLyrics.quickResetProgress((long) perc * (CloudMusic.player.getTotalTimeMillis()));

                    ZephyrClickGui.getInstance().lmbPressed = true;
                }

                int cMin = CloudMusic.player.getCurrentTimeSeconds() / 60;
                int cSec = (CloudMusic.player.getCurrentTimeSeconds() - (CloudMusic.player.getCurrentTimeSeconds() / 60) * 60);
                currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                int tMin = CloudMusic.player.getTotalTimeSeconds() / 60;
                int tSec = (CloudMusic.player.getTotalTimeSeconds() - (CloudMusic.player.getTotalTimeSeconds() / 60) * 60);
                totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

            }
        }

        if (CloudMusic.player == null || CloudMusic.player.isPausing()) {
            pausePlay.setImage(new ResourceLocation("Zephyr/textures/musicgui/play.png"));
        } else {
            pausePlay.setImage(new ResourceLocation("Zephyr/textures/musicgui/pause.png"));
        }

        double buttonsY = coverY + 10;

        pausePlay.setX(posX + width / 2.0 - 8);
        pausePlay.setY(buttonsY);
        pausePlay.setWidth(16);
        pausePlay.setHeight(16);
        pausePlay.setType(Image.Type.Normal);
        pausePlay.draw(mouseX, mouseY);

        double distance = 50;

        back.setX(posX + width / 2.0 - distance - 8);
        back.setY(buttonsY);
        back.setWidth(16);
        back.setHeight(16);
        back.setType(Image.Type.Normal);
        back.draw(mouseX, mouseY);

        next.setX(posX + width / 2.0 + distance - 8);
        next.setY(buttonsY);
        next.setWidth(16);
        next.setHeight(16);
        next.setType(Image.Type.Normal);
        next.draw(mouseX, mouseY);

        FontManager.pf18.drawString(musicName, posX + 8 + coverSize, coverY + 4, Color.white.getRGB());
        FontManager.pf14.drawString(artistName, posX + 8 + coverSize, coverY + 8 + FontManager.pf18.getHeight(), Color.GRAY.getRGB());

        FontManager.pf14.drawString(currentTime, posX + width / 2.0d - length / 2.0d - 24, posY + height - 8 - FontManager.pf14.getHeight() / 2.0, Color.white.getRGB());
        FontManager.pf14.drawString(totalTime, posX + width / 2.0d + length / 2.0d + 4, posY + height - 8 - FontManager.pf14.getHeight() / 2.0, Color.white.getRGB());

        this.renderNumberSetting(ZephyrSettings.volume, mouseX, mouseY, posX + width - 100, coverY + 12);

        hoverInfoAlpha = AnimationSystem.interpolate(hoverInfoAlpha * 255, hovering == null ? 0 : 255, 0.3f) * RenderSystem.THE_MAGIC_DIVIDE_BY_255_FLOAT;

        if (hovering != null) {
            hoveringPrev = hovering;
            this.doRenderHoverInfo(hovering, mouseX + 6, mouseY + 6);
        } else {

            if (hoveringPrev != null) {
                this.doRenderHoverInfo(hoveringPrev, mouseX + 6, mouseY + 6);
                if (hoverInfoAlpha < 0.01)
                    hoveringPrev = null;
            }

        }
    }

    IMusic hoveringPrev = null;

    private void doRenderHoverInfo(IMusic hovering, double mouseX, double mouseY) {

        int mPart = Duration.ofMillis(hovering.getDuration()).toMinutesPart();
        int sPart = Duration.ofMillis(hovering.getDuration()).toSecondsPart();

        String secondsPart = String.valueOf(sPart);

        if (secondsPart.length() == 1)
            secondsPart = "0" + secondsPart;

        List<String> renderInfo = Arrays.asList(
                hovering.getName(),
                "Artists: " + hovering.getArtists(),
                "Duration: " + mPart + ":" + secondsPart,
                "ID: " + hovering.getId()
        );

        ZFontRenderer fontRenderer = FontManager.pf20;

        double widest = 0;

        for (String s : renderInfo) {
            int stringWidth = fontRenderer.getStringWidth(s);
            if (stringWidth > widest)
                widest = stringWidth;
        }

        double width = 6 + widest;
        double height = 5 + fontRenderer.getHeight() * renderInfo.size();

        RoundedRect.drawRound(mouseX, mouseY, width, height, 3, new Color(8, 9, 10, (int) (hoverInfoAlpha * 255)));

        double offsetX = mouseX + 3, offsetY = mouseY + 2;

        for (String s : renderInfo) {
            fontRenderer.drawString(s, offsetX, offsetY, RenderSystem.hexColor(255, 255, 255, (int) (hoverInfoAlpha * 255)));
            offsetY += fontRenderer.getHeight();
        }

    }

    private void loadCoversInList(List<IMusic> musics) {
        for (IMusic music : musics) {
            MultiThreadingUtil.runAsync(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    ResourceLocation musicCover = getMusicCover(music);
                    ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(musicCover);

                    if (texture != null && texture != TextureUtil.missingTexture)
                        return;

                    InputStream inputStream = HttpClient.downloadStream(music.getPicUrl(160), 5);

                    BufferedImage img;
                    try {
                        img = ImageIO.read(inputStream);
                    } catch (IIOException e) {
                        return;
                    }

                    BufferedImage finalImg = img;
                    AsyncGLContentLoader.loadGLContentAsync(() -> Minecraft.getMinecraft().getTextureManager().loadTexture(musicCover, new DynamicTexture(finalImg)));
                }
            });
        }
    }

    public MusicEntity getEntity(IMusic music) {
        if (map.get(music) == null)
            map.put(music, new MusicEntity());

        return map.get(music);
    }

    public static ResourceLocation getCoverForList(PlayList list) {
        return new ResourceLocation("Zephyr/textures/PlayListCover" + list.id + ".png");
    }

    public ResourceLocation getMusicCover(IMusic music) {
        return new ResourceLocation("Zephyr/textures/MusicCover" + music.getId() + ".png");
    }

    private void renderNumberSetting(NumberSetting setting, double mouseX, double mouseY, double x, double y) {
        ZFontRenderer fr16 = FontManager.pf16;
        fr16.drawString(setting.getName(), x, y, -1);

        double sliderWidth = 90, sliderHeight = 3, sliderX = x, sliderY = y + fr16.getHeight() + 4, sliderRadius = 1;
        RoundedRect.drawRound(sliderX, sliderY, sliderWidth, sliderHeight, sliderRadius, new Color(159, 159, 159));

        setting.nowWidth = AnimationSystem.interpolate(setting.nowWidth, (setting.getValue().doubleValue() / setting.getMaximum().doubleValue()) * sliderWidth, 0.2);
        RoundedRect.drawRound(sliderX, sliderY, setting.nowWidth, sliderHeight, sliderRadius, new Color(76, 194, 255));
        double circleSize = 9, smallCircleSize = 6;

        RoundedRect.drawRound(sliderX + setting.nowWidth - circleSize * 0.5, sliderY + sliderHeight * 0.5 - circleSize * 0.5, circleSize, circleSize, 3, new Color(69, 69, 69));
        RoundedRect.drawRound(sliderX + setting.nowWidth - smallCircleSize * 0.5, sliderY + sliderHeight * 0.5 - smallCircleSize * 0.5, smallCircleSize, smallCircleSize, 2, new Color(76, 194, 255));

        if (RenderSystem.isHovered(mouseX, mouseY, sliderX, y + fr16.getHeight(), sliderWidth, fr16.getHeight()) && Mouse.isButtonDown(0) && !ZephyrClickGui.getInstance().lmbPressed) {

            double mouseXToLeft = mouseX - sliderX;
            double percent = mouseXToLeft / sliderWidth;

            double min = setting.getMinimum().doubleValue();
            double max = setting.getMaximum().doubleValue();

            double result = max * percent;
            if (result < min)
                result = min;

            if (result > max)
                result = max;

            setting.setValue(Integer.valueOf((int) result));
        }

        String value = setting.df.format(setting.getValue());
        fr16.drawString(value, sliderX - 6 - fr16.getStringWidth(value), y + fr16.getHeight() + 6 - fr16.getHeight() / 2.0, Color.GRAY.getRGB());
    }

    private void doRenderLoginRenderer(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        this.loginRenderer.render(mouseX, mouseY, posX, posY, width, height);

        if (this.loginRenderer.canClose() && CloudMusic.api.hasCookie()) {
            this.loginRenderer = null;
            profile = CloudMusic.getUserProfile();
            playLists = CloudMusic.playLists(profile.getProfile().getUserId());
            if (!playLists.isEmpty()) {
                selectedList = playLists.get(0);
                this.loadListCover();
            }

            this.loadAvatar();
        }
    }

    private void loadAvatar() {
        AsyncGLContentLoader.loadGLContentAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                InputStream inputStream = HttpClient.downloadStream(profile.getProfile().getAvatarUrl());
                BufferedImage img = ImageIO.read(inputStream);

                TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

                if (textureManager.getTexture(avatar) != null)
                    textureManager.deleteTexture(avatar);

                textureManager.loadTexture(avatar, new DynamicTexture(img));
            }
        });
    }

    private void loadListCover() {
        ResourceLocation coverForList = this.getCoverForList(selectedList);

        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(coverForList);

        if (texture == null || texture == TextureUtil.missingTexture) {
            AsyncGLContentLoader.loadGLContentAsync(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {

                    BufferedImage img = ImageIO.read(HttpClient.downloadStream(selectedList.cover));

                    Minecraft.getMinecraft().getTextureManager().loadTexture(coverForList, new DynamicTexture(img));
                }
            });
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.searchBox.isFocused()) {
                this.searchBox.setFocused(false);
                return true;
            }
        }

        if (keyCode == Keyboard.KEY_RETURN && searchBox.isFocused() && !searchBox.getText().isEmpty()) {
//            a;

            lastSearch = searchBox.getText();
            PlayList playList = new PlayList();
            List<IMusic> search = CloudMusic.search(searchBox.getText());
            playList.musics = search;
            selectedList = playList;
            return true;
        }

        if (searchBox.isFocused()) {
            this.searchBox.textboxKeyTyped(typedChar, keyCode);
            return true;
        }

        if (keyCode == Keyboard.KEY_SPACE && CloudMusic.currentlyPlaying != null && !CloudMusic.player.isFinished()) {

            if (CloudMusic.player.isPausing())
                CloudMusic.player.unpause();
            else
                CloudMusic.player.pause();
            return true;
        }

        return false;
    }

    @Override
    public void mouseClicked(int mX, int mY, int mouseButton) {
        double mouseX = mX * RenderSystem.getScaleFactor();
        double mouseY = mY * RenderSystem.getScaleFactor();
        this.searchBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mX, int mY, int state) {
        double mouseX = mX * RenderSystem.getScaleFactor();
        double mouseY = mY * RenderSystem.getScaleFactor();
        this.searchBox.mouseReleased(mouseX, mouseY, state);
    }

    public class MusicEntity {
        public double nameX, artX;
        public boolean nameSide = true, artSide = true, mousePressed = false;
        public Timer nameTimer = new Timer(), artTimer = new Timer();
        public double scale = 1;
    }
}
