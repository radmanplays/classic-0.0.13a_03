package com.mojang.minecraft;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;
import java.util.ArrayList;

public class Entity {
	private Level level;
	public float xo;
	public float yo;
	public float zo;
	public float x;
	public float y;
	public float z;
	public float xd;
	public float yd;
	public float zd;
	public float yRot;
	public float xRot;
	public AABB bb;
	public boolean onGround = false;
	public boolean horizontalCollision = false;
	public boolean removed = false;
	public float heightOffset = 0.0F;
	private float bbWidth = 0.6F;
	public float bbHeight = 1.8F;

	public Entity(Level var1) {
		this.level = var1;
		this.resetPos();
	}

	protected final void resetPos() {
		float var1 = (float)Math.random() * (float)(this.level.width - 2) + 1.0F;
		float var2 = (float)(this.level.depth + 10);
		float var3 = (float)Math.random() * (float)(this.level.height - 2) + 1.0F;
		this.setPos(var1, var2, var3);
	}

	public final void setSize(float var1, float var2) {
		this.bbWidth = var1;
		this.bbHeight = var2;
	}

	public final void setPos(float var1, float var2, float var3) {
		this.x = var1;
		this.y = var2;
		this.z = var3;
		float var4 = this.bbWidth / 2.0F;
		float var5 = this.bbHeight / 2.0F;
		this.bb = new AABB(var1 - var4, var2 - var5, var3 - var4, var1 + var4, var2 + var5, var3 + var4);
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
	}

	public final boolean isFree(float var1, float var2, float var3) {
		AABB var11 = this.bb;
		var11 = new AABB(var11.x0 + var3, var11.y0 + var2, var11.z0 + var3, var11.x1 + var1, var11.y1 + var2, var11.z1 + var3);
		ArrayList var13 = this.level.getCubes(var11);
		if(var13.size() > 0) {
			return false;
		} else {
			Level var12 = this.level;
			int var16 = (int)Math.floor((double)var11.x0);
			int var17 = (int)Math.floor((double)(var11.x1 + 1.0F));
			int var10 = (int)Math.floor((double)var11.y0);
			int var5 = (int)Math.floor((double)(var11.y1 + 1.0F));
			int var6 = (int)Math.floor((double)var11.z0);
			int var15 = (int)Math.floor((double)(var11.z1 + 1.0F));
			if(var16 < 0) {
				var16 = 0;
			}

			if(var10 < 0) {
				var10 = 0;
			}

			if(var6 < 0) {
				var6 = 0;
			}

			if(var17 > var12.width) {
				var17 = var12.width;
			}

			if(var5 > var12.depth) {
				var5 = var12.depth;
			}

			if(var15 > var12.height) {
				var15 = var12.height;
			}

			boolean var10000;
			for(var16 = var16; var16 < var17; ++var16) {
				for(int var7 = var10; var7 < var5; ++var7) {
					for(int var8 = var6; var8 < var15; ++var8) {
						Tile var9 = Tile.tiles[var12.getTile(var16, var7, var8)];
						if(var9 != null && var9.getLiquidType() > 0) {
							var10000 = true;
							return !var10000;
						}
					}
				}
			}

			var10000 = false;
			return !var10000;
		}
	}

