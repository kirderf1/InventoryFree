package kirderf1.inventoryfree;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

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
				: PlayerData.getUnlockedSlots((ServerPlayer) event.getPlayer());
		
		if(shouldUnlockWith(stack, unlockedSlots))
		{
			int requiredCount = getRequiredItemCount(unlockedSlots);
			if(requiredCount != -1 && stack.getCount() >= requiredCount)
			{
				event.setCanceled(true);
				event.setCancellationResult(InteractionResult.SUCCESS);
				stack.shrink(requiredCount);
				if(event.getSide() == LogicalSide.SERVER)
					PlayerData.unlockSlots((ServerPlayer) event.getPlayer(), 1);
			}
		}
	}
	
	/**
	 * Returns true if the item can be used to unlock slots, and there are still slots left to unlock.
	 */
	public static boolean shouldUnlockWith(ItemStack stack, int unlockedSlots)
	{
		return InventoryFree.getAvailableSlots(unlockedSlots) != InventoryFree.getAvailableSlots(unlockedSlots + 1)
				&& new ResourceLocation(InventoryFree.CONFIG.unlockSlotItem.get()).equals(stack.getItem().getRegistryName());
	}
	
	/**
	 * Returns the number of items needed to unlock the next slot.
	 */
	public static int getRequiredItemCount(int unlockedSlots)
	{
		unlockedSlots = Math.max(0, unlockedSlots);
		return switch(InventoryFree.CONFIG.costProgression.get())
				{
					case CONSTANT -> 1;
					case LINEAR -> 1 + unlockedSlots;
					case EXPONENTIAL -> unlockedSlots > 6 ? -1
							: (int) Math.pow(2, unlockedSlots);
				};
	}
	
	public enum CostProgression
	{
		CONSTANT,
		LINEAR,
		EXPONENTIAL,
	}
}