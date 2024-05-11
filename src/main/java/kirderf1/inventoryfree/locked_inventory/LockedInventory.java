package kirderf1.inventoryfree.locked_inventory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Data attached to a player that stores items stuck in locked slots.
 * The inventory size is assumed to be exactly 36 slots to match the number of player inventory slots.
 */
@MethodsReturnNonnullByDefault
public final class LockedInventory implements INBTSerializable<ListTag>
{
	private final ItemStackHandler handler = new ItemStackHandler(36);
	
	@Nonnull
	public ItemStack getStack(int slot)
	{
		return handler.getStackInSlot(slot);
	}
	
	@Nonnull
	public ItemStack takeStack(int slot)
	{
		ItemStack stack = handler.getStackInSlot(slot).copy();
		if(!stack.isEmpty())
			handler.setStackInSlot(slot, ItemStack.EMPTY);
		return stack;
	}
	
	public void putStack(int slot, ItemStack stack)
	{
		handler.setStackInSlot(slot, stack.copy());
	}
	
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
	
	@Override
	public ListTag serializeNBT(HolderLookup.Provider registries)
	{
		ListTag list = new ListTag();
		for(int i = 0; i < 36; i++)
		{
			ItemStack stack = getStack(i);
			if(!stack.isEmpty())
			{
				CompoundTag itemTag = new CompoundTag();
				itemTag.putInt("Slot", i);
				list.add(stack.save(registries, itemTag));
			}
		}
		return list;
	}
	
	@Override
	public void deserializeNBT(HolderLookup.Provider registries, ListTag list)
	{
		getAndClearStacks();
		
		for(int i = 0; i < list.size(); i++)
		{
			CompoundTag itemTag = list.getCompound(i);
			int slot = itemTag.getInt("Slot");
			
			if(slot >= 0 && slot < 36)
				putStack(slot, ItemStack.parseOptional(registries, itemTag));
		}
	}
}
