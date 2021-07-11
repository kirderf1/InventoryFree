package kirderf1.inventoryfree.capability;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Capability interface for the capability to store items stuck in locked slots.
 * The inventory size is assumed to be exactly 36 slots to match the number of player inventory slots.
 */
public interface ILockedInventory
{
	@Nonnull
	ItemStack getStack(int slot);
	
	@Nonnull
	ItemStack takeStack(int slot);
	
	void putStack(int slot, ItemStack stack);
	
	List<ItemStack> getAndClearStacks();
}
