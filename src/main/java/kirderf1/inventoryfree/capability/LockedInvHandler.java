package kirderf1.inventoryfree.capability;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.PlayerData;
import kirderf1.inventoryfree.network.LockedInvSyncPacket;
import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

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
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		
		onLockChange(player);
		
		if(InventoryFree.CONFIG.dropItemsInLockedSlots.get())
			dropLockedInvItems(player);
		
		sendLockedInv(player);
	}
	
	// Perform this early as it adds drops that should be on equal stance to the player inventory
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onDrops(LivingDropsEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(entity instanceof ServerPlayerEntity && !entity.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
		{
			ServerPlayerEntity player = (ServerPlayerEntity) entity;
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
		copyOverCap(event.getOriginal(), event.getPlayer());
	}
	
	public static void copyOverCap(PlayerEntity oldPlayer, PlayerEntity newPlayer)
	{
		oldPlayer.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(oldInv ->
				newPlayer.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(newInv -> {
					
					INBT nbt = ModCapabilities.LOCKED_INV_CAPABILITY.writeNBT(oldInv, null);
					ModCapabilities.LOCKED_INV_CAPABILITY.readNBT(newInv, null, nbt);
				})
		);
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		sendLockedInv((ServerPlayerEntity) event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onGamemodeChange(PlayerEvent.PlayerChangeGameModeEvent event)
	{
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		onLockChange(player, PlayerData.getAvailableSlots(player, event.getNewGameMode()));
	}
	
	/**
	 * Called when the mod config changes. Has to be called on the server-thread
	 */
	public static void onConfigReload(MinecraftServer server)
	{
		for(ServerPlayerEntity player : server.getPlayerList().getPlayers())
			onLockChange(player);
		
		if(InventoryFree.CONFIG.dropItemsInLockedSlots.get())
		{
			for(ServerPlayerEntity player : server.getPlayerList().getPlayers())
				dropLockedInvItems(player);
		}
	}
	
	/**
	 * Should be called anytime that the number of inventory slots available to a player *might* have changed.
	 * This function should then move items between the ILockedInventory and the player inventory when relevant.
	 * Should be called when:
	 * - Number of unlocked slots change for a player
	 * - Slot locking starts/stops being applied to a player (gamemode changes)
	 * - Config value might've changed
	 *   - On config reload, for all online players
	 *   - On player login, to cover those that might've not been online on config reload
	 */
	public static void onLockChange(ServerPlayerEntity player)
	{
		onLockChange(player,  PlayerData.getAvailableSlots(player));
	}
	
	// Slots provided as an argument for situations where this is called during an event before the player state has properly updated
	public static void onLockChange(ServerPlayerEntity player, int slots)
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
	
	private static int getPrevAvailableSlots(ServerPlayerEntity player)
	{
		CompoundNBT nbt = PlayerData.getPersistentTag(player);
		return nbt.contains("slot_cache", Constants.NBT.TAG_ANY_NUMERIC)
				? MathHelper.clamp(nbt.getInt("slot_cache"), 0, 36)
				: 36;
	}
	
	/**
	 * Called when slots have been locked. Moves items in those slots to the ILockedInventory
	 */
	private static void onLockSlot(ServerPlayerEntity player, int slotMin, int slotMax)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv -> {
			boolean changed = false;
			for(int slot = slotMin; slot <= slotMax; slot++)
			{
				if(lockedInv.getStack(slot).isEmpty())
				{
					ItemStack stack = player.inventory.removeItemNoUpdate(slot);
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
	private static void onUnlockSlot(ServerPlayerEntity player, int slotMin, int slotMax)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv -> {
			boolean changed = false;
			for(int slot = slotMin; slot <= slotMax; slot++)
			{
				ItemStack stack = lockedInv.takeStack(slot);
				if(!stack.isEmpty())
				{
					if(player.inventory.getItem(slot).isEmpty())
						player.inventory.setItem(slot, stack);
					else player.drop(stack, true, false);
					changed = true;
				}
			}
			if(changed)
				sendLockedInv(player);
		});
	}
	
	private static void dropLockedInvItems(ServerPlayerEntity player)
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
	
	public static void sendLockedInv(ServerPlayerEntity player)
	{
		player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv ->
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), LockedInvSyncPacket.makePacket(lockedInv)));
	}
}