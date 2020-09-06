package kirderf1.inventoryfree;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod("inventory_free")
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
	
}
