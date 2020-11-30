package kirderf1.inventoryfree;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

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
	
	private static void onItemUsed(PlayerInteractEvent event)
	{
		ItemStack stack = event.getItemStack();
		int unlockedSlots = event.getSide() == LogicalSide.CLIENT ? ClientData.getUnlockedSlots()
				: PlayerData.getUnlockedSlots((ServerPlayerEntity) event.getPlayer());
		
		if(shouldUnlockWith(stack, unlockedSlots))
		{
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
			stack.shrink(1);
			if(event.getSide() == LogicalSide.SERVER)
				PlayerData.unlockSlots((ServerPlayerEntity) event.getPlayer(), 1);
		}
	}
	
	public static boolean shouldUnlockWith(ItemStack stack, int unlockedSlots)
	{
		return InventoryFree.getAvailableSlots(unlockedSlots) != InventoryFree.getAvailableSlots(unlockedSlots + 1)
				&& new ResourceLocation(InventoryFree.CONFIG.unlockSlotItem.get()).equals(stack.getItem().getRegistryName());
	}
}