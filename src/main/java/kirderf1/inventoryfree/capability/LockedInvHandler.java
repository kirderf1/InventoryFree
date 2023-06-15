package kirderf1.inventoryfree.capability;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.PlayerData;
import kirderf1.inventoryfree.network.LockedInvSyncPacket;
import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

/**
 * Handles events and functionality surrounding ILockedInventory
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class LockedInvHandler
{
	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		onLockChange(player);
		
		if(InventoryFree.CONFIG.dropItemsInLockedSlots.get())
			dropLockedInvItems(player);
		
		sendLockedInv(player);
	}
	
	// Perform this early as it adds drops that should be on equal stance to the player inventory
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onDrops(LivingDropsEvent event)
	{
		LivingEntity entity = event.getEntity();
		if(entity instanceof ServerPlayer player
				&& !entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
		{
			player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv -> {
				// Add drops from capability
				Collection<ItemEntity> oldCapture = player.captureDrops(event.getDrops());
				lockedInv.getAndClearStacks().forEach(stack -> {
					// Only add droppable items
					if(!stack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(stack))
						player.drop(stack, true, false);
				});
				player.captureDrops(oldCapture);
				sendLockedInv(player);
			});
		}
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		copyOverCap(event.getOriginal(), event.getEntity());
	}
	
	public static void copyOverCap(Player oldPlayer, Player newPlayer)
	{
		oldPlayer.reviveCaps();
		oldPlayer.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(oldInv ->
				newPlayer.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(newInv -> {
					
					ListTag nbt = oldInv.serializeNBT();
					newInv.deserializeNBT(nbt);
				})
		);
		oldPlayer.invalidateCaps();
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		sendLockedInv((ServerPlayer) event.getEntity());
	}
	
	@SubscribeEvent
	public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event)
	{
		ServerPlayer player = (ServerPlayer) event.getEntity();
		onLockChange(player, PlayerData.getAvailableSlots(player, event.getNewGameMode()));
	}
	
	/**
	 * Called when the mod config changes. Has to be called on the server-thread
	 */
	public static void onConfigReload(MinecraftServer server)
	{
		for(ServerPlayer player : server.getPlayerList().getPlayers())
			onLockChange(player);
		
		if(InventoryFree.CONFIG.dropItemsInLockedSlots.get())
		{
			for(ServerPlayer player : server.getPlayerList().getPlayers())
				dropLockedInvItems(player);
		}
	}
	
	/**
	 * Should be called anytime that the number of inventory slots available to a player *might* have changed.
	 * This function should then move items between the ILockedInventory and the player inventory when relevant.
	 * Should be called when:
	 * - Number of unlocked slots change for a player
	 * - Slot locking starts/stops being applied to a player (game mode changes)
	 * - Config value might've changed
	 *   - On config reload, for all online players
	 *   - On player login, to cover those that might've not been online on config reload
	 */
	public static void onLockChange(ServerPlayer player)
	{
		onLockChange(player,  PlayerData.getAvailableSlots(player));
	}
	
	// Slots provided as an argument for situations where this is called during an event before the player state has properly updated
	public static void onLockChange(ServerPlayer player, int slots)
	{
		int prevSlots = getPrevAvailableSlots(player);
		if(slots != prevSlots)
		{
			if(slots > prevSlots)
				onUnlockSlot(player, prevSlots, slots - 1);
			if(slots < prevSlots && !InventoryFree.CONFIG.dropItemsInLockedSlots.get())
				onLockSlot(player, slots, prevSlots - 1);
			
			PlayerData.getOrCreatePersistentTag(player).putInt("slot_cache", slots);
		}
	}
	
	private static int getPrevAvailableSlots(ServerPlayer player)
	{
		CompoundTag nbt = PlayerData.getPersistentTag(player);
		return nbt.contains("slot_cache", Tag.TAG_ANY_NUMERIC)
				? Mth.clamp(nbt.getInt("slot_cache"), 0, 36)
				: 36;
	}
	
	/**
	 * Called when slots have been locked. Moves items in those slots to the ILockedInventory
	 */
	private static void onLockSlot(ServerPlayer player, int slotMin, int slotMax)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv -> {
			boolean changed = false;
			for(int slot = slotMin; slot <= slotMax; slot++)
			{
				if(lockedInv.getStack(slot).isEmpty())
				{
					ItemStack stack = player.getInventory().removeItemNoUpdate(slot);
					lockedInv.putStack(slot, stack);
					changed |= !stack.isEmpty();
				}
			}
			if(changed)
				sendLockedInv(player);
		});
	}
	
	/**
	 * Called when slots have been unlocked. Moves items from the ILockedInventory to those slots
	 */
	private static void onUnlockSlot(ServerPlayer player, int slotMin, int slotMax)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv -> {
			boolean changed = false;
			for(int slot = slotMin; slot <= slotMax; slot++)
			{
				ItemStack stack = lockedInv.takeStack(slot);
				if(!stack.isEmpty())
				{
					if(player.getInventory().getItem(slot).isEmpty())
						player.getInventory().setItem(slot, stack);
					else player.drop(stack, true, false);
					changed = true;
				}
			}
			if(changed)
				sendLockedInv(player);
		});
	}
	
	private static void dropLockedInvItems(ServerPlayer player)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv ->
		{
			boolean changed = false;
			for(ItemStack stack : lockedInv.getAndClearStacks())
			{
				player.drop(stack, true, false);
				changed = true;
			}
			if(changed)
				sendLockedInv(player);
		});
	}
	
	public static void sendLockedInv(ServerPlayer player)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv ->
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), LockedInvSyncPacket.makePacket(lockedInv)));
	}
}