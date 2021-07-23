package kirderf1.inventoryfree.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import kirderf1.inventoryfree.BlockedSlot;
import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.capability.ILockedInventory;
import kirderf1.inventoryfree.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.LazyOptional;
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
	
	private static final int LOCK_BLIT = 200;
	
	private final ContainerScreen<?> screen;
	private static LazyOptional<ILockedInventory> cachedLockedInv = LazyOptional.empty();
	
	public LockOverlay(ContainerScreen<?> screen)
	{
		super(0, 0, 0, 0, StringTextComponent.EMPTY);
		this.screen = screen;
	}
	
	@Override
	public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(!InventoryFree.appliesTo(mc.player))
			return;
		
		RenderSystem.enableDepthTest();
		getCachedLockedInv(mc).ifPresent(lockedInv -> {
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
		
		
		mc.getTextureManager().bind(LOCK);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		
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
	public static void onHotbarOverlay(RenderGameOverlayEvent.Post event)
	{
		if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
		{
			Minecraft mc = Minecraft.getInstance();
			if(!InventoryFree.appliesTo(mc.player) || ClientData.getAvailableSlots() >= 9)
				return;
			
			
			int scaledWidth = mc.getWindow().getGuiScaledWidth();
			int scaledHeight = mc.getWindow().getGuiScaledHeight();
			
			RenderSystem.enableDepthTest();
			getCachedLockedInv(mc).ifPresent(lockedInv -> {
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
			
			mc.getTextureManager().bind(LOCK);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
			FontRenderer font = stack.getItem().getFontRenderer(stack);
			if(font == null)
				font = mc.font;
			mc.getItemRenderer().renderAndDecorateItem(stack, x, y);
			mc.getItemRenderer().renderGuiItemDecorations(font, stack, x, y, null);
		}
	}
	
	private static LazyOptional<ILockedInventory> getCachedLockedInv(Minecraft mc)
	{
		if(!cachedLockedInv.isPresent())
		{
			cachedLockedInv = mc.player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY);
			cachedLockedInv.addListener(self -> cachedLockedInv = LazyOptional.empty());
		}
		return cachedLockedInv;
	}
	
	// Caps are not naturally invalidated in certain circumstances, so we have to clear it manually
	// Normally this shouldn't be a problem, but because the cache is static, and not part of a world object, it doesn't get cleared with the world
	// If additional cap invalidation problems show up, it should be fine to throw out the cache altogether
	
	@SubscribeEvent
	public static void onClientRespawn(ClientPlayerNetworkEvent.RespawnEvent event)
	{
		cachedLockedInv = LazyOptional.empty();
	}
	
	@SubscribeEvent
	public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
	{
		cachedLockedInv = LazyOptional.empty();
	}
}