package com.mojang.minecraft.particle;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;

public final class Particle extends Entity {
	private float xd;
	private float yd;
	private float zd;
	public int tex;
	float uo;
	float vo;
	private int age = 0;
	private int lifetime = 0;
	float size;

	public Particle(Level var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8) {
		super(var1);
		this.tex = var8;
		this.setSize(0.2F, 0.2F);
		this.heightOffset = this.bbHeight / 2.0F;
		this.setPos(var2, var3, var4);
		this.xd = var5 + (float)(Math.random() * 2.0D - 1.0D) * 0.4F;
		this.yd = var6 + (float)(Math.random() * 2.0D - 1.0D) * 0.4F;
		this.zd = var7 + (float)(Math.random() * 2.0D - 1.0D) * 0.4F;
		float var9 = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
		var2 = (float)Math.sqrt((double)(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd));
		this.xd = this.xd / var2 * var9 * 0.4F;
		this.yd = this.yd / var2 * var9 * 0.4F + 0.1F;
		this.zd = this.zd / var2 * var9 * 0.4F;
		this.uo = (float)Math.random() * 3.0F;
		this.vo = (float)Math.random() * 3.0F;
		this.size = (float)(Math.random() * 0.5D + 0.5D);
		this.lifetime = (int)(4.0D / (Math.random() * 0.9D + 0.1D));
		this.age = 0;
	}

	public final void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if(this.age++ >= this.lifetime) {
			super.removed = true;
		}

		this.yd = (float)((double)this.yd - 0.04D);
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if(this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
		}

	}
}
