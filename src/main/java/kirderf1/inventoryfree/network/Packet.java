package kirderf1.inventoryfree.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface Packet
{
	void encode(PacketBuffer buffer);
	
	void consume(Supplier<NetworkEvent.Context> event);
}
