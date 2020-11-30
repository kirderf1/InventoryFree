package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.SlotBlocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Same as {@link SlotBlocker}, but for client-side events.
 * Is also responsible for applying the {@link LockOverlay}.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class ClientSlotBlocker
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onGuiOpened(GuiOpenEvent event)
	{
		if(event.getGui() instanceof ContainerScreen<?>)
		{
			LOGGER.debug("Container screen being opened. Inserting custom inventory slots...");
			ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
			SlotBlocker.insertBlockedSlots(screen.getContainer(), Minecraft.getInstance().player, ClientData::getAvailableSlots);
		}
	}
	
	@SubscribeEvent
	public static void onGuiInitialized(GuiScreenEvent.InitGuiEvent event)
	{
		if(event.getGui() instanceof ContainerScreen<?>)
		{
			event.addWidget(new LockOverlay((ContainerScreen<?>) event.getGui()));
		}
	}
}
