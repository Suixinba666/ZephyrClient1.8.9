package tech.imxianyu.module.impl.render;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ColorSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ImXianyu
 * @since 6/20/2023 9:44 AM
 */
public class BlockESP extends Module {

    private final Timer searchTimer = new Timer();
    private final List<BlockPos> posList = new ArrayList<>();
    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Box);
    public BooleanSetting chest = new BooleanSetting("Chests", true);
    public BooleanSetting skull = new BooleanSetting("Skulls", false);
    public BooleanSetting bed = new BooleanSetting("Bed", false);

    public NumberSetting<Integer> radius = new NumberSetting<>("Radius", 50, 1, 200, 1);

    public ColorSetting color = new ColorSetting("Color", new HSBColor(0, 255, 0, 255));
    @Handler
    public void onRender3D(Render3DEvent event) {
        synchronized (posList) {
            int color = this.color.getRGB();

            for (final BlockPos blockPos : posList) {
                switch (this.mode.getValue()) {
                    case Box:
                        RenderSystem.drawBlockBox(blockPos, color, true);
                        break;
                    case Flat:
                        RenderSystem.draw2D(blockPos, color, Color.BLACK.getRGB());
                        break;
                }
            }
        }
    };
    private Thread thread;
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (searchTimer.isDelayed(1000L) && (thread == null || !thread.isAlive())) {
            int r = radius.getValue();

            thread = new Thread(() -> {
                final List<BlockPos> blockList = new ArrayList<>();

                for (int x = -r; x < r; x++) {
                    for (int y = r; y > -r; y--) {
                        for (int z = -r; z < r; z++) {
                            final int xPos = ((int) mc.thePlayer.posX + x);
                            final int yPos = ((int) mc.thePlayer.posY + y);
                            final int zPos = ((int) mc.thePlayer.posZ + z);

                            final BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
                            final Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                            if (this.isValidBlock(block))
                                blockList.add(blockPos);
                        }
                    }
                }

                searchTimer.reset();

                synchronized (posList) {
                    posList.clear();
                    posList.addAll(blockList);
                }
            }, "BlockESP-BlockFinder");
            thread.start();
        }
    };

    public BlockESP() {
        super("Block ESP", Category.RENDER);
    }

    private boolean isValidBlock(Block block) {
        if (block == Blocks.chest && chest.getValue())
            return true;

        if (block == Blocks.skull && skull.getValue())
            return true;

        return block == Blocks.bed && bed.getValue();
    }

    public enum Mode {
        Box,
        Flat
    }

}
