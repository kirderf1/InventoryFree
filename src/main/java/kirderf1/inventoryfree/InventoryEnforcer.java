package kirderf1.inventoryfree;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Is responsible for enforcing that blocked inventory slots are empty,
 * but also for stopping item pickups when appropriate.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class InventoryEnforcer
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event)
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
	public static void onItemPickup(EntityItemPickupEvent event)
	{
		if(InventoryFree.appliesTo(event.getPlayer()))
		{
			if(!canStoreItem(event.getPlayer().getInventory(), event.getItem().getItem(),
					PlayerData.getAvailableSlots((ServerPlayer) event.getPlayer())))
				event.setCanceled(true);
		}
	}
	
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
	
	private static boolean canStoreItem(Inventory inventory, ItemStack stack, int availableSlots)
	{
		if(findAvailableSlot(inventory, availableSlots) != -1)
			return true;
		if(stack.isDamaged())
			return false;
		int index = inventory.getSlotWithRemainingSpace(stack);
		return index >= 0 && !InventoryFree.isSlotToBeBlocked(index, availableSlots);
	}
	
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