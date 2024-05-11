package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.SlotUnlocker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Adds tooltip text to items which can unlock slots.
 */
@EventBusSubscriber(modid = InventoryFree.MOD_ID, value = Dist.CLIENT)
public final class UnlockTooltipHandler
{
	@SubscribeEvent
	private static void onTooltipEvent(ItemTooltipEvent event)
	{
		if(SlotUnlocker.shouldUnlockWith(event.getItemStack(), ClientData.getUnlockedSlots()))
		{
			int requiredCount = SlotUnlocker.getRequiredItemCount(ClientData.getUnlockedSlots(), event.getItemStack().getMaxStackSize());
			if(requiredCount != -1)
			{
				event.getToolTip().add(Component.translatable("inventory_free.unlock.tooltip").withStyle(ChatFormatting.AQUA));
				event.getToolTip().add(Component.translatable("inventory_free.unlock.tooltip2", requiredCount).withStyle(ChatFormatting.AQUA));
			}
		}
	}
}
