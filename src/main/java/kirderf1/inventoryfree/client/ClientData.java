package kirderf1.inventoryfree.client;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.network.UnlockedSlotsPacket;

public class ClientData
{
	private static int unlockedSlots = 0;
	
	public static int getAvailableSlots()
	{
		return InventoryFree.getAvailableSlots(unlockedSlots);
	}
	
	public static int getUnlockedSlots()
	{
		return unlockedSlots;
	}
	
	public static void onPacket(UnlockedSlotsPacket packet)
	{
		unlockedSlots = packet.getUnlockedSlots();
	}
}