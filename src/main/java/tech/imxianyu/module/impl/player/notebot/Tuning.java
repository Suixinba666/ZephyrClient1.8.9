package tech.imxianyu.module.impl.player.notebot;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.player.NoteBot;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.rotation.RotationUtils;
import tech.imxianyu.utils.timing.Timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tuning extends SubModule<NoteBot> {

    public Tuning() {
        super("Tuning");
    }

    @Override
    public void onEnable() {
        if (mc.theWorld == null) {
            this.getModule().toggle();
            return;
        }
//        this.getModule().pitchMap.clear();
        this.tuneCountMap.clear();
        this.getModule().refreshNoteBlocks();

        List<List<BlockPos>> layers = this.getModule().noteBlocks;

        if (layers.isEmpty()) {
            return;
        }

        for (List<BlockPos> layer : layers) {
            for (int i = 0; i < layer.size(); i++) {

                BlockPos noteBlock = layer.get(i);

                int pitchAtPos = this.getModule().getPitchAtPos(noteBlock);

                while (pitchAtPos != i) {

                    this.addTuneCount(noteBlock);

                    pitchAtPos ++;
                    if (pitchAtPos > 24)
                        pitchAtPos = 0;
                }
            }
        }

    }

    private void addTuneCount(BlockPos pos) {
        this.tuneCountMap.putIfAbsent(pos, 0);

        this.tuneCountMap.replace(pos, this.tuneCountMap.get(pos) + 1);
    }

    private void delTuneCount(BlockPos pos) {
        this.tuneCountMap.putIfAbsent(pos, 0);

        this.tuneCountMap.replace(pos, this.tuneCountMap.get(pos) - 1);
    }

    private int getTuneCount(BlockPos pos) {
        this.tuneCountMap.putIfAbsent(pos, 0);

        return this.tuneCountMap.get(pos);
    }

    Timer timer = new Timer();

    Map<BlockPos, Integer> tuneCountMap = new HashMap<>();

    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        List<List<BlockPos>> layers = this.getModule().noteBlocks;

        if (layers.isEmpty()) {
            return;
        }

        int tuneCount = 0, maxTune = 2;


        for (List<BlockPos> layer : layers) {
            for (int i = 0; i < layer.size(); i++) {

                BlockPos noteBlock = layer.get(i);

                int count = this.getTuneCount(noteBlock);

                if (count > 0) {

                    if (tuneCount == maxTune)
                        break;

                    float[] rotations = RotationUtils.getRotations(noteBlock.getX() + 0.5, noteBlock.getY() + 0.5, noteBlock.getZ() + 0.5);
                    MovingObjectPosition rayTrace = PlayerUtils.rayTrace(rotations[0], rotations[1], 10, mc.timer.renderPartialTicks);

                    mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), noteBlock, rayTrace.sideHit, blockDataToVec3(noteBlock, rayTrace.sideHit));
                    mc.thePlayer.swingItem();
                    tuneCount ++;


                    this.delTuneCount(noteBlock);
                }

            }

            if (tuneCount == maxTune)
                break;
        }
    }

    private Vec3 blockDataToVec3(BlockPos paramBlockPos, EnumFacing paramEnumFacing) {
        double d1 = paramBlockPos.getX() + 0.5D;
        double d2 = paramBlockPos.getY() + 0.5D;
        double d3 = paramBlockPos.getZ() + 0.5D;
        d1 += paramEnumFacing.getFrontOffsetX() / 2.0D;
        d3 += paramEnumFacing.getFrontOffsetZ() / 2.0D;
        d2 += paramEnumFacing.getFrontOffsetY() / 2.0D;
        return new Vec3(d1, d2, d3);
    }

}
