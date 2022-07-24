package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.SlotBlocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Same as {@link SlotBlocker}, but for client-side events.
 * Is also responsible for add the {@link LockOverlay} to new container screens.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class ClientSlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event)
	{
		LocalPlayer player = Objects.requireNonNull(event.getPlayer());
		SlotBlocker.insertBlockedSlots(player.inventoryMenu, player, ClientData::getAvailableSlots);
	}
	
	@SubscribeEvent
	public static void onRespawn(ClientPlayerNetworkEvent.Clone event)
	{
		LocalPlayer player = Objects.requireNonNull(event.getPlayer());
		SlotBlocker.insertBlockedSlots(player.inventoryMenu, player, ClientData::getAvailableSlots);
	}
	
	@SubscribeEvent
	public static void onGuiOpened(ScreenEvent.Opening event)
	{
		if(event.getScreen() instanceof AbstractContainerScreen<?> screen)
		{
			LOGGER.debug("Container screen being opened. Inserting custom inventory slots...");
			SlotBlocker.insertBlockedSlots(screen.getMenu(), Minecraft.getInstance().player, ClientData::getAvailableSlots);
		}
	}
	
	@SubscribeEvent
	public static void onGuiInitialized(ScreenEvent.Init.Post event)
	{
		if(event.getScreen() instanceof AbstractContainerScreen<?> screen)
		{
			event.addListener(new LockOverlay(screen));
		}
	}
}