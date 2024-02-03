package kirderf1.inventoryfree.network;

import kirderf1.inventoryfree.InventoryFree;
import kirderf1.inventoryfree.client.ClientData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet for sending the number of unlocked slots to {@link ClientData}.
 */
@MethodsReturnNonnullByDefault
public record UnlockedSlotsPayload(int unlockedSlots) implements ModPayloads.ToClientPayload
{
	public static final ResourceLocation ID = new ResourceLocation(InventoryFree.MOD_ID, "unlocked_slots");
	
	@Override
	public ResourceLocation id()
	{
		return ID;
	}
	
	public static UnlockedSlotsPayload read(FriendlyByteBuf buffer)
	{
		return new UnlockedSlotsPayload(buffer.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeInt(unlockedSlots);
	}

	@Override
	public void execute()
	{
		ClientData.handle(this);
	}
}
