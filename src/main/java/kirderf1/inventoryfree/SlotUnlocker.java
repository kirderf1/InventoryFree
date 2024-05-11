package kirderf1.inventoryfree;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

/**
 * Responsible for unlocking slots when using a configured item.
 */
@EventBusSubscriber(modid = InventoryFree.MOD_ID)
public final class SlotUnlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	private static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		if(tryUseUnlockItem(event.getItemStack(), event.getEntity()))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
	
	@SubscribeEvent
	private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		if(tryUseUnlockItem(event.getItemStack(), event.getEntity()))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
	
	@SubscribeEvent
	private static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		if(tryUseUnlockItem(event.getItemStack(), event.getEntity()))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
	
	@SubscribeEvent
	private static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event)
	{
		if(tryUseUnlockItem(event.getItemStack(), event.getEntity()))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
	
	/**
	 * Handles item right click on both logical sides to potentially consume the item and unlock a slot.
	 */
	private static boolean tryUseUnlockItem(ItemStack stack, Player player)
	{
		int unlockedSlots = player instanceof ServerPlayer serverPlayer
				? PlayerData.getUnlockedSlots(serverPlayer)
				: ClientData.getUnlockedSlots();
		
		if(!shouldUnlockWith(stack, unlockedSlots))
			return false;
		
		int requiredCount = getRequiredItemCount(unlockedSlots, stack.getMaxStackSize());
		if(requiredCount == -1 || stack.getCount() < requiredCount)
			return false;
		
		stack.shrink(requiredCount);
		if(player instanceof ServerPlayer serverPlayer)
			PlayerData.unlockSlots(serverPlayer, 1);
		return true;
	}
	
	/**
	 * Returns true if the item can be used to unlock slots, and there are still slots left to unlock.
	 */
	public static boolean shouldUnlockWith(ItemStack stack, int unlockedSlots)
	{
		return InventoryFree.getAvailableSlots(unlockedSlots) != InventoryFree.getAvailableSlots(unlockedSlots + 1)
				&& InventoryFree.CONFIG.unlockSlotItem.get().equals(String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem())));
	}
	
	/**
	 * Returns the number of items needed to unlock the next slot.
	 * When a cost would have been above the max stack size of the item,
	 * will return -1 instead to indicate that no more slots can be unlocked.
	 * (This is done because only one stack will be used when unlocking new slots)
	 */
	public static int getRequiredItemCount(int unlockedSlots, int max)
	{
		unlockedSlots = Math.max(0, unlockedSlots);
		int baseCost = InventoryFree.CONFIG.costProgression.get().costGetter.applyAsInt(unlockedSlots);
		if(baseCost == -1)
			return -1;
		
		int cost = baseCost * InventoryFree.CONFIG.costMultiplier.get();
		return cost <= max ? cost : -1;
	}
	
	@SuppressWarnings("unused")
	public enum CostProgression
	{
		CONSTANT(unlockedSlots -> 1),
		LINEAR(unlockedSlots -> 1 + unlockedSlots),
		EXPONENTIAL(unlockedSlots -> unlockedSlots > 6 ? -1 : (int) Math.pow(2, unlockedSlots)),
		;
		
		private final IntUnaryOperator costGetter;
		
		CostProgression(IntUnaryOperator costGetter)
		{
			this.costGetter = costGetter;
		}
	}
	
	public static void verifyUnlockItem(ModConfigEvent event)
	{
		String itemIdStr = InventoryFree.CONFIG.unlockSlotItem.get();
		if(itemIdStr.isEmpty())
			return;	//If the string is empty, we can assume that it is intentionally not a valid item
		
		ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);
		if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId))
		{
			LOGGER.error("Not a valid id for the unlock item: {}", itemIdStr);
			return;
		}
		
		Item item = Objects.requireNonNull(BuiltInRegistries.ITEM.get(itemId));
		
		if(getRequiredItemCount(0, item.getDefaultInstance().getMaxStackSize()) == -1)
			LOGGER.warn("Unlock item max stack size is lower than the cost to unlock the first slot. It will not be possible to unlock any slots with the item under these circumstances!");
	}
}
