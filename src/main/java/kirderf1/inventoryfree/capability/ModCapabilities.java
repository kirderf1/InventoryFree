package kirderf1.inventoryfree.capability;

import kirderf1.inventoryfree.InventoryFree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
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
		CapabilityManager.INSTANCE.register(ILockedInventory.class, new LockedInventory.Storage(), LockedInventory::new);
	}
	
	@SubscribeEvent
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity)
		{
			LockedInvProvider provider = new LockedInvProvider();
			event.addCapability(LOCKED_INV_NAME, provider);
			event.addListener(provider.optional::invalidate);
		}
	}
	
	/**
	 * Basic capability provider for providing and serializing a LockedInventory
	 */
	private static class LockedInvProvider implements ICapabilitySerializable<ListNBT>
	{
		private final ILockedInventory lockedInv = new LockedInventory();
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
		public ListNBT serializeNBT()
		{
			return (ListNBT) LOCKED_INV_CAPABILITY.writeNBT(lockedInv, null);
		}
		
		@Override
		public void deserializeNBT(ListNBT nbt)
		{
			LOCKED_INV_CAPABILITY.readNBT(lockedInv, null, nbt);
		}
	}
}