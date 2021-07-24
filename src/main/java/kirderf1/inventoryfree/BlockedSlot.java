package kirderf1.inventoryfree;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

/**
 * A custom slot to replace any slots that might become blocked.
 * Note that this isn't a proper wrapper slot, so it can overwrite any custom slot behavior of slots being replaced.
 */
public class BlockedSlot extends Slot
{
	protected final BooleanSupplier blockCondition;
	
	public BlockedSlot(Slot slot, BooleanSupplier blockCondition)
	{
		super(slot.container, slot.getSlotIndex(), slot.x, slot.y);
		this.index = slot.index;
		this.blockCondition = blockCondition;
	}
	
	@Override
	public boolean isActive()
	{
		return !blockCondition.getAsBoolean();
	}
	
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return !blockCondition.getAsBoolean();
	}
	
	@Override
	public boolean mayPickup(Player playerIn)
	{
		return !blockCondition.getAsBoolean();
	}
}