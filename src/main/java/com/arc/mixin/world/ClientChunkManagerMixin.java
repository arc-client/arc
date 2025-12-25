
package com.arc.mixin.world;

import com.arc.event.EventFlow;
import com.arc.event.events.WorldEvent;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    @Final
    @Shadow
    ClientWorld world;

    @Inject(method = "loadChunkFromPacket", at = @At("TAIL"))
    private void onChunkLoad(
            int x, int z, PacketByteBuf buf, Map<Heightmap.Type, long[]> heightmaps, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir
    ) {
        EventFlow.post(new WorldEvent.ChunkEvent.Load(cir.getReturnValue()));
    }

    @Inject(method = "loadChunkFromPacket", at = @At(value = "NEW", target = "net/minecraft/world/chunk/WorldChunk", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onChunkUnload(int x, int z, PacketByteBuf buf, Map<Heightmap.Type, long[]> heightmaps, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir, int i, WorldChunk chunk, ChunkPos chunkPos) {
        if (chunk != null) {
            EventFlow.post(new WorldEvent.ChunkEvent.Unload(chunk));
        }
    }

    @Inject(method = "unload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager$ClientChunkMap;unloadChunk(ILnet/minecraft/world/chunk/WorldChunk;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onChunkUnload(ChunkPos pos, CallbackInfo ci, int i, WorldChunk chunk) {
        EventFlow.post(new WorldEvent.ChunkEvent.Unload(chunk));
    }

//    @Inject(
//            method = "updateLoadDistance",
//            at = @At(
//                    value = "INVOKE",
//                    target = "net/minecraft/client/world/ClientChunkManager$ClientChunkMap.isInRadius(II)Z"
//            ),
//            locals = LocalCapture.CAPTURE_FAILHARD
//    )
//    private void onUpdateLoadDistance(
//            int loadDistance,
//            CallbackInfo ci,
//            int oldRadius,
//            int newRadius,
//            ClientChunkManager.ClientChunkMap clientChunkMap,
//            int k,
//            WorldChunk oldChunk,
//            ChunkPos chunkPos
//    ) {
//        if (!clientChunkMap.isInRadius(chunkPos.x, chunkPos.z)) {
//            EventFlow.post(new WorldEvent.ChunkEvent.Unload(this.world, oldChunk));
//        }
//    }
}
