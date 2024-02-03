package kirderf1.inventoryfree;

import kirderf1.inventoryfree.locked_inventory.LockedInvHandler;
import kirderf1.inventoryfree.network.UnlockedSlotsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Handles the player-specific value for unlocked slots,
 * by providing accessors and syncing it to client by
 * sending a {@link UnlockedSlotsPayload} at appropriate times.
 * It also handles loss of unlocks on death.
 */
@Mod.EventBusSubscriber
public class PlayerData
{
	@SubscribeEvent
	private static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		int unlockedSlots = getUnlockedSlots(player);
		player.connection.send(new UnlockedSlotsPayload(unlockedSlots));
	}
	
	@SubscribeEvent
	private static void lockSlotsOnRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		if(event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer player))
			return;
		
		int unlockedSlots = getUnlockedSlots(player);
		if(InventoryFree.CONFIG.unlockedLostOnDeath.get() != 0 && unlockedSlots != 0)
		{
			if(InventoryFree.CONFIG.unlockedLostOnDeath.get() < 0)
				setUnlockedSlots(player, 0);
			else
				setUnlockedSlots(player, Mth.clamp(unlockedSlots - InventoryFree.CONFIG.unlockedLostOnDeath.get(), 0, unlockedSlots));
		}
	}
	
	/**
	 * Returns the number of available slots based on the player and their game type.
	 */
	public static int getAvailableSlots(ServerPlayer player)
	{
		if(InventoryFree.appliesTo(player))
			return InventoryFree.getAvailableSlots(getUnlockedSlots(player));
		else return 36;
	}
	
	/**
	 * Returns the number of available slots based on the player and the provided game type.
	 */
	public static int getAvailableSlots(ServerPlayer player, GameType gameMode)
	{
		if(InventoryFree.appliesTo(gameMode))
			return InventoryFree.getAvailableSlots(getUnlockedSlots(player));
		else return 36;
	}
	
	/**
	 * Gets the number of unlocked slots for a player.
	 */
	public static int getUnlockedSlots(ServerPlayer player)
	{
		return player.getData(InventoryFree.UNLOCKED_SLOTS);
	}
	
	/**
	 * Changes the number of unlocked slots for a player, sends it to client-side,
	 * and notifies {@link LockedInvHandler} that there may have been a change.
	 */
	public static void unlockSlots(ServerPlayer player, int amount)
	{
		setUnlockedSlots(player, player.getData(InventoryFree.UNLOCKED_SLOTS) + amount);
	}
	
	/**
	 * Changes the number of unlocked slots for a player, sends it to client-side,
	 * and notifies {@link LockedInvHandler} that there may have been a change.
	 */
	public static void setUnlockedSlots(ServerPlayer player, int unlockedSlots)
	{
		player.setData(InventoryFree.UNLOCKED_SLOTS, unlockedSlots);
		player.connection.send(new UnlockedSlotsPayload(unlockedSlots));
		LockedInvHandler.onLockChange(player);
	}
}
