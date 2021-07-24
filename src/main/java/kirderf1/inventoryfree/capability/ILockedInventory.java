package kirderf1.inventoryfree.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Capability interface for the capability to store items stuck in locked slots.
 * The inventory size is assumed to be exactly 36 slots to match the number of player inventory slots.
 */
public interface ILockedInventory extends INBTSerializable<ListTag>
{
	@Nonnull
	ItemStack getStack(int slot);
	
	@Nonnull
	ItemStack takeStack(int slot);
	
	void putStack(int slot, ItemStack stack);
	
	List<ItemStack> getAndClearStacks();
	
	@Override
	default ListTag serializeNBT()
	{
		ListTag list = new ListTag();
		for (int i = 0; i < 36; i++)
		{
			ItemStack stack = getStack(i);
			if (!stack.isEmpty())
			{
				CompoundTag itemTag = new CompoundTag();
				itemTag.putInt("Slot", i);
				stack.save(itemTag);
				list.add(itemTag);
			}
		}
		return list;
	}
	
	@Override
	default void deserializeNBT(ListTag list)
	{
		getAndClearStacks();
		
		for(int i = 0; i < list.size(); i++)
		{
			CompoundTag itemTag = list.getCompound(i);
			int slot = itemTag.getInt("Slot");
			
			if(slot >= 0 && slot < 36)
				putStack(slot, ItemStack.of(itemTag));
		}
	}
}
