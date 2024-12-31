package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.client.ClientData;
import kirderf1.inventoryfree.locked_inventory.LockedInventory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Packet for syncing data of a {@link LockedInventory} to client-side.
 */
@MethodsReturnNonnullByDefault
public record LockedInvSyncPayload(ListTag nbt) implements ModPayloads.ToClientPayload
{
	public static final Type<LockedInvSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(InventoryFree.MOD_ID, "locked_inv_sync"));
	
	public static final StreamCodec<FriendlyByteBuf, LockedInvSyncPayload> STREAM_CODEC = StreamCodec.ofMember(LockedInvSyncPayload::write, LockedInvSyncPayload::read);
	
	private static final Logger LOGGER = LogManager.getLogger(InventoryFree.MOD_ID);
	
	@Override
	public Type<LockedInvSyncPayload> type()
	{
		return TYPE;
	}
	
	private static LockedInvSyncPayload read(FriendlyByteBuf buffer)
	{
		CompoundTag compound = buffer.readNbt();
		return new LockedInvSyncPayload(compound != null ? compound.getList("nbt", Tag.TAG_COMPOUND) : null);
	}
	
	private void write(FriendlyByteBuf buffer)
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
		} else LOGGER.warn("Received sync packet with invalid data");
	}
}
