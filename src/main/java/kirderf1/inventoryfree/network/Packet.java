package kirderf1.inventoryfree.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Basic packet interface. Contains more specific packet interfaces.
 */
public interface Packet
{
	void encode(FriendlyByteBuf buffer);
	
	void consume(Supplier<NetworkEvent.Context> context);
	
	/**
	 * Interface for a packet going from server-side to client-side.
	 */
	interface ToClient extends Packet
	{
		@Override
		default void consume(Supplier<NetworkEvent.Context> context)
		{
			context.get().enqueueWork(this::execute);
			context.get().setPacketHandled(true);
		}
		
		void execute();
	}
}