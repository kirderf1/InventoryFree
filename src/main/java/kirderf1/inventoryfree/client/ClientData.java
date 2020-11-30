package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.network.AvailableSlotsPacket;

public class ClientData
{
	private static int availableSlots = 1;
	
	public static int getAvailableSlots()
	{
		return availableSlots;
	}
	
	public static void onPacket(AvailableSlotsPacket packet)
	{
		availableSlots = packet.getAvailableSlots();
	}
}