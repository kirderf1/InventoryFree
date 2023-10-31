package kirderf1.inventoryfree;

import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BooleanSupplier;

/**
 * A custom slot to replace any slots that might become blocked.
 * Wraps around the replaced slot to the same extent as {@link net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper}.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("JavadocReference")
public class BlockedSlot extends Slot
{
	protected final BooleanSupplier blockCondition;
	protected final Slot wrappedSlot;
	
	public BlockedSlot(Slot slot, BooleanSupplier blockCondition)
	{
		super(slot.container, slot.getSlotIndex(), slot.x, slot.y);
		this.index = slot.index;
		this.blockCondition = blockCondition;
		this.wrappedSlot = slot;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return this.wrappedSlot.mayPlace(stack) && !blockCondition.getAsBoolean();
	}
	
	@Override
	public boolean mayPickup(Player playerIn)
	{
		return this.wrappedSlot.mayPickup(playerIn) && !blockCondition.getAsBoolean();
	}
	
	@Override
	public boolean isActive()
	{
		return this.wrappedSlot.isActive() && !blockCondition.getAsBoolean();
	}
	
	@Override
	public void onTake(Player player, ItemStack stack)
	{
		this.wrappedSlot.onTake(player, stack);
	}
	
	@Override
	public ItemStack getItem()
	{
		return this.wrappedSlot.getItem();
	}
	
	@Override
	public boolean hasItem()
	{
		return this.wrappedSlot.hasItem();
	}
	
	@Override
	public void set(ItemStack stack)
	{
		this.wrappedSlot.set(stack);
	}
	
	@Override
	public void setChanged()
	{
		this.wrappedSlot.setChanged();
	}
	
	@Override
	public int getMaxStackSize()
	{
		return this.wrappedSlot.getMaxStackSize();
	}
	
	@Override
	public int getMaxStackSize(ItemStack stack)
	{
		return this.wrappedSlot.getMaxStackSize(stack);
	}
	
	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
	{
		return this.wrappedSlot.getNoItemIcon();
	}
	
	@Override
	public ItemStack remove(int amount)
	{
		return this.wrappedSlot.remove(amount);
	}
	
	@Override
	public int getSlotIndex()
	{
		return this.wrappedSlot.getSlotIndex();
	}
	
	@Override
	public boolean isSameInventory(Slot other)
	{
		return this.wrappedSlot.isSameInventory(other);
	}
	
	@Override
	public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite)
	{
		return this.wrappedSlot.setBackground(atlas, sprite);
	}
}