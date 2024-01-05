package kirderf1.inventoryfree;

import kirderf1.inventoryfree.locked_inventory.LockedInvHandler;
import kirderf1.inventoryfree.network.PacketHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * The central mod class. Also holds the config and the player/slot conditions.
 */
@Mod(InventoryFree.MOD_ID)
public class InventoryFree
{
	public static final String MOD_ID = "inventory_free";
	
	@SuppressWarnings("removal")
	public InventoryFree()
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, configSpec);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(InventoryFree::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(InventoryFree::onConfigReload);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(SlotUnlocker::verifyUnlockItem);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ModCapabilities::register);
		NeoForge.EVENT_BUS.addListener(InventoryFree::onRegisterCommands);
		NeoForge.EVENT_BUS.addGenericListener(Entity.class, ModCapabilities::attachEntityCapability);
	}
	
	private static void setup(FMLCommonSetupEvent event)
	{
		PacketHandler.registerPackets();
	}
	
	private static void onConfigReload(ModConfigEvent.Reloading event)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null)
			server.submit(() -> LockedInvHandler.onConfigReload(server));
	}
	
	private static void onRegisterCommands(RegisterCommandsEvent event)
	{
		InventorySlotsCommand.register(event.getDispatcher());
	}
	
	/**
	 * Instance of the mod config.
	 */
	public static final Config CONFIG;
	private static final ModConfigSpec configSpec;
	
	static
	{
		var specPair = new ModConfigSpec.Builder().configure(Config::new);
		CONFIG = specPair.getLeft();
		configSpec = specPair.getRight();
	}
	
	/**
	 * Class that defines the server type mod config.
	 * This is where all the config options are defined.
	 */
	public static class Config
	{
		public final ModConfigSpec.IntValue availableSlots;
		public final ModConfigSpec.ConfigValue<String> unlockSlotItem;
		public final ModConfigSpec.IntValue unlockedLostOnDeath;
		public final ModConfigSpec.BooleanValue dropItemsInLockedSlots;
		public final ModConfigSpec.EnumValue<SlotUnlocker.CostProgression> costProgression;
		public final ModConfigSpec.IntValue costMultiplier;
		
		private Config(ModConfigSpec.Builder builder)
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
			costProgression = builder.comment("Specifies how the cost of unlocking a slot changes after each unlock. CONSTANT: 1, 1, 1... LINEAR: 1, 2, 3, 4... EXPONENTIAL: 1, 2, 4, 8, 16, 32, 64, after which no more slots can be unlocked.")
					.defineEnum("costProgression", SlotUnlocker.CostProgression.CONSTANT);
			costMultiplier = builder.comment("A multiplier to the cost given by the cost progression. A multiplier of 3 will affect cost progressions in the following way. CONSTANT: 3, 3, 3... LINEAR: 3, 6, 9, 12... EXPONENTIAL: 3, 6, 12, 24, 48")
					.defineInRange("costMultiplier", 1, 1, 64);
			builder.pop();
		}
	}
	
	/**
	 * Returns the number of available slots based on the number of unlocked slots.
	 * Does not take player game type into account.
	 */
	public static int getAvailableSlots(int unlockedSlots)
	{
		return Mth.clamp(InventoryFree.CONFIG.availableSlots.get() + unlockedSlots, 1, 36);
	}
	
	/**
	 * Given that the index is for a player inventory slot,
	 * determines if the slot should be blocked based on the number of available slots.
	 */
	public static boolean isSlotToBeBlocked(int index, int availableSlots)
	{
		return index >= availableSlots && index < 36;
	}
	
	/**
	 * Determines if the player should have their slots blocked.
	 */
	public static boolean appliesTo(Player player)
	{
		return player != null && !player.isCreative() && !player.isSpectator();
	}
	
	/**
	 * Determines if a player with this game type should have their slots blocked.
	 */
	public static boolean appliesTo(GameType gameMode)
	{
		return gameMode.isSurvival();
	}
}