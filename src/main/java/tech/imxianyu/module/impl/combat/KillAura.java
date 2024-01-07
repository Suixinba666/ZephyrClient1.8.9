package tech.imxianyu.module.impl.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import tech.imxianyu.eventapi.EnumPriority;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.eventapi.Priority;
import tech.imxianyu.events.RespawnEvent;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.events.rendering.Render3DEvent;
import tech.imxianyu.events.rendering.RenderPlayerRotationsEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.rotation.RotationUtils;
import tech.imxianyu.utils.timing.Timer;
import tech.imxianyu.widget.impl.keystrokes.CPSUtils;

import java.util.*;
import java.util.stream.Collectors;

public class KillAura extends Module {

    public static NumberSetting<Float> range = new NumberSetting<>("Range", 4.2f, 1.0f, 6.0f, 0.1f);
    public static NumberSetting<Float> fov = new NumberSetting<>("FoV", 60.0f, 1.0f, 360.0f, 10f);
    private final Timer delayTimer = new Timer();
    private final Timer cpsTimer = new Timer();
    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Single);
    public NumberSetting<Integer> multiLimit = new NumberSetting<>("Multi Attack Limit", 5, 0, 20, 1, () -> this.mode.getValue() == Mode.Multi);

    public ModeSetting<CPSMode> cpsMode = new ModeSetting<>("CPS Mode", CPSMode.Normal);

    public enum CPSMode {
        Normal,
        HurtTime;
    }

    public NumberSetting<Integer> hurtTime = new NumberSetting<>("Hurt Time", 0, 0, 20, 1, () -> this.cpsMode.getValue() == CPSMode.HurtTime);

    public NumberSetting<Double> minCPS = new NumberSetting<Double>("Min CPS", 7.0d, 1.0d, 40.0d, 0.1d, () -> cpsMode.getValue() == CPSMode.Normal) {
        @Override
        public void onValueChanged(Double last, Double now) {
            if (now > maxCPS.getValue()) {
                this.setValue(maxCPS.getValue());
            }
        }
    };
    public NumberSetting<Double> maxCPS = new NumberSetting<Double>("Max CPS", 9.0d, 1.0d, 40.0d, 0.1d, () -> cpsMode.getValue() == CPSMode.Normal) {
        @Override
        public void onValueChanged(Double last, Double now) {
            if (now < minCPS.getValue()) {
                this.setValue(minCPS.getValue());
            }
        }
    };
    public ModeSetting<TargetSwitchMode> targetSwitchMode = new ModeSetting<>("Target Switch Mode", TargetSwitchMode.Health);
    public ModeSetting<RotationMode> rotationMode = new ModeSetting<>("Rotation Mode", RotationMode.Static, () -> this.mode.getValue() != Mode.Multi);
    public ModeSetting<BlockMode> blockMode = new ModeSetting<>("Block Mode", BlockMode.Normal);
    public NumberSetting<Integer> switchDelay = new NumberSetting<>("Switch Delay", 200, 1, 1000, 1, () -> this.mode.getValue() == Mode.Switch);
    public NumberSetting<Double> rotationSpeed = new NumberSetting<>("Rotation Speed", 0.2, 0.01, 1.0, 0.01, () -> this.mode.getValue() != Mode.Multi);
    public BooleanSetting players = new BooleanSetting("Attack Players", true);
    public BooleanSetting mobs = new BooleanSetting("Attack Mobs", false);
    public BooleanSetting animals = new BooleanSetting("Attack Animals", false);
    public BooleanSetting villagers = new BooleanSetting("Attack Villagers", false);
    public BooleanSetting screenCheck = new BooleanSetting("Screen Check", false);
    public BooleanSetting canBeSeen = new BooleanSetting("Entity be seen", false);
    public BooleanSetting autoBlock = new BooleanSetting("Auto Block", false);
    public BooleanSetting autoDisable = new BooleanSetting("Auto Disable", false);
    public BooleanSetting teams = new BooleanSetting("Teams", false);
    public BooleanSetting noSwing = new BooleanSetting("No Swing", false);
    public BooleanSetting noRotate = new BooleanSetting("No Rotate", false, () -> this.mode.getValue() != Mode.Multi);

    public EntityLivingBase target;

    @Handler
    public void onRender3D(Render3DEvent event) {
        if (this.mode.getValue() == Mode.Single || this.mode.getValue() == Mode.Switch) {
            this.doRenderESP(this.target);
        } else {
            for (EntityLivingBase tar : this.targets) {
                this.doRenderESP(tar);
            }
        }
    }

    private void doRenderESP(EntityLivingBase targ) {
        if (targ != null) {
            double x = targ.lastTickPosX + (targ.posX - targ.lastTickPosX)
                    * (double) mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
            double y = targ.lastTickPosY + (targ.posY - targ.lastTickPosY)
                    * (double) mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY;
            double z = targ.lastTickPosZ + (targ.posZ - targ.lastTickPosZ)
                    * (double) mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;

            double width = targ.getEntityBoundingBox().maxX
                    - targ.getEntityBoundingBox().minX - 0.2;
            double height = targ.getEntityBoundingBox().maxY
                    - targ.getEntityBoundingBox().minY + 0.05;
            float red;
            float green;
            float blue = 0.0f;
            if (targ.hurtTime > 5) {
                red = 1.0f;
                green = 0.0f;
            } else {
                red = 0.0f;
                green = 1.0f;
            }
            RenderSystem.drawEntityESP(x, y, z, width, height, red, green, blue, 0.2f, red, green, blue, 1, 2);
        }
    }

    public List<EntityLivingBase> targets = new ArrayList<>();
    public float[] smoothRotation = new float[2], lastRotation = new float[2];
    @Handler
    @Priority(priority = EnumPriority.LOWEST)
    public void onRotation(RenderPlayerRotationsEvent event) {
        if (target != null && !noRotate.getValue()) {
            float[] rotations = smoothRotation;
            event.rotationYaw = rotations[0];
            event.rotationPitch = rotations[1];
        }
    };

    @Handler
    @Priority(priority = EnumPriority.LOWEST)
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre())
            return;

        StringBuilder sb = new StringBuilder();

        sb.append(mode.getValue()).append(", ");
        sb.append(targetSwitchMode.getValue()).append(", ");
        sb.append("Min: ").append(minCPS.getValue()).append(", ");
        sb.append("Max: ").append(maxCPS.getValue()).append(", ");
        sb.append("R: ").append(range.getValue()).append(", ");
        sb.append("F: ").append(fov.getValue()).append(", ");

        if (players.getValue())
            sb.append("P").append(", ");

        if (mobs.getValue())
            sb.append("M").append(", ");

        if (animals.getValue())
            sb.append("A").append(", ");

        if (villagers.getValue())
            sb.append("V").append(", ");

        if (autoBlock.getValue())
            sb.append("AB").append(", ");

        if (noSwing.getValue())
            sb.append("NS").append(", ");

        if (noRotate.getValue())
            sb.append("NR").append(", ");

        this.setSuffix(sb.substring(0, sb.toString().length() - 2));

        if (this.mode.getValue() == Mode.Multi)
            return;

        if (target != null) {
            if (smoothRotation == null) {
                smoothRotation = getRotationsEntity(target);
            }

            float[] rotations = getRotationsEntity(target);
            float speed = rotationSpeed.getFloatValue();
            /*smoothRotation[0] = AnimationSystem.interpolate(smoothRotation[0], smoothRotation[0] + MathHelper.wrapAngleTo180_float(rotations[0] - smoothRotation[0] - 5), speed);
            smoothRotation[1] = AnimationSystem.interpolate(smoothRotation[1], rotations[1], speed);*/
            smoothRotation = rotations;


            if (rotationMode.getValue() == RotationMode.Static) {
                event.setRotationYaw(smoothRotation[0]);
                event.setRotationPitch(smoothRotation[1]);
            } else {
                mc.thePlayer.rotationYaw = smoothRotation[0];
                mc.thePlayer.rotationPitch = smoothRotation[1];
            }

            lastRotation = smoothRotation;
        } else {
            if (smoothRotation == null) {
                smoothRotation = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
            }

            float speed = rotationSpeed.getFloatValue();
            smoothRotation[0] = AnimationSystem.interpolate(smoothRotation[0], smoothRotation[0] + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - smoothRotation[0]), speed);
            smoothRotation[1] = AnimationSystem.interpolate(smoothRotation[1], mc.thePlayer.rotationPitch, speed);

        }
    };
    private long lastDelay = randomClickDelay(minCPS.getValue(), maxCPS.getValue());
    public boolean blocking = false;
    @Handler
    public void onRespawn(RespawnEvent event) {
        if (this.autoDisable.getValue()) {
            if (autoBlock.getValue() && this.canBlock()) {
                this.unBlock();
            }
            this.setEnabled(false);
        }
    };
    @Handler
    public void onReceive(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            String text = ((S02PacketChat) event.getPacket()).getChatComponent().getUnformattedText();
            if ((text.startsWith("DEATH!") || text.startsWith("死亡!") || text.startsWith("死亡！")) && autoDisable.getValue()) {
                if (autoBlock.getValue() && this.canBlock()) {
                    this.unBlock();
                }
                this.setEnabled(false);
            }
        }
    };
    private int entityIdx = 0;

    public KillAura() {
        super("Kill Aura", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        target = null;

        if (mc.thePlayer != null) {
            smoothRotation[0] = mc.thePlayer.rotationYaw;
            smoothRotation[1] = mc.thePlayer.rotationPitch;
        }

        lastRotation = smoothRotation;

        this.lastDelay = 1L;
    }

    @Override
    public void onDisable() {
        target = null;

        if (autoBlock.getValue()) {
            this.unBlock();
        }

        KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }

    public void visualSwing() {
        if (!mc.thePlayer.isSwingInProgress || mc.thePlayer.swingProgressInt >= mc.thePlayer.getArmSwingAnimationEnd() / 2 || mc.thePlayer.swingProgressInt < 0) {
            mc.thePlayer.swingProgressInt = -1;
            mc.thePlayer.isSwingInProgress = true;
        }
    }

    public boolean canUnBlock() {
        return (this.mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking() || mc.thePlayer.itemInUseCount != 0);
    }

    private boolean canBlock() {
        return this.mc.thePlayer.getCurrentEquippedItem() != null && this.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword;
    }
    @Handler
    public void attack(Render2DEvent event) {

        if (!this.isEnabled())
            return;

        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (mc.playerController.getCurBlockDamageMP() != 0F) {
            return;
        }

        if (mc.currentScreen != null && screenCheck.getValue())
            return;

//        this.setSuffix(this.mode.getCurMode());

        if (target == null && canBlock() && autoBlock.getValue() && !mc.gameSettings.keyBindUseItem.pressed)
            this.unBlock();

        if (mc.thePlayer.getHealth() <= 0.0f && autoDisable.getValue()) {
            if (autoBlock.getValue() && this.canBlock()) {
                this.unBlock();
            }
//            this.toggle();
            this.setEnabled(false);
        }


        this.targets = this.getTarget();

        if (targets.isEmpty()) {
            this.target = null;
            return;
        } else {
            switch (this.mode.getValue()) {
                case Multi, Single -> {
                    this.target = this.targets.get(0);
                }
                case Switch -> {
                    if (delayTimer.isDelayed(this.switchDelay.getValue())) {
                        delayTimer.reset();
                        entityIdx++;
                    }

                    if (entityIdx > targets.size() - 1) {
                        entityIdx = 0;
                    }

                    this.target = this.targets.get(entityIdx);
                }
            }
        }

        if (mc.thePlayer.getDistanceToEntity(this.target) > range.getValue() || target.isDead || !target.isEntityAlive() || target.getHealth() <= 0.0F) {
            target = null;
        }

        if (this.mode.getValue() == Mode.Multi) {
            mc.playerController.syncCurrentPlayItem();

            List<EntityLivingBase> entityLivingBases = this.targets;

            if (this.cpsMode.getValue() == CPSMode.Normal) {
                if (cpsTimer.isDelayed(lastDelay)) {
                    mc.leftClickCounter = 0;
                    cpsTimer.reset();
                    if (!this.noSwing.getValue()) {
                        mc.thePlayer.swingItem();
                    } else {
                        this.visualSwing();
                    }
                    CPSUtils.addLeftCPS();
                    this.lastDelay = this.randomClickDelay(this.minCPS.getValue(), this.maxCPS.getValue());
                    if (canBlock() && autoBlock.getValue())
                        this.unBlock();

                    for (int i = 0; i < entityLivingBases.size(); i++) {

                        if (this.multiLimit.getValue() != 0 && i > this.multiLimit.getValue())
                            break;

                        EntityLivingBase entityLivingBase = entityLivingBases.get(i);
                        target = entityLivingBase;
                        mc.thePlayer.onCriticalHit(entityLivingBase);
                        if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), entityLivingBase.getCreatureAttribute()) > 0F)
                            mc.thePlayer.onEnchantmentCritical(entityLivingBase);
                        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

                    }

                    if (canBlock() && this.target != null && autoBlock.getValue()) {
                        block();
                    }
                }
            } else if (!this.targets.isEmpty()) {
                mc.leftClickCounter = 0;
                if (!this.noSwing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    this.visualSwing();
                }
                CPSUtils.addLeftCPS();
                if (canBlock() && autoBlock.getValue())
                    this.unBlock();

                for (int i = 0; i < entityLivingBases.size(); i++) {

                    EntityLivingBase entityLivingBase = entityLivingBases.get(i);
                    target = entityLivingBase;

                    if (this.multiLimit.getValue() != 0 && i > this.multiLimit.getValue())
                        break;

                    if (entityLivingBase.hurtResistantTime <= this.hurtTime.getValue() && entityLivingBase.attackTimer.isDelayed(100)) {
                        mc.thePlayer.onCriticalHit(entityLivingBase);
                        if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), entityLivingBase.getCreatureAttribute()) > 0F)
                            mc.thePlayer.onEnchantmentCritical(entityLivingBase);
                        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                        entityLivingBase.attackTimer.reset();
                    }

                }

                if (canBlock() && this.target != null && autoBlock.getValue()) {
                    block();
                }
            }

        } else {
            if (cpsTimer.isDelayed(lastDelay) && target != null) {
                mc.leftClickCounter = 0;
                cpsTimer.reset();
                if (!this.noSwing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    this.visualSwing();
                }
                CPSUtils.addLeftCPS();
                this.lastDelay = this.randomClickDelay(this.minCPS.getValue(), this.maxCPS.getValue());
//            mc.thePlayer.addChatMessage(String.valueOf(this.lastDelay));
                if (canBlock() && autoBlock.getValue())
                    this.unBlock();

                mc.thePlayer.onCriticalHit(target);
                if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.getHeldItem(), target.getCreatureAttribute()) > 0F)
                    mc.thePlayer.onEnchantmentCritical(target);
                mc.playerController.syncCurrentPlayItem();
                mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

                if (canBlock() && this.target != null && autoBlock.getValue()) {
                    block();
                }
            }
        }

    }

    public void block() {
        if (canBlock() && (!canUnBlock() || !blocking)) {
            blocking = true;
//            mc.thePlayer.addChatMessage("Block! " + new Random().nextInt(500));
            mc.thePlayer.sendQueue.getNetworkManager()
                    .sendPacketNoEvent(new C08PacketPlayerBlockPlacement(
                            blockMode.getValue() == BlockMode.Hypixel ? new BlockPos(-1, -1, -1) : BlockPos.ORIGIN, 255,
                            mc.thePlayer.getHeldItem(), 0.0f, 0.0f, 0.0f));
            (mc.thePlayer).itemInUseCount = (mc.thePlayer.getHeldItem().getMaxItemUseDuration());
        }
    }

    private void unBlock() {
        if (mc.thePlayer != null && canBlock() && (canUnBlock() || blocking)) {
            blocking = false;
//            mc.thePlayer.addChatMessage("UnBlock! " + new Random().nextInt(500));

            mc.thePlayer.itemInUseCount = 0;
//            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, blockMode.getValue() == BlockMode.Hypixel ? new BlockPos(-1, -1, -1) : BlockPos.ORIGIN, EnumFacing.DOWN));
//            this.mc.playerController.onStoppedUsingItem(this.mc.thePlayer);
            mc.thePlayer.clearItemInUse();
        }
    }

    public float[] getRotationsEntity(Entity entity) {
        return getRotations(entity.posX/* + MathUtils.randomNumber(0.2, -0.2)*/, entity.posY + (double) entity.getEyeHeight() - 0.4/* + MathUtils.randomNumber(0.3, -0.3)*/, entity.posZ/* + MathUtils.randomNumber(0.3, -0.5)*/);
    }

    public float[] getRotations(double posX, double posY, double posZ) {
        EntityPlayerSP player = mc.thePlayer;
        double x = posX - player.posX;
        double y = posY - (player.posY + (double) player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float aYaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float aPitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
//        float pitch = changeRotation(this.mc.thePlayer.rotationPitch, aPitch, vSpeed);
//        float yaw = changeRotation(this.mc.thePlayer.rotationYaw, aYaw, hSpeed);
        return new float[]{aYaw, aPitch};
    }

    public long randomClickDelay(double minCPS, double maxCPS) {
        double random = new Random().nextInt((int) ((maxCPS + 0.01 - minCPS) * 1000)) / 1000.0;
        double cps = minCPS + random;
        return ((long) (10000 / Math.max(minCPS, cps))) / 10;
    }

    private boolean isValidToAttack(EntityLivingBase entity) {
        if (entity == null || entity.isDead || entity.getHealth() <= 0)
            return false;

        if (entity == mc.thePlayer)
            return false;

        //range
        if (!(mc.thePlayer.getDistanceToEntity(entity) <= range.getValue()))
            return false;


        //fov
        if (fov.getValue() != 360f && !RotationUtils.isVisibleFOV(entity, fov.getValue()))
            return false;

        //bots (last)
        if (ModuleManager.antiBots.isEnabled() && AntiBots.isBot(entity))
            return false;

        if (teams.getValue() && isOnSameTeam(entity))
            return false;

        if (canBeSeen.getValue() && !mc.thePlayer.canEntityBeSeen(entity))
            return false;

        if (!players.getValue() && entity instanceof EntityPlayer)
            return false;

        if (!animals.getValue() && entity instanceof EntityAnimal)
            return false;

        if (!mobs.getValue() && entity instanceof EntityMob)
            return false;

        if (!villagers.getValue() && entity instanceof EntityVillager)
            return false;

        return !(entity instanceof EntityArmorStand);
    }

    public boolean isOnSameTeam(Entity entity) {
        if (Minecraft.getMinecraft().thePlayer.getDisplayName().getUnformattedText().startsWith("\247")) {
            if (Minecraft.getMinecraft().thePlayer.getDisplayName().getUnformattedText().length() <= 2
                    || entity.getDisplayName().getUnformattedText().length() <= 2) {
                return false;
            }
            return Minecraft.getMinecraft().thePlayer.getDisplayName().getUnformattedText().substring(0, 2).equals(entity.getDisplayName().getUnformattedText().substring(0, 2));
        }
        return false;
    }

    private Comparator<EntityLivingBase> getComparator() {
        return switch (targetSwitchMode.getValue()) {
            case Distance -> Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e));
            case Health -> (o1, o2) -> {

                boolean bot1 = o1.getDisplayName().getUnformattedText().equals("bot");
                boolean bot2 = o2.getDisplayName().getUnformattedText().equals("bot");

                if (bot1 || bot2) {

                    if (bot1 && bot2)
                        return 0;
                    else if (bot1)
                        return 1;
                } else {
                    return Float.compare(o1.getHealth(), o2.getHealth());
                }

                return 0;
            };
            case Armor ->
                    Comparator.comparingInt(o -> o instanceof EntityPlayer ? ((EntityPlayer) o).inventory.getTotalArmorValue() : (int) o.getHealth());
            case HurtTime -> Comparator.comparingInt(o -> o.hurtResistantTime);
        };
    }

    private List<EntityLivingBase> getTarget() {
        /*return mc.theWorld.loadedEntityList.stream().filter(this::isValidToAttack).sorted(
                this.getComparator()
        ).collect(Collectors.toList());*/

        List<EntityLivingBase> result = new ArrayList<>();

        for (Entity ent : mc.theWorld.loadedEntityList) {
            if (!(ent instanceof EntityLivingBase entity))
                continue;

            if (this.isValidToAttack(entity)) {
                result.add(entity);
            }
        }

        return result.stream().sorted(this.getComparator()).collect(Collectors.toList());
    }


    public enum Mode {
        Single,
        Switch,
        Multi
    }

    public enum TargetSwitchMode {
        HurtTime,
        Distance,
        Health,
        Armor
    }

    public enum RotationMode {
        Static, LockView
    }


    public enum BlockMode {
        Hypixel,
        Normal
    }









}
