package kirderf1.inventoryfree;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.IntUnaryOperator;

/**
 * Responsible for unlocking slots when using a configured item.
 */
@Mod.EventBusSubscriber
public class SlotUnlocker
{
	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		onItemUsed(event);
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		onItemUsed(event);
	}
	
	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		onItemUsed(event);
	}
	
	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event)
	{
		onItemUsed(event);
	}
	
	/**
	 * Handles item right click on both logical sides to potentially consume the item and unlock a slot.
	 */
	private static void onItemUsed(PlayerInteractEvent event)
	{
		ItemStack stack = event.getItemStack();
		int unlockedSlots = event.getSide() == LogicalSide.CLIENT ? ClientData.getUnlockedSlots()
				: PlayerData.getUnlockedSlots((ServerPlayer) event.getEntity());
		
		if(shouldUnlockWith(stack, unlockedSlots))
		{
			int requiredCount = getRequiredItemCount(unlockedSlots, stack.getMaxStackSize());
			if(requiredCount != -1 && stack.getCount() >= requiredCount)
			{
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.SUCCESS);
				stack.shrink(requiredCount);
				if(event.getSide() == LogicalSide.SERVER)
					PlayerData.unlockSlots((ServerPlayer) event.getEntity(), 1);
			}
		}
	}
	
	/**
	 * Returns true if the item can be used to unlock slots, and there are still slots left to unlock.
	 */
	public static boolean shouldUnlockWith(ItemStack stack, int unlockedSlots)
	{
		return InventoryFree.getAvailableSlots(unlockedSlots) != InventoryFree.getAvailableSlots(unlockedSlots + 1)
				&& InventoryFree.CONFIG.unlockSlotItem.get().equals(String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem())));
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
}