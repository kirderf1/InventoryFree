package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.network.PacketBuffer;

public class AvailableSlotsPacket implements Packet.ToClient
{
	private final int availableSlots;
	
	public AvailableSlotsPacket(int availableSlots)
	{
		this.availableSlots = availableSlots;
	}
	
	public static AvailableSlotsPacket decode(PacketBuffer buffer)
	{
		return new AvailableSlotsPacket(buffer.readInt());
	}
	
	@Override
	public void encode(PacketBuffer buffer)
	{
		buffer.writeInt(availableSlots);
	}
	
	@Override
	public void execute()
	{
		ClientData.onPacket(this);
	}
	
	public int getAvailableSlots()
	{
		return availableSlots;
	}
}
