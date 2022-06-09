package kirderf1.inventoryfree;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Defines the /inventory_slots command and all of its subcommands.
 */
public class InventorySlotsCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("inventory_slots").requires(source -> source.hasPermission(2))
				.then(setAvailable()).then(setUnlocked()).then(unlock()).then(lock()).then(clear()).then(get()));
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> setAvailable()
	{
		return Commands.literal("set_available").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer(1, 36))
						.executes(context -> setUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								fromAvailable(IntegerArgumentType.getInteger(context, "amount"))))));
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> setUnlocked()
	{
		return Commands.literal("set_unlocked").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> setUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								IntegerArgumentType.getInteger(context, "amount")))));
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> unlock()
	{
		return Commands.literal("unlock").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> addUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								IntegerArgumentType.getInteger(context, "amount")))));
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> lock()
	{
		return Commands.literal("lock").then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> addUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								-IntegerArgumentType.getInteger(context, "amount")))));
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> clear()
	{
		return Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players())
						.executes(context -> setUnlockedFor(context.getSource(), EntityArgument.getPlayers(context, "targets"),
								0)));
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> get()
	{
		return Commands.literal("get").then(Commands.argument("target", EntityArgument.player())
				.executes(context -> getFor(context.getSource(), EntityArgument.getPlayer(context, "target"))));
	}
	
	private static int fromAvailable(int availableSlots)
	{
		return availableSlots - InventoryFree.CONFIG.availableSlots.get();
	}
	
	private static int setUnlockedFor(CommandSourceStack source, Collection<ServerPlayer> targets, int unlockedSlots)
	{
		targets.forEach(player -> PlayerData.setUnlockedSlots(player, unlockedSlots));
		source.sendSuccess(Component.literal("Set unlocked slots for "+targets.size()+" players"), true);
		return targets.size();
	}
	
	private static int addUnlockedFor(CommandSourceStack source, Collection<ServerPlayer> targets, int addedSlots)
	{
		targets.forEach(player -> PlayerData.unlockSlots(player, addedSlots));
		source.sendSuccess(Component.literal("Changed unlocked slots for "+targets.size()+" players"), true);
		return targets.size();
	}
	
	private static int getFor(CommandSourceStack source, ServerPlayer target)
	{
		source.sendSuccess(Component.literal("Unlocked slots: "+PlayerData.getUnlockedSlots(target)), false);
		source.sendSuccess(Component.literal("Available slots: "+PlayerData.getAvailableSlots(target)), false);
		return 1;
	}
}