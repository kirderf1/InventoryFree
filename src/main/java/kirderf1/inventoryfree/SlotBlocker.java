package kirderf1.inventoryfree;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class SlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onContainerOpened(PlayerContainerEvent.Open event)
	{
		LOGGER.debug("Container being opened. Inserting custom inventory slots...");
		insertBlockedSlots(event.getContainer(), event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		LOGGER.debug("Player logged in. Inserting custom inventory slots into the inventory container...");
		insertBlockedSlots(event.getPlayer().container, event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		LOGGER.debug("Player entity being cloned. Inserting custom inventory slots into the inventory container...");
		insertBlockedSlots(event.getPlayer().container, event.getPlayer());
	}
	
	public static void insertBlockedSlots(Container container, PlayerEntity player)
	{
		List<Slot> slots = container.inventorySlots;
		for(int index = 0; index < slots.size(); index++)
		{
			Slot slot = slots.get(index);
			if(!(slot instanceof BlockedSlot) && shouldReplaceSlot(slot, player.inventory))
			{
				Slot newSlot = new BlockedSlot(slot, () -> isDisabledFor(player));
				slots.set(index, newSlot);
			}
		}
	}
	
	public static boolean isDisabledFor(PlayerEntity player)
	{
		return player.abilities.isCreativeMode;
	}
	
	private static boolean shouldReplaceSlot(Slot slot, PlayerInventory playerInv)
	{
		return slot.inventory == playerInv && slot.getSlotIndex() < 36;
	}
}
