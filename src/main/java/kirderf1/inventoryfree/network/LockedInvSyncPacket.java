package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.capability.ILockedInventory;
import kirderf1.inventoryfree.client.ClientCapabilityHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Packet for syncing data of a {@link ILockedInventory} capability to client-side
 */
public class LockedInvSyncPacket implements Packet.ToClient
{
	private static final Logger LOGGER = LogManager.getLogger();
	private final ListTag nbt;
	
	public static LockedInvSyncPacket makePacket(ILockedInventory lockedInv)
	{
		return new LockedInvSyncPacket(lockedInv.serializeNBT());
	}
	
	private LockedInvSyncPacket(ListTag nbt)
	{
		this.nbt = nbt;
	}
	
	public static LockedInvSyncPacket decode(FriendlyByteBuf buffer)
	{
		CompoundTag compound = buffer.readNbt();
		return new LockedInvSyncPacket(compound != null ? compound.getList("nbt", Tag.TAG_COMPOUND) : null);
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
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
			ClientCapabilityHandler.handleLockedInvPacket(this);
		} else LOGGER.warn("InventoryFree got sync packet with invalid data");
	}
	
	public ListTag getNbt()
	{
		return nbt;
	}
}