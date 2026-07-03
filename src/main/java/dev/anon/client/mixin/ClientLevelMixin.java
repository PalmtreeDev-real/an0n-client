/*
 * This file is part of the AN0N Client distribution (https://github.com/Palmtreedev-real/AN0N).
 * Copyright (c) 2026 AtlasDevMC.
 */

package dev.anon.client.mixin;

import dev.anon.client.AnonClient;
import dev.anon.client.events.entity.EntityAddedEvent;
import dev.anon.client.events.entity.EntityRemovedEvent;
import dev.anon.client.systems.modules.Modules;
import dev.anon.client.systems.modules.render.NoRender;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Shadow
    @Nullable
    public abstract Entity getEntity(int id);

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null) AnonClient.EVENT_BUS.post(EntityAddedEvent.get(entity));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        if (getEntity(id) != null)
            AnonClient.EVENT_BUS.post(EntityRemovedEvent.get(getEntity(id)));
    }

    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void onAddDestroyBlockEffect(BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noParticle(ParticleTypes.BLOCK)) ci.cancel();
    }

    @Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
    private void onAddBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noParticle(ParticleTypes.BLOCK)) ci.cancel();
    }

    @ModifyArgs(method = "animateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;doAnimateTick(IIIILnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos$MutableBlockPos;)V"))
    private void doRandomBlockDisplayTicks(Args args) {
        if (Modules.get().get(NoRender.class).noBarrierInvis()) {
            args.set(5, Blocks.BARRIER);
        }
    }


}
