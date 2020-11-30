package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.network.PacketBuffer;

public class UnlockedSlotsPacket implements Packet.ToClient
{
	private final int unlockedSlots;
	
	public UnlockedSlotsPacket(int unlockedSlots)
	{
		this.unlockedSlots = unlockedSlots;
	}
	
	public static UnlockedSlotsPacket decode(PacketBuffer buffer)
	{
		return new UnlockedSlotsPacket(buffer.readInt());
	}
	
	@Override
	public void encode(PacketBuffer buffer)
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
