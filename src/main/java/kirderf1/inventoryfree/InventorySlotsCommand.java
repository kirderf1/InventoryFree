package kirderf1.inventoryfree;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;

public class InventorySlotsCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("inventory_slots").requires(source -> source.hasPermissionLevel(2))
				.then(setAvailable()).then(setUnlocked()).then(unlock()).then(lock()).then(clear()).then(get()));
	}
	
	private static LiteralArgumentBuilder<CommandSource> setAvailable()
	{
		return Commands.literal("set_available").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer(1, 36))
						.executes(context -> setUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								fromAvailable(IntegerArgumentType.getInteger(context, "amount"))))));
	}
	
	private static LiteralArgumentBuilder<CommandSource> setUnlocked()
	{
		return Commands.literal("set_unlocked").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> setUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								IntegerArgumentType.getInteger(context, "amount")))));
	}
	
	private static LiteralArgumentBuilder<CommandSource> unlock()
	{
		return Commands.literal("unlock").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> addUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								IntegerArgumentType.getInteger(context, "amount")))));
	}
	
	private static LiteralArgumentBuilder<CommandSource> lock()
	{
		return Commands.literal("lock").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> addUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								-IntegerArgumentType.getInteger(context, "amount")))));
	}
	
	private static LiteralArgumentBuilder<CommandSource> clear()
	{
		return Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players())
						.executes(context -> setUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								0)));
	}
	
	private static LiteralArgumentBuilder<CommandSource> get()
	{
		return Commands.literal("get").then(Commands.argument("target", EntityArgument.player())
				.executes(context -> getFor(context.getSource(), EntityArgument.getPlayer(context, "target"))));
	}
	
	private static int fromAvailable(int availableSlots)
	{
		return availableSlots - InventoryFree.CONFIG.availableSlots.get();
	}
	
	private static int setUnlockedFor(CommandSource source, Collection<ServerPlayerEntity> targets, int unlockedSlots)
	{
		targets.forEach(player -> PlayerData.setUnlockedSlots(player, unlockedSlots));
		source.sendFeedback(new StringTextComponent("Set unlocked slots for "+targets.size()+" players"), true);
		return targets.size();
	}
	
	private static int addUnlockedFor(CommandSource source, Collection<ServerPlayerEntity> targets, int addedSlots)
	{
		targets.forEach(player -> PlayerData.unlockSlots(player, addedSlots));
		source.sendFeedback(new StringTextComponent("Changed unlocked slots for "+targets.size()+" players"), true);
		return targets.size();
	}
	
	private static int getFor(CommandSource source, ServerPlayerEntity target)
	{
		source.sendFeedback(new StringTextComponent("Unlocked slots: "+PlayerData.getUnlockedSlots(target)), false);
		source.sendFeedback(new StringTextComponent("Available slots: "+PlayerData.getAvailableSlots(target)), false);
		return 1;
	}
}