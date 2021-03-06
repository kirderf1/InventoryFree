package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.capability.LockedInvHandler;
import kirderf1.inventoryfree.capability.ModCapabilities;
import kirderf1.inventoryfree.network.LockedInvSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class ClientCapabilityHandler
{
	
	@SubscribeEvent
	public static void onClientRespawn(ClientPlayerNetworkEvent.RespawnEvent event)
	{
		LockedInvHandler.copyOverCap(event.getOldPlayer(), event.getNewPlayer());
	}
	
	public static void handleLockedInvPacket(LockedInvSyncPacket packet)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null)
			player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv ->
					ModCapabilities.LOCKED_INV_CAPABILITY.readNBT(lockedInv, null, packet.getNbt()));
	}
}