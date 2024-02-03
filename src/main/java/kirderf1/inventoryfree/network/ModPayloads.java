package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.registration.IDirectionAwarePayloadHandlerBuilder;

/**
 * Sets up the network channel and registers packets to it.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModPayloads
{
	@SubscribeEvent
	private static void register(RegisterPayloadHandlerEvent event)
	{
		event.registrar(InventoryFree.MOD_ID)
				.versioned("1")
				.play(UnlockedSlotsPayload.ID, UnlockedSlotsPayload::read, ToClientPayload::handler)
				.play(LockedInvSyncPayload.ID, LockedInvSyncPayload::read, ToClientPayload::handler);
	}
	
	/**
	 * Interface for a packet going from server-side to client-side.
	 */
	public interface ToClientPayload extends CustomPacketPayload
	{
		void execute();
		
		static <P extends ToClientPayload> void handler(IDirectionAwarePayloadHandlerBuilder<P, IPlayPayloadHandler<P>> builder)
		{
			builder.client((payload, context) -> context.workHandler().execute(payload::execute));
		}
	}
}