package kirderf1.inventoryfree.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	
	static class Storage implements Capability.IStorage<ILockedInventory>
	{
		@Nullable
		@Override
		public INBT writeNBT(Capability<ILockedInventory> capability, ILockedInventory instance, Direction side)
		{
			ListNBT list = new ListNBT();
			for (int i = 0; i < 36; i++)
			{
				ItemStack stack = instance.getStack(i);
				if (!stack.isEmpty())
				{
					CompoundNBT itemTag = new CompoundNBT();
					itemTag.putInt("Slot", i);
					stack.save(itemTag);
					list.add(itemTag);
				}
			}
			return list;
		}
		
		@Override
		public void readNBT(Capability<ILockedInventory> capability, ILockedInventory instance, Direction side, INBT nbt)
		{
			instance.getAndClearStacks();
			
			if(nbt instanceof ListNBT)
			{
				ListNBT list = (ListNBT) nbt;
				for(int i = 0; i < list.size(); i++)
				{
					CompoundNBT itemTag = list.getCompound(i);
					int slot = itemTag.getInt("Slot");
					
					if(slot >= 0 && slot < 36)
						instance.putStack(slot, ItemStack.of(itemTag));
				}
			} else throw new IllegalArgumentException("Expected a ListNBT, but got " + nbt.getClass());
		}
	}
}