	public final void move(float var1, float var2, float var3) {
		float var4 = var1;
		float var5 = var2;
		float var6 = var3;
		Level var10000 = this.level;
		AABB var9 = this.bb;
		float var7 = var9.x0;
		float var8 = var9.y0;
		float var13 = var9.z0;
		float var14 = var9.x1;
		float var15 = var9.y1;
		float var18 = var9.z1;
		if(var1 < 0.0F) {
			var7 += var1;
		}

		if(var1 > 0.0F) {
			var14 += var1;
		}

		if(var2 < 0.0F) {
			var8 += var2;
		}

		if(var2 > 0.0F) {
			var15 += var2;
		}

		if(var3 < 0.0F) {
			var13 += var3;
		}

		if(var3 > 0.0F) {
			var18 += var3;
		}

		ArrayList var16 = var10000.getCubes(new AABB(var7, var8, var13, var14, var15, var18));

		AABB var10;
		float var11;
		float var12;
		int var17;
		AABB var19;
		float var20;
		for(var17 = 0; var17 < var16.size(); ++var17) {
			var19 = (AABB)var16.get(var17);
			var11 = var2;
			var10 = this.bb;
			var9 = var19;
			if(var10.x1 > var9.x0 && var10.x0 < var9.x1) {
				if(var10.z1 > var9.z0 && var10.z0 < var9.z1) {
					if(var2 > 0.0F && var10.y1 <= var9.y0) {
						var12 = var9.y0 - var10.y1;
						if(var12 < var2) {
							var11 = var12;
						}
					}

					if(var11 < 0.0F && var10.y0 >= var9.y1) {
						var12 = var9.y1 - var10.y0;
						if(var12 > var11) {
							var11 = var12;
						}
					}

					var20 = var11;
				} else {
					var20 = var2;
				}
			} else {
				var20 = var2;
			}

			var2 = var20;
		}

		this.bb.move(0.0F, var2, 0.0F);

		for(var17 = 0; var17 < var16.size(); ++var17) {
			var19 = (AABB)var16.get(var17);
			var11 = var1;
			var10 = this.bb;
			var9 = var19;
			if(var10.y1 > var9.y0 && var10.y0 < var9.y1) {
				if(var10.z1 > var9.z0 && var10.z0 < var9.z1) {
					if(var1 > 0.0F && var10.x1 <= var9.x0) {
						var12 = var9.x0 - var10.x1;
						if(var12 < var1) {
							var11 = var12;
						}
					}

					if(var11 < 0.0F && var10.x0 >= var9.x1) {
						var12 = var9.x1 - var10.x0;
						if(var12 > var11) {
							var11 = var12;
						}
					}

					var20 = var11;
				} else {
					var20 = var1;
				}
			} else {
				var20 = var1;
			}

			var1 = var20;
		}

		this.bb.move(var1, 0.0F, 0.0F);

		for(var17 = 0; var17 < var16.size(); ++var17) {
			var19 = (AABB)var16.get(var17);
			var11 = var3;
			var10 = this.bb;
			var9 = var19;
			if(var10.x1 > var9.x0 && var10.x0 < var9.x1) {
				if(var10.y1 > var9.y0 && var10.y0 < var9.y1) {
					if(var3 > 0.0F && var10.z1 <= var9.z0) {
						var12 = var9.z0 - var10.z1;
						if(var12 < var3) {
							var11 = var12;
						}
					}

					if(var11 < 0.0F && var10.z0 >= var9.z1) {
						var12 = var9.z1 - var10.z0;
						if(var12 > var11) {
							var11 = var12;
						}
					}

					var20 = var11;
				} else {
					var20 = var3;
				}
			} else {
				var20 = var3;
			}

			var3 = var20;
		}

		this.bb.move(0.0F, 0.0F, var3);
		this.horizontalCollision = var4 != var1 || var6 != var3;
		this.onGround = var5 != var2 && var5 < 0.0F;
		if(var4 != var1) {
			this.xd = 0.0F;
		}

		if(var5 != var2) {
			this.yd = 0.0F;
		}

		if(var6 != var3) {
			this.zd = 0.0F;
		}

		this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
		this.y = this.bb.y0 + this.heightOffset;
		this.z = (this.bb.z0 + this.bb.z1) / 2.0F;
	}

	public final boolean isInWater() {
		return this.level.containsLiquid(this.bb.grow(0.0F, -0.4F, 0.0F), 1);
	}

	public final boolean isInLava() {
		return this.level.containsLiquid(this.bb, 2);
	}

	public final void moveRelative(float var1, float var2, float var3) {
		float var4 = var1 * var1 + var2 * var2;
		if(var4 >= 0.01F) {
			var4 = var3 / (float)Math.sqrt((double)var4);
			var1 *= var4;
			var2 *= var4;
			var3 = (float)Math.sin((double)this.yRot * Math.PI / 180.0D);
			var4 = (float)Math.cos((double)this.yRot * Math.PI / 180.0D);
			this.xd += var1 * var4 - var2 * var3;
			this.zd += var2 * var4 + var1 * var3;
		}
	}

	public final boolean isLit() {
		int var1 = (int)this.x;
		int var2 = (int)this.y;
		int var3 = (int)this.z;
		return this.level.isLit(var1, var2, var3);
	}

	public void render(float var1) {
	}
}
