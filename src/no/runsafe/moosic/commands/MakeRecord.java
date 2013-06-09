package no.runsafe.moosic.commands;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.util.HashMap;

public class MakeRecord extends PlayerCommand implements IConfigurationChanged
{
	public MakeRecord()
	{
		super("makerecord", "Forges a record with the item currently being held.", "runsafe.moosic.record.make", "song");
		captureTail();
	}

	@Override
	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
	{
		RunsafeItemStack item = Item.Special.Crafted.EnchantedBook.getItem();
		item.setAmount(1);
		item.addLore(parameters.get("song")).setDisplayName(customRecordName);
		executor.getInventory().addItems(item);
		return "&2Success!";
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		this.customRecordName = configuration.getConfigValueAsString("customRecordName");
	}

	private String customRecordName;
}
