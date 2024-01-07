package tech.imxianyu.module.impl.player;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNote;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.world.WorldChangedEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.player.notebot.Playing;
import tech.imxianyu.module.impl.player.notebot.Tuning;
import tech.imxianyu.module.impl.render.Perspective;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.timing.Timer;

import java.util.*;

public class NoteBot extends Module {

    public NoteBot() {
        super("NoteBot", Category.PLAYER);
        super.addSubModules(new Tuning(), new Playing());
    }


    public NumberSetting<Integer> radius = new NumberSetting<>("Radius", 5, 1, 10, 1);

    public final Timer searchTimer = new Timer();
    public Map<BlockPos, Integer> pitchMap = new HashMap<>();
    public List<List<BlockPos>> noteBlocks = new ArrayList<>();

    @Handler
    public void onRespawn(WorldChangedEvent event) {
        pitchMap.clear();
    }



    public int getPitchAtPos(BlockPos pos) {
        if (!pitchMap.containsKey(pos))
            pitchMap.put(pos, 0);

        return pitchMap.get(pos);
    }

    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        this.setSuffix(this.getCurrentSubModule().getName());

        if (searchTimer.isDelayed(1000, true)) {
            this.refreshNoteBlocks();
        }
    }

    public void refreshNoteBlocks() {
        this.noteBlocks = this.getNoteBlocksNearBy();
    }

    @Handler
    public void onRender2D(Render2DEvent event) {
//        FontManager.pf18.drawString("Layers: " + this.noteBlocks.size(), 50, 50, -1);
    }

    @Handler
    public void onRender3D(Render3DEvent event) {

        int r = 255, g = 255, b = 255;

        for (int i = 0; i < this.noteBlocks.size(); i++) {
            List<BlockPos> layer = this.noteBlocks.get(i);

            for (BlockPos noteBlock : layer) {

                setup3DTransforms(noteBlock);

                RenderSystem.color(RenderSystem.hexColor(r, g, b));

                Block block = mc.theWorld.getBlockState(noteBlock).getBlock();

                if (!(block instanceof BlockNote)) {
                    dispose();
                    continue;
                }

                String msg = "Normal";

                Material material = mc.theWorld.getBlockState(noteBlock.down()).getBlock().getMaterial();

                if (material == Material.wood)
                {
                    msg = "Low";
                }

                if (material == Material.glass)
                {
                    msg = "High";
                }

                int pitch = getPitchAtPos(noteBlock);

                FontManager.pf18.drawCenteredString(NoteConstant.getNoteByPitch(pitch).getDisplayName(), 0, -FontManager.pf18.getHeight() * 0.5, RenderSystem.hexColor(r, g, b));
                FontManager.pf18.drawCenteredString(msg, 0, FontManager.pf18.getHeight() * 0.5, RenderSystem.hexColor(r, g, b));

                dispose();
            }
        }


    }

    private void setup3DTransforms(BlockPos noteBlock) {
        GlStateManager.pushMatrix();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0F, -1500000.0F);

        double posX = noteBlock.getX() - mc.getRenderManager().renderPosX + 0.5;
        double posY = noteBlock.getY() - mc.getRenderManager().renderPosY + 1.5;
        double posZ = noteBlock.getZ() - mc.getRenderManager().renderPosZ + 0.5;

        GlStateManager.translate(posX, posY, posZ);
        boolean flag = this.mc.gameSettings.thirdPersonView == 2;
        GlStateManager.rotate(-Perspective.getCameraYaw() + (flag ? 180 : 0), 0.0F, 1.0F, 0.0F);
        float var10001 = flag ? -1.0F : 1.0F;
        GlStateManager.rotate(Perspective.getCameraPitch(), var10001, 0.0F, 0.0F);
        double scale = 0.0125;
        GlStateManager.scale(-scale, -scale, -scale);
    }

    private void dispose() {
        GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
    }

    private List<List<BlockPos>> getNoteBlocksNearBy() {
        List<List<BlockPos>> result = new ArrayList<>();
        int radius = this.radius.getValue();
        for(int y = radius; y >= -radius; --y) {
            List<BlockPos> layer = new ArrayList<>();
            for (int x = -radius; x < radius; ++x) {
                for (int z = -radius; z < radius; ++z) {
                    int posX = (int) Math.floor(this.mc.thePlayer.posX) + x;
                    int posY = (int) Math.floor(this.mc.thePlayer.posY) + y;
                    int posZ = (int) Math.floor(this.mc.thePlayer.posZ) + z;

                    BlockPos bp = new BlockPos(posX, posY, posZ);

                    if (mc.theWorld.getBlockState(bp).getBlock() instanceof BlockNote)
                        layer.add(bp);
                }
            }
            if (!layer.isEmpty())
                result.add(layer);
        }

        Collections.reverse(result);

        return result;
    }

    public enum NoteConstant {
        C2(-18, "C2"),
        C_2(-17, "C#2"),
        D2(-16, "D2"),
        D_2(-15, "D#2"),
        E2(-14, "E2"),

        F2(-13, "F2"),
        F_2(-12, "F#2"),
        G2(-11, "G2"),
        G_2(-10, "G#2"),
        A2(-9, "A2"),
        A_2(-8, "A#2"),
        B2(-7, "B2"),
        C3(-6, "C3"),
        C_3(-5, "C#3"),
        D3(-4, "D3"),
        D_3(-3, "D#3"),
        E3(-2, "E3"),

        F3(-1, "F3"),
        F_3(0, "F#3"),
        G3(1, "G3"),
        G_3(2, "G#3"),
        A3(3, "A3"),
        A_3(4, "A#3"),
        B3(5, "B3"),
        C4(6, "C4"),
        C_4(7, "C#4"),
        D4(8, "D4"),
        D_4(9, "D#4"),
        E4(10, "E4"),
        F4(11, "F4"),
        F_4(12, "F#4"),
        G4(13, "G4"),
        G_4(14, "G#4"),
        A4(15, "A4"),
        A_4(16, "A#4"),
        B4(17, "B4"),
        C5(18, "C5"),
        C_5(19, "C#5"),
        D5(20, "D5"),
        D_5(21, "D#5"),
        E5(22, "E5"),
        F5(23, "F5"),
        F_5(24, "F#5"),
        G5(25, "G5"),
        G_5(26, "G#5"),
        A5(27, "A5"),
        A_5(28, "A#5"),
        B5(29, "B5"),
        C6(30, "C6"),
        C_6(31, "C#6"),
        D6(32, "D6"),
        D_6(33, "D#6"),
        E6(34, "E6"),
        F6(35, "F6"),
        F_6(36, "F#6"),
        G6(37, "G6"),
        G_6(38, "G#6"),
        A6(39, "A6"),
        A_6(40, "A#6"),
        B6(41, "B6");

        @Getter
        private final int pitch;
        @Getter
        private final String displayName;
        NoteConstant(int pitch, String displayName) {
            this.pitch = pitch;
            this.displayName = displayName;
        }

        public static NoteConstant getNoteByPitch(int pitch) {
            for (NoteConstant value : NoteConstant.values()) {
                if (value.getPitch() == pitch)
                    return value;
            }

            return null;
        }

        public static NoteConstant getNoteByName(String name) {
            for (NoteConstant value : NoteConstant.values()) {
                if (Objects.equals(value.getDisplayName(), name))
                    return value;
            }

            return null;
        }

        public static NoteConstant getNoteByNameAndOctave(String name, int octave) {

            NoteConstant result = null;
            boolean find = false;

            for (NoteConstant value : NoteConstant.values()) {
                if (Objects.equals(value.getDisplayName(), name + octave)) {
                    find = true;
                    result = value;
                    break;
                }
            }

            if (!find) {
                for (int i = 3; i < 5; i++) {
                    for (NoteConstant value : NoteConstant.values()) {
                        if (Objects.equals(value.getDisplayName(), name + i)) {
                            result = value;
                            break;
                        }
                    }
                }
            }

            return result;
        }
    }

}
