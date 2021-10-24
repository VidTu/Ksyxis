package ru.vidtu.ksyxis.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import ru.vidtu.ksyxis.Ksyxis;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Redirect(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;getTotalChunksLoadedCount()I"))
	public int onPrepareStartReg_redirectChunksLoaded(ServerChunkManager scm) {
		Ksyxis.LOG.info("Not the long loadi-");
		return 441;
	}
}
