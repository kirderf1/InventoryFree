package kirderf1.inventoryfree;

import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The central mod class. Currently holds the config and the player/slot conditions.
 */
@Mod(InventoryFree.MOD_ID)
public class InventoryFree
{
	public static final String MOD_ID = "inventory_free";
	
	public static final Config CONFIG;
	private static final ForgeConfigSpec configSpec;
	
	public InventoryFree()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, configSpec);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(InventoryFree::setup);
		MinecraftForge.EVENT_BUS.addListener(InventoryFree::onServerStarting);
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
		public final ForgeConfigSpec.ConfigValue<String> unlockSlotItem;
		
		private Config(ForgeConfigSpec.Builder builder)
		{
			builder.push("options");
			availableSlots = builder.comment("Defines the number of inventory slots that will be available.")
					.defineInRange("availableSlots", 9, 1, 36);
			unlockSlotItem = builder.comment("Specifies a registry name for an item that on use would unlock a slot and consume the item, or nothing to not allow any item. Examples: minecraft:rabbit_foot, minecraft:nether_star, minestuck:captcha_card")
					.define("unlockSlotItem", "");
			builder.pop();
		}
	}
	
	public static void setup(FMLCommonSetupEvent event)
	{
		PacketHandler.registerPackets();
	}
	
	public static void onServerStarting(FMLServerStartingEvent event)
	{
		InventorySlotsCommand.register(event.getCommandDispatcher());
	}
	
	public static int getAvailableSlots(int unlockedSlots)
	{
		return MathHelper.clamp(InventoryFree.CONFIG.availableSlots.get() + unlockedSlots, 1, 36);
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
