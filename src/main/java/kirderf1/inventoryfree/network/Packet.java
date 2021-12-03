package kirderf1.inventoryfree.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface Packet
{
	void encode(FriendlyByteBuf buffer);
	
	void consume(Supplier<NetworkEvent.Context> context);
	
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