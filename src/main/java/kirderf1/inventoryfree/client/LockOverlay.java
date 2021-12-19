package kirderf1.inventoryfree.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import kirderf1.inventoryfree.BlockedSlot;
import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.capability.ILockedInventory;
import kirderf1.inventoryfree.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A widget which will draw an icon on top of all slots which are blocked.
 * It is also responsible for drawing the icon on the overlay hotbar when appropriate.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public class LockOverlay extends AbstractWidget
{
	private static final ResourceLocation LOCK = new ResourceLocation(InventoryFree.MOD_ID, "textures/item/lock.png");
	
	private static final int LOCK_BLIT = 200;
	
	private final AbstractContainerScreen<?> screen;
	
	public LockOverlay(AbstractContainerScreen<?> screen)
	{
		super(0, 0, 0, 0, TextComponent.EMPTY);
		this.screen = screen;
	}
	
	@Override
	public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
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
					drawItem(lockedInv.getStack(slot.getSlotIndex()), mc,
							screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y);
				}
			}
		});
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, LOCK);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		
		// Draw lock textures on locked slots
		for(Slot slot : screen.getMenu().slots)
		{
			if(slot instanceof BlockedSlot && !slot.isActive())
			{
				blit(matrixStack, screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, getBlitOffset() + LOCK_BLIT, 0, 0, 16, 16, 16, 16);
			}
		}
	}
	
	@SubscribeEvent
	public static void onHotbarOverlay(RenderGameOverlayEvent.PostLayer event)
	{
		if(event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT)
		{
			Minecraft mc = Minecraft.getInstance();
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
						drawItem(lockedInv.getStack(slot.getSlotIndex()), mc, x, y);
					}
				}
			});
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, LOCK);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			
			for(Slot slot : mc.player.inventoryMenu.slots)
			{
				if(slot.getSlotIndex() < 9 && slot instanceof BlockedSlot && !slot.isActive())
				{
					int x = (scaledWidth/2 - 90) + (slot.getSlotIndex() * 20 + 2);
					int y = (scaledHeight - 16) - 3;
					blit(event.getMatrixStack(), x, y, LOCK_BLIT, 0, 0, 16, 16, 16, 16);
				}
			}
		}
	}
	
	private static void drawItem(ItemStack stack, Minecraft mc, int x, int y)
	{
		if(!stack.isEmpty())
		{
			mc.getItemRenderer().renderAndDecorateItem(stack, x, y);
			mc.getItemRenderer().renderGuiItemDecorations(mc.font, stack, x, y, null);
		}
	}
	
	private static LazyOptional<ILockedInventory> getLockedInv(Minecraft mc)
	{
		if(mc.player != null)
		{
			return mc.player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY);
		} else
			return LazyOptional.empty();
	}
	
	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput)
	{
	
	}
}