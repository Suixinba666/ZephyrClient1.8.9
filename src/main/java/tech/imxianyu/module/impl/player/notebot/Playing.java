package tech.imxianyu.module.impl.player.notebot;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.module.impl.player.NoteBot;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.rotation.RotationUtils;

import javax.sound.midi.*;
import java.io.File;
import java.util.*;

public class Playing extends SubModule<NoteBot> {

    public Playing() {
        super("Playing");
    }

    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public final List<Note> notes = Collections.synchronizedList(new ArrayList<>());
    public double msPerTick = 0;
    public long startTime = System.currentTimeMillis();

    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre() || notes.isEmpty()) {
            return;
        }

        Iterator<Note> it = notes.iterator();

        while (it.hasNext()) {
            Note next = it.next();

            long elapsed = System.currentTimeMillis() - startTime;

            if (next.tick <= elapsed) {
                this.play(next.note);
                it.remove();
            }
        }
    }

    public void play(NoteBot.NoteConstant note) {
        List<List<BlockPos>> layers = this.getModule().noteBlocks;

        if (layers.isEmpty()) {
            return;
        }

        for (List<BlockPos> layer : layers) {
            for (int i = 0; i < layer.size(); i++) {

                BlockPos noteBlock = layer.get(i);

                int pitchAtPos = this.getModule().getPitchAtPos(noteBlock);

                Material material = mc.theWorld.getBlockState(noteBlock.down()).getBlock().getMaterial();

                if (material == Material.wood)
                {
                    pitchAtPos -= 24;
                }

                if (material == Material.glass)
                {
                    pitchAtPos += 24;
                }

                if (note.getPitch() == pitchAtPos) {

                    float[] rotations = RotationUtils.getRotations(noteBlock.getX() + 0.5, noteBlock.getY() + 0.5, noteBlock.getZ() + 0.5);
                    MovingObjectPosition rayTrace = PlayerUtils.rayTrace(rotations[0], rotations[1], 10, mc.timer.renderPartialTicks);

                    mc.playerController.clickBlock(noteBlock, rayTrace.sideHit);
                    mc.thePlayer.swingItem();

//                    float f = (float)Math.pow(2.0D, (double)(pitchAtPos - 12) / 12.0D);
//                    mc.thePlayer.playSound("note.harp", 10.0F, f);
//                    mc.theWorld.spawnParticle(EnumParticleTypes.NOTE, (double)noteBlock.getX() + 0.5D, (double)noteBlock.getY() + 1.2D, (double)noteBlock.getZ() + 0.5D, (double)pitchAtPos / 24.0D, 0.0D, 0.0D);

                    break;
                }
            }
        }
    }

    @SneakyThrows
    public void play(File file) {
        notes.clear();
        Sequence sequence = MidiSystem.getSequence(file);

        Synthesizer synthesizer = MidiSystem.getSynthesizer();

        Instrument[] instruments = null;
        Soundbank sb = synthesizer.getDefaultSoundbank();
        if (sb!=null) instruments = synthesizer.getDefaultSoundbank().getInstruments();

        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
//            System.out.println("Track " + trackNumber + ": size = " + track.size());
//            System.out.println();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
//                System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
//                    System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();

                        notes.add(new Note(event.getTick() * msPerTick, NoteBot.NoteConstant.getNoteByNameAndOctave(noteName, octave)));
                    }

                    if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        if (instruments != null) {
                            Instrument instrument = instruments[sm.getData1()];
                            System.out.println(instrument);
                        }
                    }

                } else if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    if(mm.getType() == 0x51 /*set tempo*/){
                        byte[] data = mm.getData();
                        int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                        double bpm = (double) (60000000 / tempo) + 1;
                        msPerTick = (60000 / (bpm * sequence.getResolution()));
                    }
                }
            }
        }

        startTime = System.currentTimeMillis();
    }

    public class Note {
        @Getter
        private final NoteBot.NoteConstant note;

        @Getter
        private final double tick;

        public Note(double tick, NoteBot.NoteConstant note) {
            this.tick = tick;
            this.note = note;
        }
    }

    public void stop() {
        this.notes.clear();
    }
}
