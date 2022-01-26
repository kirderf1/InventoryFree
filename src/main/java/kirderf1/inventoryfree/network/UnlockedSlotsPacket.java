package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet for sending the number of unlocked slots to {@link ClientData}.
 */
public class UnlockedSlotsPacket implements Packet.ToClient
{
	private final int unlockedSlots;
	
	public UnlockedSlotsPacket(int unlockedSlots)
	{
		this.unlockedSlots = unlockedSlots;
	}
	
	public static UnlockedSlotsPacket decode(FriendlyByteBuf buffer)
	{
		return new UnlockedSlotsPacket(buffer.readInt());
	}
	
	@Override
	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeInt(unlockedSlots);
	}
	
	@Override
	public void execute()
	{
		ClientData.onPacket(this);
	}
	
	public int getUnlockedSlots()
	{
		return unlockedSlots;
	}
}
