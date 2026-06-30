package io.github.eatmyvenom.litematicin.utils;

public final class PacketSyncState {
	private static volatile boolean synced = false;

	private PacketSyncState() {
	}

	public static boolean isSynced() {
		return synced;
	}

	public static void markSynced() {
		synced = true;
	}

	public static void reset() {
		synced = false;
	}
}
