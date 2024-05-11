package kirderf1.inventoryfree.slot_blocking;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Supplier;

/**
 * The common class for inserting our custom slots into container menus.
 * Listens to some server-side events to handle new container menus.
 */
@EventBusSubscriber(modid = InventoryFree.MOD_ID)
public final class SlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	private static void onContainerOpened(PlayerContainerEvent.Open event)
	{
		LOGGER.debug("Container being opened. Inserting custom inventory slots...");
		ServerPlayer player = (ServerPlayer) event.getEntity();
		insertBlockedSlots(event.getContainer(), player, () -> PlayerData.getAvailableSlots(player));
	}
	
	@SubscribeEvent
	private static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		LOGGER.debug("Player logged in. Inserting custom inventory slots into the inventory container...");
		ServerPlayer player = (ServerPlayer) event.getEntity();
		insertBlockedSlots(player.inventoryMenu, player, () -> PlayerData.getAvailableSlots(player));
	}
	
	@SubscribeEvent
	private static void onPlayerClone(PlayerEvent.Clone event)
	{
		LOGGER.debug("Player entity being cloned. Inserting custom inventory slots into the inventory container...");
		ServerPlayer player = (ServerPlayer) event.getEntity();
		insertBlockedSlots(player.inventoryMenu, player, () -> PlayerData.getAvailableSlots(player));
	}
	
	/**
	 * Goes through the menu slots and replaces certain slots with a {@link BlockedSlot}.
	 * This function can be used on both logical sides by providing the correct "available slots"-getter.
	 */
	public static void insertBlockedSlots(AbstractContainerMenu menu, Player player, Supplier<Integer> availableSlots)
	{
		List<Slot> slots = menu.slots;
		for(int index = 0; index < slots.size(); index++)
		{
			Slot slot = slots.get(index);
			if(!(slot instanceof BlockedSlot) && shouldReplaceSlot(slot, player.getInventory()))
			{
				final int finalIndex = slot.getSlotIndex();
				Slot newSlot = new BlockedSlot(slot, () -> InventoryFree.appliesTo(player)
						&& InventoryFree.isSlotToBeBlocked(finalIndex, availableSlots.get()));
				slots.set(index, newSlot);
			}
		}
	}
	
	/**
	 * Determines if the given slot should be replaced with a {@link BlockedSlot}.
	 */
	private static boolean shouldReplaceSlot(Slot slot, Inventory playerInv)
	{
		return slot.container == playerInv && slot.getSlotIndex() < 36;
	}
}
