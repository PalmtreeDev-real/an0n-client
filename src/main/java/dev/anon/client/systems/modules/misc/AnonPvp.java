package dev.anon.client.systems.modules.misc;

import dev.anon.client.events.world.TickEvent;
import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.combat.*;
import dev.anon.client.systems.modules.movement.NoSlow;
import dev.anon.client.systems.modules.movement.Velocity;
import dev.anon.client.systems.modules.player.*;
import dev.anon.client.systems.modules.world.PacketMine;
import dev.anon.client.systems.modules.world.Timer;
import dev.anon.client.utils.entity.SortPriority;
import dev.anon.client.utils.entity.TargetUtils;
import dev.anon.client.utils.player.FindItemResult;
import dev.anon.client.utils.player.InvUtils;
import dev.anon.client.utils.player.PlayerUtils;
import dev.anon.client.utils.player.Rotations;
import dev.anon.client.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class AnonPvp extends Module {
    private static final Random RANDOM = new Random();

    public enum Skillset {
        None, LT5, LT4, LT3, LT2, LT1, HT1
    }

    public enum PvpMode {
        AutoPVP, AutoCrystal, PVPBot, AutoMace, AutoCart
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgTiming = settings.createGroup("Timing");
    private final SettingGroup sgHumanization = settings.createGroup("Humanization");

    private final Setting<Skillset> skillset = sgGeneral.add(new EnumSetting.Builder<Skillset>()
        .name("skillset")
        .description("PvP skillset tier.")
        .defaultValue(Skillset.None)
        .build()
    );

    private final Setting<PvpMode> mode = sgGeneral.add(new EnumSetting.Builder<PvpMode>()
        .name("mode")
        .description("The PVP mode to use.")
        .defaultValue(PvpMode.AutoPVP)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The attack range.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(8)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate towards the target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to select the target.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Integer> minCps = sgTiming.add(new IntSetting.Builder()
        .name("min-cps")
        .description("Minimum clicks per second.")
        .defaultValue(4)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> maxCps = sgTiming.add(new IntSetting.Builder()
        .name("max-cps")
        .description("Maximum clicks per second.")
        .defaultValue(8)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> reactionDelay = sgHumanization.add(new IntSetting.Builder()
        .name("reaction-delay")
        .description("Ticks before first attack after target appears.")
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> missChance = sgHumanization.add(new IntSetting.Builder()
        .name("miss-chance")
        .description("Percentage chance to miss a hit (0 = never miss).")
        .defaultValue(5)
        .min(0)
        .sliderMax(50)
        .build()
    );

    private final Setting<Boolean> randomStrafe = sgHumanization.add(new BoolSetting.Builder()
        .name("random-strafe")
        .description("Randomly strafe left/right while closing in.")
        .defaultValue(true)
        .build()
    );

    private Entity target;
    private int nextActionTick;
    private int reactionTicks;
    private int strafeTicks;
    private boolean strafeDir;
    private float aimOffsetYaw;
    private float aimOffsetPitch;
    private int aimChangeTicks;

    public AnonPvp() {
        super(Categories.Misc, "an0n-pvp", "All-in-one PVP module with humanized combat.");
    }

    @Override
    public void onActivate() {
        target = null;
        nextActionTick = 0;
        reactionTicks = 0;
        strafeTicks = 0;
        aimOffsetYaw = 0;
        aimOffsetPitch = 0;
        aimChangeTicks = 0;
        if (skillset.get() != Skillset.None) applySkillset(skillset.get());
    }

    @Override
    public void onDeactivate() {
        releaseMovementKeys();
        target = null;
        if (skillset.get() != Skillset.None) undoSkillset();
    }

    private void releaseMovementKeys() {
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
    }

    private Predicate<Entity> entityPredicate() {
        return entity -> {
            if (entity == mc.player) return false;
            if (!entities.get().contains(entity.getType())) return false;
            if (entity instanceof LivingEntity living && !living.isAlive()) return false;
            return true;
        };
    }

    private void applySkillset(Skillset tier) {
        if (tier == Skillset.None) return;
        applyCombat(tier);
        applyMovement(tier);
        applyPlayer(tier);
        applyWorld(tier);
        info("Applied %s skillset.", tier.name());
    }

    private void toggleModule(Module module, boolean on) {
        if (module == null) return;
        if (on && !module.isActive()) module.toggle();
        else if (!on && module.isActive()) module.toggle();
    }

    private void setSetting(Module module, String name, Object value) {
        if (module == null) return;
        Setting<?> setting = module.settings.get(name);
        if (setting != null) setting.parse(String.valueOf(value));
    }

    private void applyCombat(Skillset tier) {
        KillAura ka = Modules.get().get(KillAura.class);
        Criticals crits = Modules.get().get(Criticals.class);
        AutoTotem totem = Modules.get().get(AutoTotem.class);
        AutoArmor armor = Modules.get().get(AutoArmor.class);
        Surround surround = Modules.get().get(Surround.class);
        AutoWeapon weapon = Modules.get().get(AutoWeapon.class);
        Hitboxes hitboxes = Modules.get().get(Hitboxes.class);
        BowAimbot bow = Modules.get().get(BowAimbot.class);
        AutoEXP exp = Modules.get().get(AutoEXP.class);
        Offhand offhand = Modules.get().get(Offhand.class);

        switch (tier) {
            case LT5 -> {
                toggleModule(ka, true);
                if (ka != null) {
                    setSetting(ka, "range", 6.0);
                    setSetting(ka, "walls-range", 6.0);
                    setSetting(ka, "rotation", "Always");
                    setSetting(ka, "auto-switch", true);
                    setSetting(ka, "max-targets", 1);
                }
                toggleModule(crits, true);
                if (crits != null) setSetting(crits, "mode", "Packet");
                toggleModule(totem, true);
                toggleModule(armor, true);
                toggleModule(surround, true);
                toggleModule(weapon, true);
                toggleModule(hitboxes, true);
                if (hitboxes != null) setSetting(hitboxes, "value", 1.0);
                toggleModule(bow, true);
                if (bow != null) setSetting(bow, "range", 40.0);
                toggleModule(exp, true);
                toggleModule(offhand, true);
            }
            case LT4 -> {
                toggleModule(ka, true);
                if (ka != null) {
                    setSetting(ka, "range", 5.5);
                    setSetting(ka, "walls-range", 5.0);
                    setSetting(ka, "rotation", "Always");
                    setSetting(ka, "auto-switch", true);
                }
                toggleModule(crits, true);
                if (crits != null) setSetting(crits, "mode", "Packet");
                toggleModule(totem, true);
                toggleModule(armor, true);
                toggleModule(surround, true);
                toggleModule(weapon, true);
                toggleModule(hitboxes, true);
                if (hitboxes != null) setSetting(hitboxes, "value", 0.7);
                toggleModule(bow, true);
                if (bow != null) setSetting(bow, "range", 30.0);
                toggleModule(exp, true);
                toggleModule(offhand, true);
            }
            case LT3 -> {
                toggleModule(ka, true);
                if (ka != null) {
                    setSetting(ka, "range", 4.8);
                    setSetting(ka, "walls-range", 4.0);
                    setSetting(ka, "rotation", "OnHit");
                    setSetting(ka, "auto-switch", true);
                }
                toggleModule(crits, true);
                if (crits != null) setSetting(crits, "mode", "Packet");
                toggleModule(totem, true);
                toggleModule(armor, true);
                toggleModule(surround, true);
                toggleModule(weapon, true);
                toggleModule(hitboxes, true);
                if (hitboxes != null) setSetting(hitboxes, "value", 0.5);
                toggleModule(bow, false);
                toggleModule(exp, false);
                toggleModule(offhand, true);
            }
            case LT2 -> {
                toggleModule(ka, true);
                if (ka != null) {
                    setSetting(ka, "range", 4.5);
                    setSetting(ka, "walls-range", 3.5);
                    setSetting(ka, "rotation", "OnHit");
                    setSetting(ka, "auto-switch", false);
                }
                toggleModule(crits, true);
                if (crits != null) setSetting(crits, "mode", "Packet");
                toggleModule(totem, true);
                toggleModule(armor, true);
                toggleModule(surround, true);
                toggleModule(weapon, true);
                toggleModule(hitboxes, false);
                toggleModule(bow, false);
                toggleModule(exp, false);
                toggleModule(offhand, true);
            }
            case LT1 -> {
                toggleModule(ka, true);
                if (ka != null) {
                    setSetting(ka, "range", 4.3);
                    setSetting(ka, "walls-range", 3.0);
                    setSetting(ka, "rotation", "None");
                    setSetting(ka, "auto-switch", false);
                }
                toggleModule(crits, true);
                if (crits != null) setSetting(crits, "mode", "Packet");
                toggleModule(totem, true);
                toggleModule(armor, true);
                toggleModule(surround, false);
                toggleModule(weapon, true);
                toggleModule(hitboxes, false);
                toggleModule(bow, false);
                toggleModule(exp, false);
                toggleModule(offhand, true);
            }
            case HT1 -> {
                toggleModule(ka, false);
                toggleModule(crits, true);
                if (crits != null) setSetting(crits, "mode", "Packet");
                toggleModule(totem, true);
                toggleModule(armor, true);
                toggleModule(surround, false);
                toggleModule(weapon, true);
                toggleModule(hitboxes, false);
                toggleModule(bow, false);
                toggleModule(exp, false);
                toggleModule(offhand, true);
            }
        }
    }

    private void applyMovement(Skillset tier) {
        Velocity velo = Modules.get().get(Velocity.class);
        NoSlow noSlow = Modules.get().get(NoSlow.class);
        Timer timer = Modules.get().get(Timer.class);

        switch (tier) {
            case LT5 -> {
                toggleModule(velo, true);
                if (velo != null) {
                    setSetting(velo, "knockback", true);
                    setSetting(velo, "knockback-horizontal", 0.0);
                    setSetting(velo, "knockback-vertical", 0.0);
                    setSetting(velo, "explosions", true);
                    setSetting(velo, "explosions-horizontal", 0.0);
                    setSetting(velo, "explosions-vertical", 0.0);
                }
                toggleModule(noSlow, true);
                toggleModule(timer, true);
                if (timer != null) setSetting(timer, "multiplier", 1.2);
            }
            case LT4 -> {
                toggleModule(velo, true);
                if (velo != null) {
                    setSetting(velo, "knockback", true);
                    setSetting(velo, "knockback-horizontal", 0.0);
                    setSetting(velo, "knockback-vertical", 0.0);
                    setSetting(velo, "explosions", true);
                    setSetting(velo, "explosions-horizontal", 0.3);
                    setSetting(velo, "explosions-vertical", 0.3);
                }
                toggleModule(noSlow, true);
                toggleModule(timer, true);
                if (timer != null) setSetting(timer, "multiplier", 1.1);
            }
            case LT3 -> {
                toggleModule(velo, true);
                if (velo != null) {
                    setSetting(velo, "knockback", true);
                    setSetting(velo, "knockback-horizontal", 0.3);
                    setSetting(velo, "knockback-vertical", 0.3);
                    setSetting(velo, "explosions", true);
                    setSetting(velo, "explosions-horizontal", 0.5);
                    setSetting(velo, "explosions-vertical", 0.5);
                }
                toggleModule(noSlow, true);
                toggleModule(timer, false);
            }
            case LT2 -> {
                toggleModule(velo, true);
                if (velo != null) {
                    setSetting(velo, "knockback", true);
                    setSetting(velo, "knockback-horizontal", 0.5);
                    setSetting(velo, "knockback-vertical", 0.5);
                    setSetting(velo, "explosions", true);
                    setSetting(velo, "explosions-horizontal", 0.7);
                    setSetting(velo, "explosions-vertical", 0.7);
                }
                toggleModule(noSlow, true);
                toggleModule(timer, false);
            }
            case LT1 -> {
                toggleModule(velo, true);
                if (velo != null) {
                    setSetting(velo, "knockback", true);
                    setSetting(velo, "knockback-horizontal", 0.7);
                    setSetting(velo, "knockback-vertical", 0.7);
                    setSetting(velo, "explosions", false);
                }
                toggleModule(noSlow, true);
                toggleModule(timer, false);
            }
            case HT1 -> {
                toggleModule(velo, false);
                toggleModule(noSlow, false);
                toggleModule(timer, false);
            }
        }
    }

    private void applyPlayer(Skillset tier) {
        Reach reach = Modules.get().get(Reach.class);
        AntiHunger hunger = Modules.get().get(AntiHunger.class);
        SpeedMine speedMine = Modules.get().get(SpeedMine.class);
        NoMiningTrace trace = Modules.get().get(NoMiningTrace.class);
        AutoGap autoGap = Modules.get().get(AutoGap.class);
        AutoClicker clicker = Modules.get().get(AutoClicker.class);
        FastUse fastUse = Modules.get().get(FastUse.class);
        AutoTool autoTool = Modules.get().get(AutoTool.class);
        AutoEat autoEat = Modules.get().get(AutoEat.class);

        switch (tier) {
            case LT5 -> {
                toggleModule(reach, true);
                if (reach != null) setSetting(reach, "extra-entity-reach", 3.0);
                toggleModule(hunger, true);
                toggleModule(speedMine, true);
                toggleModule(trace, true);
                toggleModule(autoGap, true);
                toggleModule(clicker, true);
                toggleModule(fastUse, true);
                toggleModule(autoTool, true);
                toggleModule(autoEat, true);
            }
            case LT4 -> {
                toggleModule(reach, true);
                if (reach != null) setSetting(reach, "extra-entity-reach", 2.5);
                toggleModule(hunger, true);
                toggleModule(speedMine, true);
                toggleModule(trace, true);
                toggleModule(autoGap, true);
                toggleModule(clicker, true);
                toggleModule(fastUse, true);
                toggleModule(autoTool, true);
                toggleModule(autoEat, true);
            }
            case LT3 -> {
                toggleModule(reach, true);
                if (reach != null) setSetting(reach, "extra-entity-reach", 2.0);
                toggleModule(hunger, true);
                toggleModule(speedMine, true);
                toggleModule(trace, false);
                toggleModule(autoGap, true);
                toggleModule(clicker, false);
                toggleModule(fastUse, false);
                toggleModule(autoTool, true);
                toggleModule(autoEat, true);
            }
            case LT2 -> {
                toggleModule(reach, true);
                if (reach != null) setSetting(reach, "extra-entity-reach", 1.7);
                toggleModule(hunger, true);
                toggleModule(speedMine, true);
                toggleModule(trace, false);
                toggleModule(autoGap, true);
                toggleModule(clicker, false);
                toggleModule(fastUse, false);
                toggleModule(autoTool, true);
                toggleModule(autoEat, true);
            }
            case LT1 -> {
                toggleModule(reach, false);
                toggleModule(hunger, true);
                toggleModule(speedMine, false);
                toggleModule(trace, false);
                toggleModule(autoGap, true);
                toggleModule(clicker, false);
                toggleModule(fastUse, false);
                toggleModule(autoTool, true);
                toggleModule(autoEat, true);
            }
            case HT1 -> {
                toggleModule(reach, false);
                toggleModule(hunger, false);
                toggleModule(speedMine, false);
                toggleModule(trace, false);
                toggleModule(autoGap, true);
                toggleModule(clicker, false);
                toggleModule(fastUse, false);
                toggleModule(autoTool, true);
                toggleModule(autoEat, true);
            }
        }
    }

    private void applyWorld(Skillset tier) {
        PacketMine packetMine = Modules.get().get(PacketMine.class);
        if (tier == Skillset.LT5 || tier == Skillset.LT4 || tier == Skillset.LT3 || tier == Skillset.LT2) {
            toggleModule(packetMine, true);
        } else {
            toggleModule(packetMine, false);
        }
    }

    private void undoSkillset() {
        toggleModule(Modules.get().get(KillAura.class), false);
        toggleModule(Modules.get().get(Criticals.class), false);
        toggleModule(Modules.get().get(AutoTotem.class), false);
        toggleModule(Modules.get().get(AutoArmor.class), false);
        toggleModule(Modules.get().get(Surround.class), false);
        toggleModule(Modules.get().get(AutoWeapon.class), false);
        toggleModule(Modules.get().get(Hitboxes.class), false);
        toggleModule(Modules.get().get(BowAimbot.class), false);
        toggleModule(Modules.get().get(AutoEXP.class), false);
        toggleModule(Modules.get().get(Offhand.class), false);
        toggleModule(Modules.get().get(Velocity.class), false);
        toggleModule(Modules.get().get(NoSlow.class), false);
        toggleModule(Modules.get().get(Timer.class), false);
        toggleModule(Modules.get().get(Reach.class), false);
        toggleModule(Modules.get().get(AntiHunger.class), false);
        toggleModule(Modules.get().get(SpeedMine.class), false);
        toggleModule(Modules.get().get(NoMiningTrace.class), false);
        toggleModule(Modules.get().get(AutoGap.class), false);
        toggleModule(Modules.get().get(AutoClicker.class), false);
        toggleModule(Modules.get().get(FastUse.class), false);
        toggleModule(Modules.get().get(AutoTool.class), false);
        toggleModule(Modules.get().get(AutoEat.class), false);
        toggleModule(Modules.get().get(PacketMine.class), false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;
        if (!isActive()) return;

        target = TargetUtils.get(entityPredicate(), priority.get());
        if (target == null) {
            if (mode.get() == PvpMode.PVPBot) releaseMovementKeys();
            nextActionTick = 0;
            reactionTicks = 0;
            return;
        }

        double dist = PlayerUtils.distanceTo(target);
        if (dist > range.get() + 1.0) {
            if (mode.get() == PvpMode.PVPBot) releaseMovementKeys();
            return;
        }

        if (nextActionTick > 0) {
            nextActionTick--;
            if (mode.get() == PvpMode.PVPBot) doPvpBotMovement(dist);
            return;
        }

        int min = Math.max(1, minCps.get());
        int max = Math.max(min, maxCps.get());
        int delay = Math.max(1, 20 / (min + RANDOM.nextInt(max - min + 1)));
        nextActionTick = delay + (RANDOM.nextInt(3) - 1);
        if (nextActionTick < 1) nextActionTick = 1;

        switch (mode.get()) {
            case AutoPVP -> doAutoPvp(dist);
            case AutoCrystal -> doAutoCrystal();
            case PVPBot -> doPvpBot(dist);
            case AutoMace -> doAutoMace();
            case AutoCart -> doAutoCart();
        }
    }

    private boolean shouldMiss() {
        return missChance.get() > 0 && RANDOM.nextInt(100) < missChance.get();
    }

    private void doAutoPvp(double distance) {
        if (!(target instanceof LivingEntity living)) return;
        if (living instanceof ArmorStand) return;

        if (distance > range.get()) return;

        updateAim();

        if (rotate.get()) {
            double yaw = Rotations.getYaw(living);
            double pitch = Rotations.getPitch(living);
            Rotations.rotate(yaw + aimOffsetYaw, pitch + aimOffsetPitch);
        }

        if (shouldMiss()) return;

        mc.gameMode.attack(mc.player, living);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private void doAutoCrystal() {
        if (!(target instanceof Player)) return;

        FindItemResult crystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!crystal.found()) return;

        BlockPos targetPos = target.blockPosition().above();
        if (!mc.level.getBlockState(targetPos).isAir()) return;

        InvUtils.swap(crystal.slot(), true);

        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target));
        }

        BlockUtils.place(targetPos, crystal, true, 0, true);
    }

    private void doPvpBot(double distance) {
        if (!(target instanceof Player)) return;

        if (reactionTicks < reactionDelay.get()) {
            releaseMovementKeys();
            reactionTicks++;
            return;
        }

        doPvpBotMovement(distance);
        updateAim();

        if (rotate.get()) {
            double yaw = Rotations.getYaw(target);
            double pitch = Rotations.getPitch(target);
            Rotations.rotate(yaw + aimOffsetYaw, pitch + aimOffsetPitch);
        }

        if (distance < range.get() && mc.player.getAttackStrengthScale(0.5f) >= 0.85f) {
            if (shouldMiss()) return;

            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void doPvpBotMovement(double distance) {
        if (mc.player.getVehicle() != null) {
            double dx = target.getX() - mc.player.getX();
            double dz = target.getZ() - mc.player.getZ();
            mc.player.getVehicle().setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
            return;
        }

        if (distance > 1.8) {
            mc.options.keyUp.setDown(true);
            mc.options.keyDown.setDown(false);

            if (randomStrafe.get()) {
                strafeTicks--;
                if (strafeTicks <= 0) {
                    strafeDir = RANDOM.nextBoolean();
                    strafeTicks = 5 + RANDOM.nextInt(15);
                }
                mc.options.keyLeft.setDown(strafeDir);
                mc.options.keyRight.setDown(!strafeDir);
            }

            if (distance > 2.5 && RANDOM.nextInt(100) < 15) {
                mc.options.keyJump.setDown(true);
            } else {
                mc.options.keyJump.setDown(false);
            }

            if (RANDOM.nextInt(100) < 5) {
                mc.options.keyUp.setDown(false);
            }
        } else {
            mc.options.keyUp.setDown(false);
            mc.options.keyDown.setDown(false);
            mc.options.keyLeft.setDown(false);
            mc.options.keyRight.setDown(false);

            if (RANDOM.nextInt(100) < 3) {
                mc.options.keyJump.setDown(true);
            } else {
                mc.options.keyJump.setDown(false);
            }
        }
    }

    private void doAutoMace() {
        if (!(target instanceof LivingEntity living)) return;
        if (living instanceof ArmorStand) return;

        ItemStack mainHand = mc.player.getMainHandItem();
        if (!(mainHand.getItem() instanceof MaceItem)) {
            FindItemResult mace = InvUtils.findInHotbar(item -> item.getItem() instanceof MaceItem);
            if (mace.found()) InvUtils.swap(mace.slot(), true);
            else return;
        }

        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(living), Rotations.getPitch(living));
        }

        if (mc.player.fallDistance > 2.5) {
            mc.gameMode.attack(mc.player, living);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void doAutoCart() {
        if (!(target instanceof Player)) return;

        FindItemResult tntCart = InvUtils.findInHotbar(Items.TNT_MINECART);
        if (!tntCart.found()) return;

        InvUtils.swap(tntCart.slot(), true);

        BlockPos placePos = target.blockPosition().above(2);
        if (!mc.level.getBlockState(placePos).isAir()) {
            placePos = target.blockPosition().above(3);
        }

        if (mc.level.getBlockState(placePos).isAir()) {
            BlockUtils.place(placePos, tntCart, true, 0, true);
        }
    }

    private void updateAim() {
        aimChangeTicks--;
        if (aimChangeTicks <= 0) {
            aimOffsetYaw = (RANDOM.nextFloat() - 0.5f) * 4.0f;
            aimOffsetPitch = (RANDOM.nextFloat() - 0.5f) * 2.0f;
            aimChangeTicks = 5 + RANDOM.nextInt(10);
        }
    }
}
