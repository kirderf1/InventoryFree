package kirderf1.inventoryfree;

import kirderf1.inventoryfree.network.AvailableSlotsPacket;
import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber
public class PlayerData
{
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		int availableSlots = getAvailableSlots(player);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new AvailableSlotsPacket(availableSlots));
	}
	
	public static int getAvailableSlots(ServerPlayerEntity player)
	{
		return InventoryFree.CONFIG.availableSlots.get();
	}
	
	public static CompoundNBT getPersistentTag(ServerPlayerEntity player)
	{
		return player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG).getCompound(InventoryFree.MOD_ID);
	}
	public static CompoundNBT getOrCreatePersistentTag(ServerPlayerEntity player)
	{
		return getOrCreate(getOrCreate(player.getPersistentData(), PlayerEntity.PERSISTED_NBT_TAG), InventoryFree.MOD_ID);
	}
	private static CompoundNBT getOrCreate(CompoundNBT nbt, String str)
	{
		if(!nbt.contains(str, Constants.NBT.TAG_COMPOUND))
			nbt.put(str, new CompoundNBT());
		return nbt.getCompound(str);
	}
}