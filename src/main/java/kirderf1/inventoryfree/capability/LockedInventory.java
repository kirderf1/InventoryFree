package kirderf1.inventoryfree.capability;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic capability implementation of {@link ILockedInventory}.
 */
public class LockedInventory implements ILockedInventory
{
	private final ItemStackHandler handler = new ItemStackHandler(36);
	
	@Nonnull
	@Override
	public ItemStack getStack(int slot)
	{
		return handler.getStackInSlot(slot);
	}
	
	@Nonnull
	@Override
	public ItemStack takeStack(int slot)
	{
		ItemStack stack = handler.getStackInSlot(slot).copy();
		if(!stack.isEmpty())
			handler.setStackInSlot(slot, ItemStack.EMPTY);
		return stack;
	}
	
	@Override
	public void putStack(int slot, ItemStack stack)
	{
		handler.setStackInSlot(slot, stack.copy());
	}
	
	@Override
	public List<ItemStack> getAndClearStacks()
	{
		List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
		for(int i = 0; i < handler.getSlots(); i++)
		{
			ItemStack stack  = takeStack(i);
			if(!stack.isEmpty())
				stacks.add(stack);
		}
		
		return stacks;
	}
}