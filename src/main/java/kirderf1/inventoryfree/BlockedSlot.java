package kirderf1.inventoryfree;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.BooleanSupplier;

public class BlockedSlot extends Slot
{
	protected final BooleanSupplier disableCondition;
	
	public BlockedSlot(Slot slot, BooleanSupplier disableCondition)
	{
		super(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
		this.slotNumber = slot.slotNumber;
		this.disableCondition = () -> disableCondition.getAsBoolean() || !InventoryEnforcer.isSlotToBeBlocked(getSlotIndex());
	}
	
	@Override
	public boolean isEnabled()
	{
		return disableCondition.getAsBoolean();
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return disableCondition.getAsBoolean();
	}
	
	@Override
	public boolean canTakeStack(PlayerEntity playerIn)
	{
		return disableCondition.getAsBoolean();
	}
}