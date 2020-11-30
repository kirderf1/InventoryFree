package kirderf1.inventoryfree.client;

import com.mojang.blaze3d.systems.RenderSystem;
import kirderf1.inventoryfree.BlockedSlot;
import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A widget which will draw an icon on top of all slots which are blocked.
 * It is also responsible for drawing the icon on the overlay hotbar when appropriate.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class LockOverlay extends Widget
{
	private static final ResourceLocation LOCK = new ResourceLocation(InventoryFree.MOD_ID, "textures/item/lock.png");
	
	private final ContainerScreen<?> screen;
	
	public LockOverlay(ContainerScreen<?> screen)
	{
		super(0, 0, "");
		this.screen = screen;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(!InventoryFree.appliesTo(mc.player))
			return;
		
		mc.getTextureManager().bindTexture(LOCK);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		
		for(Slot slot : screen.getContainer().inventorySlots)
		{
			if(slot instanceof BlockedSlot && !slot.isEnabled())
			{
				blit(screen.getGuiLeft() + slot.xPos, screen.getGuiTop() + slot.yPos, getBlitOffset(), 0, 0, 16, 16, 16, 16);
			}
		}
	}
	
	@SubscribeEvent
	public static void onHotbarOverlay(RenderGameOverlayEvent.Post event)
	{
		if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
		{
			Minecraft mc = Minecraft.getInstance();
			if(!InventoryFree.appliesTo(mc.player) || ClientData.getAvailableSlots() >= 9)
				return;
			
			mc.getTextureManager().bindTexture(LOCK);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			int scaledWidth = mc.getMainWindow().getScaledWidth();
			int scaledHeight = mc.getMainWindow().getScaledHeight();
			
			for(Slot slot : mc.player.container.inventorySlots)
			{
				if(slot.getSlotIndex() < 9 && slot instanceof BlockedSlot && !slot.isEnabled())
				{
					int x = (scaledWidth/2 - 90) + (slot.getSlotIndex() * 20 + 2);
					int y = (scaledHeight - 16) - 3;
					blit(x, y, 0, 0, 16, 16, 16, 16);
				}
			}
		}
	}
}
