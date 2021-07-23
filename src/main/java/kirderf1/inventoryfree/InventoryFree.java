package kirderf1.inventoryfree;

import kirderf1.inventoryfree.capability.LockedInvHandler;
import kirderf1.inventoryfree.capability.ModCapabilities;
import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(InventoryFree::onConfigReload);
		MinecraftForge.EVENT_BUS.addListener(InventoryFree::onRegisterCommands);
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
		public final ForgeConfigSpec.IntValue unlockedLostOnDeath;
		public final ForgeConfigSpec.BooleanValue dropItemsInLockedSlots;
		
		private Config(ForgeConfigSpec.Builder builder)
		{
			builder.push("options");
			availableSlots = builder.comment("Defines the number of inventory slots that will be available.")
					.defineInRange("availableSlots", 9, 1, 36);
			unlockSlotItem = builder.comment("Specifies a registry name for an item that on use would unlock a slot and consume the item, or nothing to not allow any item. Examples: minecraft:rabbit_foot, minecraft:nether_star, minestuck:captcha_card")
					.define("unlockSlotItem", "");
			unlockedLostOnDeath = builder.comment("The number of unlocked slots that are relocked on death. If 0, slots will be unaffected by death. If -1, all unlocked slots will be relocked.")
					.defineInRange("unlockedLostOnDeath", 0, -1, 36);
			dropItemsInLockedSlots = builder.comment("If true, items will be dropped when the slot they're in is locked. If false, the items will instead be moved to a locked inventory where they'll stay until the slot is unlocked again.")
					.define("dropItemsInLockedSlots", false);
			builder.pop();
		}
	}
	
	public static void setup(FMLCommonSetupEvent event)
	{
		PacketHandler.registerPackets();
		ModCapabilities.register();
	}
	
	public static void onConfigReload(ModConfig.Reloading event)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			server.submit(() -> LockedInvHandler.onConfigReload(server));
	}
	
	public static void onRegisterCommands(RegisterCommandsEvent event)
	{
		InventorySlotsCommand.register(event.getDispatcher());
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
		return player != null && !player.isCreative() && !player.isSpectator();
	}
	
	public static boolean appliesTo(GameType gameMode)
	{
		return gameMode.isSurvival();
	}
	
}
