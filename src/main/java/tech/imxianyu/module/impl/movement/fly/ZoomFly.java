package tech.imxianyu.module.impl.movement.fly;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.events.packet.SendPacketEvent;
import tech.imxianyu.events.player.MoveEvent;
import tech.imxianyu.events.player.StepEvent;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.impl.movement.Fly;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.timing.Timer;

import java.util.ArrayList;
import java.util.List;

public class ZoomFly extends SubModule<Fly> {

    public ZoomFly() {
        super("ZoomFly");
    }

    @Handler
    public void onRecv(ReceivePacketEvent e) {
        if (e.getPacket() instanceof S12PacketEntityVelocity || e.getPacket() instanceof S27PacketExplosion) {
            e.setCancelled();
        }
    };
    @Handler
    public void onSend(SendPacketEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer) {
            ((C03PacketPlayer) event.getPacket()).onGround = false;
        }
    };
    private final ArrayList<Packet> packetList = new ArrayList();
    private final boolean failedStart = false;
    private final int bHS1 = 0;
    private final Timer flyTimer = new Timer();
    private final Timer c13timer = new Timer();
    @Handler
    public void onTick(TickEvent event) {
        if (this.c13timer.isDelayed(899 + this.randomNumber(0, 5))) {
            this.c13timer.reset();
        }
    };
    private final Timer dmgTimer = new Timer();
    private final Timer disTimer = new Timer();
    private final List<C0FPacketConfirmTransaction> c0fs = new ArrayList();
    private final boolean tp = false;
    public NumberSetting<Double> zoomSpeed = new NumberSetting<>("ZoomFly Speed", 1.0, 0.1, 4.0, 0.1);
    public BooleanSetting jumpStart = new BooleanSetting("Jump Start", false);
    public BooleanSetting decrease = new BooleanSetting("Decrease", false);
    int level = 0;
    int Const;
    float y;
    float bypass;
    int bypassstage;
    Minecraft mc = Minecraft.getMinecraft();
    @Handler
    public void onStep(StepEvent evnet) {
        mc.thePlayer.stepHeight = 0.0F;

    };
    private boolean hasgo = false;
    private boolean failedStart1 = false;
    private boolean noPacketModify;
    private int boostHypixelState = 0;
    private double moveSpeed;
    private double lastDistance;
    @Handler
    public void onMove(MoveEvent e) {
        if (!PlayerUtils.isMoving2()) {
            e.setX(0.0);
            e.setZ(0.0);
        } else if (!this.failedStart) {
            double baseSpeed = 0.29;
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                baseSpeed *= 1.0 + 0.2 * (double) (amplifier + 1);
            }

            switch (this.boostHypixelState) {
                case 1:
                    this.moveSpeed = baseSpeed;
                    this.moveSpeed *= Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed) ? 1.56 : 2.034;
                    this.boostHypixelState = 2;
                    break;
                case 2:
                    this.moveSpeed *= zoomSpeed.getValue();
                    this.boostHypixelState = 3;
                    break;
                case 3:
                    this.moveSpeed = this.lastDistance - (mc.thePlayer.ticksExisted % 2 == 0 ? 0.0123 : 0.0103) * (this.lastDistance - baseSpeed);
                    this.boostHypixelState = 4;
                    break;
                case 4: {
                    if (decrease.getValue())
                        this.moveSpeed = this.lastDistance - this.lastDistance / 159.8;
                    break;
                }
            }

            this.moveSpeed = Math.max(this.moveSpeed, getBaseMovementSpeed());

            if (!ModuleManager.speed.isEnabled() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                setSpeed(e, this.moveSpeed);
            }
        }
    };
    @Handler
    public void onUpdate(UpdateEvent event) {
        if (!event.isPre()) {
            double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            this.lastDistance = Math.sqrt(xDist * xDist + zDist * zDist);
            mc.thePlayer.cameraYaw = 0.035F;
            return;
        }

        if (!this.failedStart && this.boostHypixelState > 0) {
            mc.thePlayer.motionY = 0.0;
        }

        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + (double) (this.bypass * (float) (mc.thePlayer.ticksExisted % 2 == 0 ? 0.5 : 0)), mc.thePlayer.posZ);
        if (++this.bypassstage > 5) {
            this.bypassstage = 0;
        }

        this.bypass = 1.0E-7F;
    };
    private double startY;
    private double firstX;
    private double firstY;
    private double firstZ;

    public static void setSpeed(MoveEvent moveEvent, double moveSpeed, float pseudoYaw, double pseudoStrafe, double pseudoForward) {
        double forward = pseudoForward;
        double strafe = pseudoStrafe;
        float yaw = pseudoYaw;
        if (pseudoForward == 0.0 && pseudoStrafe == 0.0) {
            moveEvent.setZ(0.0);
            moveEvent.setX(0.0);
        } else {
            if (pseudoForward != 0.0) {
                if (pseudoStrafe > 0.0) {
                    yaw = pseudoYaw + (float) (pseudoForward > 0.0 ? -45 : 45);
                } else if (pseudoStrafe < 0.0) {
                    yaw = pseudoYaw + (float) (pseudoForward > 0.0 ? 45 : -45);
                }

                strafe = 0.0;
                if (pseudoForward > 0.0) {
                    forward = 1.0;
                } else if (pseudoForward < 0.0) {
                    forward = -1.0;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw + 90.0F));
            double sin = Math.sin(Math.toRadians(yaw + 90.0F));
            moveEvent.setX(forward * moveSpeed * cos + strafe * moveSpeed * sin);
            moveEvent.setZ(forward * moveSpeed * sin - strafe * moveSpeed * cos);
        }

    }

    public void onEnable() {
        if (mc.thePlayer != null) {
            this.c0fs.clear();
            this.bypassstage = 0;
            this.firstX = mc.thePlayer.prevPosX;
            this.firstY = mc.thePlayer.prevPosY;
            this.firstZ = mc.thePlayer.prevPosZ;
            this.hasgo = true;
            this.boostHypixelState = 1;
            this.level = 0;
            this.failedStart1 = !PlayerUtils.isMoving2() && !this.isOnGround(0.0);

            if (jumpStart.getValue())
                mc.thePlayer.motionY = 0.42;


            this.flyTimer.reset();
            this.hasgo = true;
            this.noPacketModify = true;
            this.moveSpeed = 0.0;
            this.lastDistance = 0.0;
            this.startY = mc.thePlayer.posY;
            this.noPacketModify = false;
        }
    }

    public boolean isOnGround(double n) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -n, 0.0)).isEmpty();
    }

    public void setSpeed(MoveEvent moveEvent, double moveSpeed) {
        float rotationYaw = mc.thePlayer.rotationYaw;
        MovementInput movementInput = mc.thePlayer.movementInput;
        double pseudoStrafe = mc.thePlayer.movementInput.moveStrafe;
        MovementInput movementInput2 = mc.thePlayer.movementInput;
        setSpeed(moveEvent, moveSpeed, rotationYaw, pseudoStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public int randomNumber(int n, int n2) {
        return Math.round((float) n2 + (float) Math.random() * (float) (n - n2));
    }

    public double getBaseMovementSpeed() {
        double baseSpeed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double) (amplifier + 1);
        }

        return baseSpeed;
    }

    public void onDisable() {
        if (this.failedStart) {
            mc.thePlayer.setPositionAndUpdate(this.firstX, this.firstY, this.firstZ);
            mc.thePlayer.fallDistance = 0.0F;
        }

        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.capabilities.isFlying = false;
            mc.thePlayer.capabilities.allowFlying = false;
            mc.timer.timerSpeed = 1.0F;
            mc.thePlayer.stepHeight = 0.5F;
        }

        for (C0FPacketConfirmTransaction p : this.c0fs) {
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p);
        }
    }
}
