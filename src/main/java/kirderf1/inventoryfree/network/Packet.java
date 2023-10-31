package kirderf1.inventoryfree.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Basic packet interface. Contains more specific packet interfaces.
 */
public interface Packet
{
	void encode(FriendlyByteBuf buffer);
	
	void consume(NetworkEvent.Context context);
	
	/**
	 * Interface for a packet going from server-side to client-side.
	 */
	interface ToClient extends Packet
	{
		@Override
		default void consume(NetworkEvent.Context context)
		{
			context.enqueueWork(this::execute);
			context.setPacketHandled(true);
		}
		
		void execute();
	}
}