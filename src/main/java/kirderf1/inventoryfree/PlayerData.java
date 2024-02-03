package kirderf1.inventoryfree;

import kirderf1.inventoryfree.locked_inventory.LockedInvHandler;
import kirderf1.inventoryfree.network.UnlockedSlotsPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
	private static void onClone(PlayerEvent.Clone event)
	{
		int unlockedSlots = getUnlockedSlots((ServerPlayer) event.getEntity());
		if(event.isWasDeath() && InventoryFree.CONFIG.unlockedLostOnDeath.get() != 0
				&& unlockedSlots != 0)
		{
			if(InventoryFree.CONFIG.unlockedLostOnDeath.get() < 0)
				setUnlockedSlots((ServerPlayer) event.getEntity(), 0);
			else setUnlockedSlots((ServerPlayer) event.getEntity(), Math.min(unlockedSlots,
					Math.max(0, unlockedSlots - InventoryFree.CONFIG.unlockedLostOnDeath.get())));
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
		return getPersistentTag(player).getInt("unlocked_slots");
	}
	
	/**
	 * Changes the number of unlocked slots for a player, sends it to client-side,
	 * and notifies {@link LockedInvHandler} that there may have been a change.
	 */
	public static void unlockSlots(ServerPlayer player, int amount)
	{
		CompoundTag nbt = getOrCreatePersistentTag(player);
		nbt.putInt("unlocked_slots", nbt.getInt("unlocked_slots") + amount);
		player.connection.send(new UnlockedSlotsPayload(nbt.getInt("unlocked_slots")));
		LockedInvHandler.onLockChange(player);
	}
	
	/**
	 * Changes the number of unlocked slots for a player, sends it to client-side,
	 * and notifies {@link LockedInvHandler} that there may have been a change.
	 */
	public static void setUnlockedSlots(ServerPlayer player, int unlockedSlots)
	{
		getOrCreatePersistentTag(player).putInt("unlocked_slots", unlockedSlots);
		player.connection.send(new UnlockedSlotsPayload(unlockedSlots));
		LockedInvHandler.onLockChange(player);
	}
	
	public static CompoundTag getPersistentTag(ServerPlayer player)
	{
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(InventoryFree.MOD_ID);
	}
	public static CompoundTag getOrCreatePersistentTag(ServerPlayer player)
	{
		return getOrCreate(getOrCreate(player.getPersistentData(), Player.PERSISTED_NBT_TAG), InventoryFree.MOD_ID);
	}
	private static CompoundTag getOrCreate(CompoundTag nbt, String str)
	{
		if(!nbt.contains(str, Tag.TAG_COMPOUND))
			nbt.put(str, new CompoundTag());
		return nbt.getCompound(str);
	}
}