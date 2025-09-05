package com.mojang.minecraft;

import com.mojang.minecraft.level.Level;

public final class Player extends Entity {
	boolean[] keys = new boolean[10];

	public Player(Level var1) {
		super(var1);
		this.heightOffset = 1.62F;
	}

	public final void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		float var1 = 0.0F;
		float var2 = 0.0F;
		boolean var3 = this.isInWater();
		boolean var4 = this.isInLava();
		if(this.keys[0]) {
			var2 = 0.0F - 1.0F;
		}

		if(this.keys[1]) {
			++var2;
		}

		if(this.keys[2]) {
			var1 = 0.0F - 1.0F;
		}

		if(this.keys[3]) {
			++var1;
		}

		if(this.keys[4]) {
			if(var3) {
				this.yd += 0.04F;
			} else if(var4) {
				this.yd += 0.04F;
			} else if(this.onGround) {
				this.yd = 0.42F;
				this.keys[4] = false;
			}
		}

		float var5;
		if(var3) {
			var5 = this.y;
			this.moveRelative(var1, var2, 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.8F;
			this.yd *= 0.8F;
			this.zd *= 0.8F;
			this.yd = (float)((double)this.yd - 0.02D);
			if(this.horizontalCollision && this.isFree(this.xd, this.yd + 0.6F - this.y + var5, this.zd)) {
				this.yd = 0.3F;
			}

		} else if(var4) {
			var5 = this.y;
			this.moveRelative(var1, var2, 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.5F;
			this.yd *= 0.5F;
			this.zd *= 0.5F;
			this.yd = (float)((double)this.yd - 0.02D);
			if(this.horizontalCollision && this.isFree(this.xd, this.yd + 0.6F - this.y + var5, this.zd)) {
				this.yd = 0.3F;
			}

		} else {
			this.moveRelative(var1, var2, this.onGround ? 0.1F : 0.02F);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.91F;
			this.yd *= 0.98F;
			this.zd *= 0.91F;
			this.yd = (float)((double)this.yd - 0.08D);
			if(this.onGround) {
				this.xd *= 0.6F;
				this.zd *= 0.6F;
			}

		}
	}
}
