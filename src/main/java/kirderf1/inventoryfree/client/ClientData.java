package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.network.LockedInvSyncPayload;
import kirderf1.inventoryfree.network.UnlockedSlotsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side version of {@link kirderf1.inventoryfree.PlayerData}.
 * Stores the number of unlocked slots for use client-side.
 */
public class ClientData
{
	private static int unlockedSlots = 0;
	
	public static int getAvailableSlots()
	{
		return InventoryFree.getAvailableSlots(unlockedSlots);
	}
	
	public static int getUnlockedSlots()
	{
		return unlockedSlots;
	}
	
	public static void handle(UnlockedSlotsPayload payload)
	{
		unlockedSlots = payload.unlockedSlots();
	}
	
	public static void handle(LockedInvSyncPayload payload)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
			player.getData(InventoryFree.LOCKED_INVENTORY).deserializeNBT(player.registryAccess(), payload.nbt());
	}
}
