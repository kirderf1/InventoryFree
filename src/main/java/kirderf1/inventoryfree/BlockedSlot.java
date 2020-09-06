package kirderf1.inventoryfree;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.BooleanSupplier;

public class BlockedSlot extends Slot
{
	protected final BooleanSupplier blockCondition;
	
	public BlockedSlot(Slot slot, BooleanSupplier blockCondition)
	{
		super(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
		this.slotNumber = slot.slotNumber;
		this.blockCondition = blockCondition;
	}
	
	@Override
	public boolean isEnabled()
	{
		return !blockCondition.getAsBoolean();
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return !blockCondition.getAsBoolean();
	}
	
	@Override
	public boolean canTakeStack(PlayerEntity playerIn)
	{
		return !blockCondition.getAsBoolean();
	}
}