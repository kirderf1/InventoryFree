package kirderf1.inventoryfree;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Supplier;

/**
 * The common class for inserting our custom slots into containers.
 * Listens to some server-side events to handle new containers.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class SlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onContainerOpened(PlayerContainerEvent.Open event)
	{
		LOGGER.debug("Container being opened. Inserting custom inventory slots...");
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		insertBlockedSlots(event.getContainer(), player, () -> PlayerData.getAvailableSlots(player));
	}
	
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		LOGGER.debug("Player logged in. Inserting custom inventory slots into the inventory container...");
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		insertBlockedSlots(player.inventoryMenu, player, () -> PlayerData.getAvailableSlots(player));
	}
	
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		LOGGER.debug("Player entity being cloned. Inserting custom inventory slots into the inventory container...");
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		insertBlockedSlots(player.inventoryMenu, player, () -> PlayerData.getAvailableSlots(player));
	}
	
	public static void insertBlockedSlots(AbstractContainerMenu container, Player player, Supplier<Integer> availableSlots)
	{
		List<Slot> slots = container.slots;
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
	
	private static boolean shouldReplaceSlot(Slot slot, Inventory playerInv)
	{
		return slot.container == playerInv && slot.getSlotIndex() < 36;
	}
}
