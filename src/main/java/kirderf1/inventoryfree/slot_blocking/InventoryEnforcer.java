package kirderf1.inventoryfree.slot_blocking;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Is responsible on the logical server-side for
 * enforcing that blocked inventory slots are empty,
 * but also for stopping item pickups when appropriate.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class InventoryEnforcer
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	private static void onTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.START)
			return;
		
		if(InventoryFree.appliesTo(event.player))
		{
			Inventory inventory = event.player.getInventory();
			int availableSlots = PlayerData.getAvailableSlots((ServerPlayer) event.player);
			
			int counter = 0;
			for(int index = 0; index < inventory.getContainerSize(); index++)
			{
				if(enforceSlot(index, inventory, availableSlots))
					counter++;
			}
			if(counter > 0)
				LOGGER.info("Player \"{}\" had {} item stacks in invalid places this tick.",
						event.player.getDisplayName().getString(), counter);
		}
	}
	
	@SubscribeEvent
	private static void onItemPickup(EntityItemPickupEvent event)
	{
		if(InventoryFree.appliesTo(event.getEntity()))
		{
			if(!canStoreItem(event.getEntity().getInventory(), event.getItem().getItem(),
					PlayerData.getAvailableSlots((ServerPlayer) event.getEntity())))
				event.setCanceled(true);
		}
	}
	
	/**
	 * Checks if the slot is blocked and has an item,
	 * and if so tries to move it to an empty unblocked slot.
	 * If it fails to do so, the player will drop the item instead.
	 */
	private static boolean enforceSlot(int index, Inventory inventory, int availableSlots)
	{
		ItemStack stack = inventory.getItem(index);
		if(InventoryFree.isSlotToBeBlocked(index, availableSlots) && !stack.isEmpty())
		{
			int freeIndex = findAvailableSlot(inventory, availableSlots);
			inventory.removeItemNoUpdate(index);
			if(freeIndex < 0)
				inventory.player.drop(stack, true, false);
			else
				inventory.setItem(freeIndex, stack);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if an item can be picked up given the number of available slots.
	 */
	private static boolean canStoreItem(Inventory inventory, ItemStack stack, int availableSlots)
	{
		if(findAvailableSlot(inventory, availableSlots) != -1)
			return true;
		if(stack.isDamaged())
			return false;
		int index = inventory.getSlotWithRemainingSpace(stack);
		return index >= 0 && !InventoryFree.isSlotToBeBlocked(index, availableSlots);
	}
	
	/**
	 * Searches the player inventory for the first empty slot that is not blocked,
	 * and returns its index, or -1 if no such slot was found.
	 */
	private static int findAvailableSlot(Inventory inventory, int availableSlots)
	{
		for(int index = 0; index < inventory.items.size(); index++)
		{
			if(!InventoryFree.isSlotToBeBlocked(index, availableSlots) && inventory.getItem(index).isEmpty())
				return index;
		}
		return -1;
	}
}