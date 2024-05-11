package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Sets up the network channel and registers packets to it.
 */
@EventBusSubscriber(modid = InventoryFree.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModPayloads
{
	@SubscribeEvent
	private static void register(RegisterPayloadHandlersEvent event)
	{
		event.registrar(InventoryFree.MOD_ID)
				.versioned("1")
				.playToClient(UnlockedSlotsPayload.TYPE, UnlockedSlotsPayload.STREAM_CODEC, ToClientPayload.handler())
				.playToClient(LockedInvSyncPayload.TYPE, LockedInvSyncPayload.STREAM_CODEC, ToClientPayload.handler());
	}
	
	/**
	 * Interface for a packet going from server-side to client-side.
	 */
	public interface ToClientPayload extends CustomPacketPayload
	{
		void execute();
		
		static <P extends ToClientPayload> IPayloadHandler<P> handler()
		{
			return (payload, context) -> payload.execute();
		}
	}
}
