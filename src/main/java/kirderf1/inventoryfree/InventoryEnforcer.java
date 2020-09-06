package kirderf1.inventoryfree;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class InventoryEnforcer
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.START)
			return;
		
		if(shouldApply(event.player))
		{
			PlayerInventory inventory = event.player.inventory;
			int counter = 0;
			for(int index = 0; index < inventory.getSizeInventory(); index++)
			{
				if(enforceSlot(index, inventory))
					counter++;
			}
			if(counter > 0)
				LOGGER.info("Player \"{}\" had {} item stacks in invalid places this tick.", event.player.getDisplayName().getFormattedText(), counter);
		}
	}
	
	@SubscribeEvent
	public static void onItemPickup(EntityItemPickupEvent event)
	{
		if(shouldApply(event.getPlayer()))
		{
			if(!canStoreItem(event.getPlayer().inventory, event.getItem().getItem()))
				event.setCanceled(true);
		}
	}
	
	public static boolean isSlotToBeBlocked(int index)
	{
		return index >= InventoryFree.CONFIG.availableSlots.get() && index < 36;
	}
	
	private static boolean shouldApply(PlayerEntity player)
	{
		return player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).interactionManager.survivalOrAdventure();
	}
	
	private static boolean enforceSlot(int index, PlayerInventory inventory)
	{
		ItemStack stack = inventory.getStackInSlot(index);
		if(isSlotToBeBlocked(index) && !stack.isEmpty())
		{
			int freeIndex = findAvailableSlot(inventory);
			inventory.removeStackFromSlot(index);
			if(freeIndex < 0)
				inventory.player.dropItem(stack, true, false);
			else
				inventory.setInventorySlotContents(freeIndex, stack);
			return true;
		}
		return false;
	}
	
	private static boolean canStoreItem(PlayerInventory inventory, ItemStack stack)
	{
		if(findAvailableSlot(inventory) != -1)
			return true;
		if(stack.isDamaged())
			return false;
		int index = inventory.storeItemStack(stack);
		return index >= 0 && !isSlotToBeBlocked(index);
	}
	
	private static int findAvailableSlot(PlayerInventory inventory)
	{
		for(int index = 0; index < inventory.mainInventory.size(); index++)
		{
			if(!isSlotToBeBlocked(index) && inventory.getStackInSlot(index).isEmpty())
				return index;
		}
		return -1;
	}
}