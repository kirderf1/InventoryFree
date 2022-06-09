package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.SlotUnlocker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Adds tooltip text to items which can unlock slots.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class UnlockTooltipHandler
{
	@SubscribeEvent
	public static void onTooltipEvent(ItemTooltipEvent event)
	{
		if(SlotUnlocker.shouldUnlockWith(event.getItemStack(), ClientData.getUnlockedSlots()))
		{
			int requiredCount = SlotUnlocker.getRequiredItemCount(ClientData.getUnlockedSlots());
			if(requiredCount != -1)
			{
				event.getToolTip().add(Component.translatable("inventory_free.unlock.tooltip").withStyle(ChatFormatting.AQUA));
				event.getToolTip().add(Component.translatable("inventory_free.unlock.tooltip2", requiredCount).withStyle(ChatFormatting.AQUA));
			}
		}
	}
}