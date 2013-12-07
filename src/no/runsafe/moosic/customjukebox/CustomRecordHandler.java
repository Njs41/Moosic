package no.runsafe.moosic.customjukebox;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.block.IBlockBreakEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.event.block.RunsafeBlockBreakEvent;
import no.runsafe.framework.minecraft.item.RunsafeItemStack;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.moosic.MusicHandler;
import no.runsafe.moosic.MusicTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomRecordHandler implements IConfigurationChanged, IPlayerRightClickBlock, IBlockBreakEvent, IPluginEnabled
{
	public CustomRecordHandler(MusicHandler musicHandler, CustomJukeboxRepository repository)
	{
		this.musicHandler = musicHandler;
		this.repository = repository;
	}

	@Override
	public void OnBlockBreakEvent(RunsafeBlockBreakEvent event)
	{
		CustomJukebox jukebox = this.getJukeboxAtLocation(event.getBlock().getLocation());
		if (jukebox != null)
			this.stopJukebox(jukebox);
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		RunsafeLocation blockLocation = targetBlock.getLocation();
		if (targetBlock.is(Item.Decoration.Jukebox))
		{
			CustomJukebox jukebox = this.getJukeboxAtLocation(blockLocation);
			if (jukebox != null)
			{
				this.stopJukebox(jukebox);
				return false;
			}
			else
			{
				if (usingItem != null)
				{
					if (usingItem.is(Item.Special.Crafted.EnchantedBook))
					{
						if (this.isCustomRecord(usingItem))
						{
							player.getInventory().remove(usingItem);
							player.updateInventory();
							jukebox = this.playJukebox(player, new CustomJukebox(blockLocation, usingItem));
							this.repository.storeJukebox(blockLocation, usingItem);
							this.jukeboxes.add(jukebox);
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void OnPluginEnabled()
	{
		// Restore any active jukeboxes from the DB.
		for (CustomJukebox jukebox : this.repository.getJukeboxes())
			this.jukeboxes.add(jukebox);
	}

	private void stopJukebox(CustomJukebox jukebox)
	{
		int playerID = jukebox.getPlayerID();
		if (this.musicHandler.playerExists(playerID))
			this.musicHandler.forceStop(playerID);

		jukebox.ejectRecord();
		this.jukeboxes.remove(jukebox);
		this.repository.deleteJukeboxes(jukebox.getLocation());
	}

	private boolean isCustomRecord(RunsafeItemStack item)
	{
		return item instanceof RunsafeMeta && ((RunsafeMeta) item).getDisplayName().equalsIgnoreCase(this.customRecordName);
	}

	public CustomJukebox getJukeboxAtLocation(RunsafeLocation location)
	{
		for (CustomJukebox jukebox : this.jukeboxes)
			if (jukebox.getLocation().getWorld().equals(location.getWorld()))
				if (jukebox.getLocation().distance(location) < 1)
					return jukebox;

		return null;
	}

	private CustomJukebox playJukebox(IPlayer player, CustomJukebox jukebox)
	{
		File musicFile = this.musicHandler.loadSongFile(jukebox.getSongName() + ".nbs");
		if (musicFile.exists())
		{
			MusicTrack musicTrack = null;
			try
			{
				musicTrack = new MusicTrack(musicFile);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (musicTrack != null)
				jukebox.setPlayerID(this.musicHandler.startSong(musicTrack, jukebox.getLocation(), 65));
		}
		else
		{
			// Corrupt record, presumably.
			player.sendColouredMessage("&cThe record cracks and scratches as you put it in the jukebox.");
		}
		return jukebox;
	}

	public void onTrackPlayerStopped(int trackPlayerID)
	{
		for (CustomJukebox jukebox : this.jukeboxes)
		{
			if (jukebox.getPlayerID() == trackPlayerID)
			{
				this.jukeboxes.remove(jukebox);
				jukebox.setPlayerID(-1);
				this.jukeboxes.add(jukebox);
			}
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		this.customRecordName = configuration.getConfigValueAsString("customRecordName");
	}

	private final List<CustomJukebox> jukeboxes = new ArrayList<CustomJukebox>();
	private String customRecordName;
	private final MusicHandler musicHandler;
	private final CustomJukeboxRepository repository;
}
