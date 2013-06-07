package no.runsafe.moosic;

import no.runsafe.framework.server.RunsafeLocation;

import java.util.List;

public class TrackPlayer
{
	public TrackPlayer(RunsafeLocation location, MusicTrack musicTrack, float volume)
	{
		this.location = location;
		this.volume = volume;
		this.musicTrack = musicTrack;
	}

	public boolean playNextTick()
	{
		this.currentTick++;

		if (this.currentTick > this.musicTrack.getLength())
			return false;

		List<NoteBlockSound> sounds = this.musicTrack.getNoteBlocksAtTick(this.currentTick);
		for (NoteBlockSound sound : sounds)
			sound.playAtLocation(this.location, this.volume);

		return true;
	}

	public int getTimerID()
	{
		return this.timerID;
	}

	public void setTimerID(int timerID)
	{
		this.timerID = timerID;
	}

	private RunsafeLocation location;
	private float volume;
	private short currentTick = -1;
	private int timerID = -1;
	private MusicTrack musicTrack;
}
