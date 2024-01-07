package tech.imxianyu.widget.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Formatting;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.interfaces.IFontRenderer;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.*;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.widget.Widget;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.lwjgl.opengl.GL11;

public class ScoreBoard extends Widget {

    public ScoreBoard() {
        super("ScoreBoard");
    }

    public BooleanSetting clientChat = new BooleanSetting("Use client font renderer", false);

    @Override
    public void onRender(Render2DEvent event, boolean editing) {
        Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.thePlayer.getName());

        if (scoreplayerteam != null)
        {
            int i1 = scoreplayerteam.getChatFormat().getColorIndex();

            if (i1 >= 0)
            {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreobjective1 != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.getX(), this.getY(), 0);
            GlStateManager.scale(1.1, 1.1, 0);
            GlStateManager.translate(-this.getX(), -this.getY(), 0);

            this.renderScoreboard(this.getX(), this.getY(), scoreobjective1);
            GlStateManager.popMatrix();
        }
    }

    private void renderScoreboard(double x, double y, ScoreObjective objective)
    {
        Scoreboard scoreboard = objective.getScoreboard();
        List<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> scores = collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());

        if (scores.size() > 15)
        {
            collection = Lists.newArrayList(Iterables.skip(scores, collection.size() - 15));
        }
        else
        {
            collection = scores;
        }

        int maxWidth = this.getFontRenderer().getStringWidth(objective.getDisplayName());

        for (Score score : collection)
        {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + Formatting.RED + score.getScorePoints();
            maxWidth = Math.max(maxWidth, this.getFontRenderer().getStringWidth(s));
        }

        Collections.reverse(collection);

        double round = 2;

        if (!ZephyrSettings.reduceShaders.getValue()) {
            ShaderUtils.doRoundedBlurAndBloom(x, y, maxWidth, this.getFontRenderer().getHeight() * (collection.size() + 1), round);
        } else {
            TexturedShadow.drawShadow(x, y, maxWidth, this.getFontRenderer().getHeight() * (collection.size() + 1), 1.0f, 6);
            Rect.draw(x, y, maxWidth, this.getFontRenderer().getHeight() * (collection.size() + 1), RenderSystem.hexColor(0, 0, 0, 80), Rect.RectType.EXPAND);
        }

        double offsetY = this.getFontRenderer().getHeight();
        for (int i = 0; i < collection.size(); i++) {

            if (i == 0) {
                GlStateManager.enableAlpha();
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
                String name = objective.getDisplayName();
//                Rect.draw(x, y, maxWidth, this.getFontRenderer().getHeight(), RenderSystem.hexColor(0, 0, 0, 16), Rect.RectType.EXPAND);
                this.drawString(name, x + maxWidth * 0.5 - this.getFontRenderer().getStringWidth(name) * 0.5, y + 1, RenderSystem.hexColor(255, 255, 255, 255));
            }

            Score score1 = collection.get(i);

            ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
            String left = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
            String right = Formatting.RED + String.valueOf(score1.getScorePoints());

            this.drawString(left, x + 2, y + offsetY, RenderSystem.hexColor(255, 255, 255, 255));
            this.drawString(right, x + maxWidth - 2 - this.getFontRenderer().getStringWidth(right), y + offsetY, RenderSystem.hexColor(255, 255, 255, 255));
            offsetY += this.getFontRenderer().getHeight();
        }

        this.setWidth(maxWidth * 1.1);
        this.setHeight(offsetY * 1.1);
    }
    
    private void drawString(String text, double x, double y, int color) {
        IFontRenderer fontRenderer = this.getFontRenderer();
        
        if (fontRenderer instanceof ZFontRenderer) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 1);
            double scale = 1 / 1.1;
            GlStateManager.scale(scale, scale, 1);
            fontRenderer.drawString(text, 0, 0, color);
            GlStateManager.popMatrix();
        } else {
            fontRenderer.drawString(text, x, y, color);
        }
        
    }

    private IFontRenderer getFontRenderer() {
        return this.clientChat.getValue() ? FontManager.pf18 : mc.fontRendererObj;
    }
}
