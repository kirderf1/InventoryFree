package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.capability.LockedInvHandler;
import kirderf1.inventoryfree.capability.ModCapabilities;
import kirderf1.inventoryfree.network.LockedInvSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Handles capabilities on client-side by copying over capability data on respawn
 * and handling data sync packets.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class ClientCapabilityHandler
{
	
	@SubscribeEvent
	private static void onClientRespawn(ClientPlayerNetworkEvent.Clone event)
	{
		LockedInvHandler.copyOverCap(event.getOldPlayer(), event.getNewPlayer());
	}
	
	public static void handleLockedInvPacket(LockedInvSyncPacket packet)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
			player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv ->
					lockedInv.deserializeNBT(packet.getNbt()));
	}
}