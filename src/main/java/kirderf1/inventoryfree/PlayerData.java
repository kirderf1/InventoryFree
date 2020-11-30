package kirderf1.inventoryfree;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

public class PlayerData
{
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