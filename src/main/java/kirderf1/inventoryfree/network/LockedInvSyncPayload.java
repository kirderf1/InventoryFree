package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.locked_inventory.LockedInventory;
import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Packet for syncing data of a {@link LockedInventory} to client-side.
 */
@MethodsReturnNonnullByDefault
public record LockedInvSyncPayload(ListTag nbt) implements ModPayloads.ToClientPayload
{
	public static final ResourceLocation ID = new ResourceLocation(InventoryFree.MOD_ID, "locked_inv_sync");
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static LockedInvSyncPayload makePacket(LockedInventory lockedInv)
	{
		return new LockedInvSyncPayload(lockedInv.serializeNBT());
	}
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	public static LockedInvSyncPayload read(FriendlyByteBuf buffer)
	{
		CompoundTag compound = buffer.readNbt();
		return new LockedInvSyncPayload(compound != null ? compound.getList("nbt", Tag.TAG_COMPOUND) : null);
	}
	
	@Override
	public void write(FriendlyByteBuf buffer)
	{
		CompoundTag compound = new CompoundTag();
		compound.put("nbt", nbt);
		buffer.writeNbt(compound);
	}
	
	@Override
	public void execute()
	{
		if(nbt != null)
		{
			ClientData.handle(this);
		} else LOGGER.warn("InventoryFree got sync packet with invalid data");
	}
	
}