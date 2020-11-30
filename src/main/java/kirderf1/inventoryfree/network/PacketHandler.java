package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Function;

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
	}
	
	private static <T extends Packet> void register(Class<T> c, Function<PacketBuffer, T> decoder)
	{
		INSTANCE.registerMessage(id++, c, Packet::encode, decoder, Packet::consume);
	}
}