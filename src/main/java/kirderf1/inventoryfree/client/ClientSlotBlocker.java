package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.slot_blocking.SlotBlocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Same as {@link SlotBlocker}, but for client-side events.
 * Is also responsible for add the {@link LockOverlay} to new container screens.
 */
@EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public final class ClientSlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger(InventoryFree.MOD_ID);
	
	@SubscribeEvent
	private static void onLogin(ClientPlayerNetworkEvent.LoggingIn event)
	{
		LocalPlayer player = Objects.requireNonNull(event.getPlayer());
		SlotBlocker.insertBlockedSlots(player.inventoryMenu, player, ClientData::getAvailableSlots);
	}
	
	@SubscribeEvent
	private static void onRespawn(ClientPlayerNetworkEvent.Clone event)
	{
		LocalPlayer player = Objects.requireNonNull(event.getPlayer());
		SlotBlocker.insertBlockedSlots(player.inventoryMenu, player, ClientData::getAvailableSlots);
	}
	
	@SubscribeEvent
	private static void onGuiOpened(ScreenEvent.Opening event)
	{
		if(event.getScreen() instanceof AbstractContainerScreen<?> screen)
		{
			LOGGER.debug("Container screen being opened. Inserting custom inventory slots...");
			SlotBlocker.insertBlockedSlots(screen.getMenu(), Minecraft.getInstance().player, ClientData::getAvailableSlots);
		}
	}
	
	@SubscribeEvent
	private static void onGuiInitialized(ScreenEvent.Init.Post event)
	{
		if(event.getScreen() instanceof AbstractContainerScreen<?> screen)
		{
			event.addListener(new LockOverlay(screen));
		}
	}
}
