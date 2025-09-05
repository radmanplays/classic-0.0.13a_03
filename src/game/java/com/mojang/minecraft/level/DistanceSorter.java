package com.mojang.minecraft.level;

import com.mojang.minecraft.Player;
import java.util.Comparator;

public final class DistanceSorter implements Comparator {
	private Player player;

	public DistanceSorter(Player var1) {
		this.player = var1;
	}

	public final int compare(Object var1, Object var2) {
		Chunk var10001 = (Chunk)var1;
		Chunk var4 = (Chunk)var2;
		Chunk var3 = var10001;
		return var3.compare(this.player) < var4.compare(this.player) ? -1 : 1;
	}
}
