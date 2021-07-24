package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.SlotBlocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Same as {@link SlotBlocker}, but for client-side events.
 * Is also responsible for applying the {@link LockOverlay}.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class ClientSlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onLogin(ClientPlayerNetworkEvent.LoggedInEvent event)
	{
		LocalPlayer player = Objects.requireNonNull(event.getPlayer());
		SlotBlocker.insertBlockedSlots(player.inventoryMenu, player, ClientData::getAvailableSlots);
	}
	
	@SubscribeEvent
	public static void onRespawn(ClientPlayerNetworkEvent.RespawnEvent event)
	{
		LocalPlayer player = Objects.requireNonNull(event.getPlayer());
		SlotBlocker.insertBlockedSlots(player.inventoryMenu, player, ClientData::getAvailableSlots);
	}
	
	@SubscribeEvent
	public static void onGuiOpened(GuiOpenEvent event)
	{
		if(event.getGui() instanceof AbstractContainerScreen<?> screen)
		{
			LOGGER.debug("Container screen being opened. Inserting custom inventory slots...");
			SlotBlocker.insertBlockedSlots(screen.getMenu(), Minecraft.getInstance().player, ClientData::getAvailableSlots);
		}
	}
	
	@SubscribeEvent
	public static void onGuiInitialized(GuiScreenEvent.InitGuiEvent event)
	{
		if(event.getGui() instanceof AbstractContainerScreen<?> screen)
		{
			event.addWidget(new LockOverlay(screen));
		}
	}
}