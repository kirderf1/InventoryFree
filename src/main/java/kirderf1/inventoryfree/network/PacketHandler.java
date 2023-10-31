package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.MessageFunctions;
import net.neoforged.neoforge.network.simple.SimpleChannel;

/**
 * Sets up the network channel and registers packets to it.
 */
public class PacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(InventoryFree.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	
	private static int id = 0;
	
	public static void registerPackets()
	{
		if(id != 0)
			throw new IllegalStateException("Packets already registered!");
		
		register(UnlockedSlotsPacket.class, UnlockedSlotsPacket::decode);
		register(LockedInvSyncPacket.class, LockedInvSyncPacket::decode);
	}
	
	private static <T extends Packet> void register(Class<T> c, MessageFunctions.MessageDecoder<T> decoder)
	{
		INSTANCE.registerMessage(id++, c, Packet::encode, decoder, Packet::consume);
	}
}