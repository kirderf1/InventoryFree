package kirderf1.inventoryfree.capability;

import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Registers and attaches capabilities provided by InventoryFree.
 */
@Mod.EventBusSubscriber(modid = InventoryFree.MOD_ID)
public class ModCapabilities
{
	private static final ResourceLocation LOCKED_INV_NAME = new ResourceLocation(InventoryFree.MOD_ID, "locked_inv");
	
	/**
	 * Capability to store items that are stuck in locked slots
	 */
	@CapabilityInject(ILockedInventory.class)
	public static Capability<ILockedInventory> LOCKED_INV_CAPABILITY = null;
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(ILockedInventory.class);
	}
	
	@SubscribeEvent
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
		{
			LockedInvProvider provider = new LockedInvProvider();
			event.addCapability(LOCKED_INV_NAME, provider);
		}
	}
	
	/**
	 * Basic capability provider for providing and serializing a LockedInventory
	 */
	private static class LockedInvProvider implements ICapabilitySerializable<ListTag>
	{
		private final LockedInventory lockedInv = new LockedInventory();
		private final LazyOptional<ILockedInventory> optional = LazyOptional.of(() -> lockedInv);
		
		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
		{
			if(cap == LOCKED_INV_CAPABILITY)
				return optional.cast();
			return LazyOptional.empty();
		}
		
		@Override
		public ListTag serializeNBT()
		{
			return lockedInv.serializeNBT();
		}
		
		@Override
		public void deserializeNBT(ListTag nbt)
		{
			lockedInv.deserializeNBT(nbt);
		}
	}
}