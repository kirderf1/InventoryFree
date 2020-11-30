package kirderf1.inventoryfree;

import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The central mod class. Currently holds the config and the player/slot conditions.
 */
@Mod(InventoryFree.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = InventoryFree.MOD_ID)
public class InventoryFree
{
	public static final String MOD_ID = "inventory_free";
	
	public static final Config CONFIG;
	private static final ForgeConfigSpec configSpec;
	
	public InventoryFree()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, configSpec);
	}
	
	static
	{
		Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
		CONFIG = specPair.getLeft();
		configSpec = specPair.getRight();
	}
	
	public static class Config
	{
		public final ForgeConfigSpec.IntValue availableSlots;
		
		private Config(ForgeConfigSpec.Builder builder)
		{
			builder.push("options");
			availableSlots = builder.comment("Defines the number of inventory slots that will be available.")
					.defineInRange("availableSlots", 9, 1, 36);
			builder.pop();
		}
	}
	
	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event)
	{
		PacketHandler.registerPackets();
	}
	
	public static boolean isSlotToBeBlocked(int index, int availableSlots)
	{
		return index >= availableSlots && index < 36;
	}
	
	public static boolean appliesTo(PlayerEntity player)
	{
		return !player.isCreative() && !player.isSpectator();
	}
	
}
