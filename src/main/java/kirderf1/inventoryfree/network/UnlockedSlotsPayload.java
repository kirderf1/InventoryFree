package kirderf1.inventoryfree.network;

import io.netty.buffer.ByteBuf;
import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet for sending the number of unlocked slots to {@link ClientData}.
 */
@MethodsReturnNonnullByDefault
public record UnlockedSlotsPayload(int unlockedSlots) implements ModPayloads.ToClientPayload
{
	public static final Type<UnlockedSlotsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(InventoryFree.MOD_ID, "unlocked_slots"));
	
	public static final StreamCodec<ByteBuf, UnlockedSlotsPayload> STREAM_CODEC = ByteBufCodecs.INT.map(UnlockedSlotsPayload::new, UnlockedSlotsPayload::unlockedSlots);
	
	@Override
	public Type<UnlockedSlotsPayload> type()
	{
		return TYPE;
	}
	
	@Override
	public void execute()
	{
		ClientData.handle(this);
	}
}
