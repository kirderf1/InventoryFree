package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.SlotUnlocker;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class UnlockTooltipHandler
{
	@SubscribeEvent
	public static void onTooltipEvent(ItemTooltipEvent event)
	{
		if(SlotUnlocker.shouldUnlockWith(event.getItemStack(), ClientData.getUnlockedSlots()))
			event.getToolTip().add(new TranslationTextComponent("inventory_free.unlock.tooltip").withStyle(TextFormatting.AQUA));
	}
}