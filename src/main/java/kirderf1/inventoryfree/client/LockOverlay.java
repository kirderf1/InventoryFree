package kirderf1.inventoryfree.client;

import com.mojang.blaze3d.systems.RenderSystem;
import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.locked_inventory.LockedInventory;
import kirderf1.inventoryfree.slot_blocking.BlockedSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * A widget which will draw an icon on top of all slots which are blocked.
 * It is also responsible for drawing the icon on the overlay hotbar when appropriate.
 * Any items in the {@link LockedInventory} capability will be drawn underneath the icon.
 */
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public final class LockOverlay extends AbstractWidget
{
	private static final ResourceLocation LOCK = new ResourceLocation(InventoryFree.MOD_ID, "textures/item/lock.png");
	
	// Must be large enough that the lock texture is drawn on top of slot items and the gui background,
	// but small enough that it is drawn behind floating/dragged items and item tooltips.
	private static final int LOCK_BLIT = 220;
	
	private final AbstractContainerScreen<?> screen;
	
	public LockOverlay(AbstractContainerScreen<?> screen)
	{
		super(0, 0, 0, 0, CommonComponents.EMPTY);
		this.screen = screen;
	}
	
	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(!InventoryFree.appliesTo(mc.player))
			return;
		
		RenderSystem.enableDepthTest();
		getLockedInv(mc).ifPresent(lockedInv -> {
			// Draw items in locked slots from the locked inventory
			for(Slot slot : screen.getMenu().slots)
			{
				if(slot instanceof BlockedSlot && !slot.isActive())
				{
					drawItem(graphics, lockedInv.getStack(slot.getSlotIndex()), mc,
							screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y);
				}
			}
		});
		
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		
		// Draw lock textures on locked slots
		for(Slot slot : screen.getMenu().slots)
		{
			if(slot instanceof BlockedSlot && !slot.isActive())
			{
				graphics.blit(LOCK, screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, LOCK_BLIT, 0, 0, 16, 16, 16, 16);
			}
		}
	}
	
	@SubscribeEvent
	private static void onHotbarOverlay(RenderGuiLayerEvent.Post event)
	{
		if(event.getName().equals(VanillaGuiLayers.HOTBAR))
		{
			Minecraft mc = Minecraft.getInstance();
			GuiGraphics graphics = event.getGuiGraphics();
			if(!InventoryFree.appliesTo(mc.player) || ClientData.getAvailableSlots() >= 9)
				return;
			
			
			int scaledWidth = mc.getWindow().getGuiScaledWidth();
			int scaledHeight = mc.getWindow().getGuiScaledHeight();
			
			RenderSystem.enableDepthTest();
			getLockedInv(mc).ifPresent(lockedInv -> {
				// Draw items in locked slots from the locked inventory
				for(Slot slot : mc.player.inventoryMenu.slots)
				{
					if(slot.getSlotIndex() < 9 && slot instanceof BlockedSlot && !slot.isActive())
					{
						int x = (scaledWidth/2 - 90) + (slot.getSlotIndex() * 20 + 2);
						int y = (scaledHeight - 16) - 3;
						drawItem(graphics, lockedInv.getStack(slot.getSlotIndex()), mc, x, y);
					}
				}
			});
			
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			for(Slot slot : mc.player.inventoryMenu.slots)
			{
				if(slot.getSlotIndex() < 9 && slot instanceof BlockedSlot && !slot.isActive())
				{
					int x = (scaledWidth/2 - 90) + (slot.getSlotIndex() * 20 + 2);
					int y = (scaledHeight - 16) - 3;
					graphics.blit(LOCK, x, y, LOCK_BLIT, 0, 0, 16, 16, 16, 16);
				}
			}
		}
	}
	
	private static void drawItem(GuiGraphics graphics, ItemStack stack, Minecraft mc, int x, int y)
	{
		if(!stack.isEmpty())
		{
			graphics.renderItem(stack, x, y);
			graphics.renderItemDecorations(mc.font, stack, x, y);
		}
	}
	
	private static Optional<LockedInventory> getLockedInv(Minecraft mc)
	{
		return Optional.ofNullable(mc.player).map(player -> player.getData(InventoryFree.LOCKED_INVENTORY));
	}
	
	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{
	
	}
}
