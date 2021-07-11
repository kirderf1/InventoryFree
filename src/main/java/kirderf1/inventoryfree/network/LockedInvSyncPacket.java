package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.capability.ILockedInventory;
import kirderf1.inventoryfree.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Packet for syncing data of a {@link ILockedInventory} capability to client-side
 */
public class LockedInvSyncPacket implements Packet.ToClient
{
	private static final Logger LOGGER = LogManager.getLogger();
	private final INBT nbt;
	
	public static LockedInvSyncPacket makePacket(ILockedInventory lockedInv)
	{
		return new LockedInvSyncPacket(ModCapabilities.LOCKED_INV_CAPABILITY.writeNBT(lockedInv, null));
	}
	
	private LockedInvSyncPacket(INBT nbt)
	{
		this.nbt = nbt;
	}
	
	public static LockedInvSyncPacket decode(PacketBuffer buffer)
	{
		CompoundNBT compound = buffer.readCompoundTag();
		return new LockedInvSyncPacket(compound != null ? compound.get("nbt") : null);
	}
	
	@Override
	public void encode(PacketBuffer buffer)
	{
		CompoundNBT compound = new CompoundNBT();
		compound.put("nbt", nbt);
		buffer.writeCompoundTag(compound);
	}
	
	@Override
	public void execute()
	{
		if(nbt != null)
		{
			PlayerEntity player = Minecraft.getInstance().player;
			if(player != null)
				player.getCapability(ModCapabilities.LOCKED_INV_CAPABILITY).ifPresent(lockedInv ->
						ModCapabilities.LOCKED_INV_CAPABILITY.readNBT(lockedInv, null, nbt));
		} else LOGGER.warn("InventoryFree got sync packet with invalid data");
	}
}