package net.romvoid.crashbot.core;

public enum BotState {
	
	PRELOAD("Preloading"),
	LOADING("Loading"),
	LOADING_SHARDS("Shards Loading"),
	LOADED("Fully Loaded"),
	POSTLOAD("Ready");

	private final String s;

	BotState(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}
}
