package kirderf1.inventoryfree;

import kirderf1.inventoryfree.network.PacketHandler;
import kirderf1.inventoryfree.network.UnlockedSlotsPacket;
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
		int unlockedSlots = getUnlockedSlots(player);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UnlockedSlotsPacket(unlockedSlots));
	}
	
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event)
	{
		if(event.isWasDeath() && InventoryFree.CONFIG.clearUnlockedOnDeath.get()
				&& getUnlockedSlots((ServerPlayerEntity) event.getPlayer()) != 0)
			setUnlockedSlots((ServerPlayerEntity) event.getPlayer(), 0);
	}
	
	public static int getAvailableSlots(ServerPlayerEntity player)
	{
		return InventoryFree.getAvailableSlots(getUnlockedSlots(player));
	}
	
	public static int getUnlockedSlots(ServerPlayerEntity player)
	{
		return getPersistentTag(player).getInt("unlocked_slots");
	}
	
	public static void unlockSlots(ServerPlayerEntity player, int amount)
	{
		CompoundNBT nbt = getOrCreatePersistentTag(player);
		nbt.putInt("unlocked_slots", nbt.getInt("unlocked_slots") + amount);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UnlockedSlotsPacket(nbt.getInt("unlocked_slots")));
	}
	
	public static void setUnlockedSlots(ServerPlayerEntity player, int unlockedSlots)
	{
		getOrCreatePersistentTag(player).putInt("unlocked_slots", unlockedSlots);
		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new UnlockedSlotsPacket(unlockedSlots));
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