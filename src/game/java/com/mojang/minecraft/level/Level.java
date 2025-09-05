package com.mojang.minecraft.level;

import com.mojang.minecraft.HitResult;
import com.mojang.minecraft.character.Vec3;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;
import java.util.ArrayList;
import java.util.Random;

public final class Level {
	public int width;
	public int height;
	public int depth;
	public byte[] blocks;
	private int[] heightMap;
	ArrayList levelListeners = new ArrayList();
	public Random random = new Random();
	public int randValue = this.random.nextInt();
	public String name;
	public String creator;
	public long createTime;
	public int unprocessed = 0;

	public final void setData(int var1, int var2, int var3, byte[] var4) {
		this.width = var1;
		this.height = var3;
		this.depth = var2;
		this.blocks = var4;
		this.heightMap = new int[var1 * var3];
		this.calcLightDepths(0, 0, var1, var3);

		for(var1 = 0; var1 < this.levelListeners.size(); ++var1) {
			((LevelRenderer)this.levelListeners.get(var1)).allChanged();
		}

	}

	private void calcLightDepths(int var1, int var2, int var3, int var4) {
		for(int var5 = var1; var5 < var1 + var3; ++var5) {
			for(int var6 = var2; var6 < var2 + var4; ++var6) {
				int var7 = this.heightMap[var5 + var6 * this.width];

				int var8;
				for(var8 = this.depth - 1; var8 > 0; --var8) {
					Tile var14 = Tile.tiles[this.getTile(var5, var8, var6)];
					if(var14 == null ? false : var14.blocksLight()) {
						break;
					}
				}

				this.heightMap[var5 + var6 * this.width] = var8 + 1;
				if(var7 != var8) {
					int var9 = var7 < var8 ? var7 : var8;
					var7 = var7 > var8 ? var7 : var8;

					for(var8 = 0; var8 < this.levelListeners.size(); ++var8) {
						LevelRenderer var10 = (LevelRenderer)this.levelListeners.get(var8);
						var10.setDirty(var5 - 1, var9 - 1, var6 - 1, var5 + 1, var7 + 1, var6 + 1);
					}
				}
			}
		}

	}

	public final ArrayList getCubes(AABB var1) {
		ArrayList var2 = new ArrayList();
		int var3 = (int)Math.floor((double)var1.x0);
		int var4 = (int)Math.floor((double)(var1.x1 + 1.0F));
		int var5 = (int)Math.floor((double)var1.y0);
		int var6 = (int)Math.floor((double)(var1.y1 + 1.0F));
		int var7 = (int)Math.floor((double)var1.z0);
		int var12 = (int)Math.floor((double)(var1.z1 + 1.0F));

		for(var3 = var3; var3 < var4; ++var3) {
			for(int var8 = var5; var8 < var6; ++var8) {
				for(int var9 = var7; var9 < var12; ++var9) {
					AABB var10;
					if(var3 >= 0 && var8 >= 0 && var9 >= 0 && var3 < this.width && var8 < this.depth && var9 < this.height) {
						Tile var11 = Tile.tiles[this.getTile(var3, var8, var9)];
						if(var11 != null) {
							var10 = var11.getAABB(var3, var8, var9);
							if(var10 != null) {
								var2.add(var10);
							}
						}
					} else if(var3 < 0 || var8 < 0 || var9 < 0 || var3 >= this.width || var9 >= this.height) {
						var10 = Tile.unbreakable.getAABB(var3, var8, var9);
						if(var10 != null) {
							var2.add(var10);
						}
					}
				}
			}
		}

		return var2;
	}

	public final boolean setTile(int var1, int var2, int var3, int var4) {
		if(var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height) {
			if(var4 == this.blocks[(var2 * this.height + var3) * this.width + var1]) {
				return false;
			} else {
				this.blocks[(var2 * this.height + var3) * this.width + var1] = (byte)var4;
				this.updateNeighborAt(var1 - 1, var2, var3, var4);
				this.updateNeighborAt(var1 + 1, var2, var3, var4);
				this.updateNeighborAt(var1, var2 - 1, var3, var4);
				this.updateNeighborAt(var1, var2 + 1, var3, var4);
				this.updateNeighborAt(var1, var2, var3 - 1, var4);
				this.updateNeighborAt(var1, var2, var3 + 1, var4);
				this.calcLightDepths(var1, var3, 1, 1);

				for(var4 = 0; var4 < this.levelListeners.size(); ++var4) {
					LevelRenderer var5 = (LevelRenderer)this.levelListeners.get(var4);
					var5.setDirty(var1 - 1, var2 - 1, var3 - 1, var1 + 1, var2 + 1, var3 + 1);
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public final boolean setTileNoUpdate(int var1, int var2, int var3, int var4) {
		if(var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height) {
			if(var4 == this.blocks[(var2 * this.height + var3) * this.width + var1]) {
				return false;
			} else {
				this.blocks[(var2 * this.height + var3) * this.width + var1] = (byte)var4;
				return true;
			}
		} else {
			return false;
		}
	}

	private void updateNeighborAt(int var1, int var2, int var3, int var4) {
		if(var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height) {
			Tile var5 = Tile.tiles[this.blocks[(var2 * this.height + var3) * this.width + var1]];
			if(var5 != null) {
				var5.neighborChanged(this, var1, var2, var3, var4);
			}

		}
	}

	public final boolean isLit(int var1, int var2, int var3) {
		return var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height ? var2 >= this.heightMap[var1 + var3 * this.width] : true;
	}

	public final int getTile(int var1, int var2, int var3) {
		return var1 >= 0 && var2 >= 0 && var3 >= 0 && var1 < this.width && var2 < this.depth && var3 < this.height ? this.blocks[(var2 * this.height + var3) * this.width + var1] : 0;
	}

	public final boolean containsLiquid(AABB var1, int var2) {
		int var3 = (int)Math.floor((double)var1.x0);
		int var4 = (int)Math.floor((double)(var1.x1 + 1.0F));
		int var5 = (int)Math.floor((double)var1.y0);
		int var6 = (int)Math.floor((double)(var1.y1 + 1.0F));
		int var7 = (int)Math.floor((double)var1.z0);
		int var11 = (int)Math.floor((double)(var1.z1 + 1.0F));
		if(var3 < 0) {
			var3 = 0;
		}

		if(var5 < 0) {
			var5 = 0;
		}

		if(var7 < 0) {
			var7 = 0;
		}

		if(var4 > this.width) {
			var4 = this.width;
		}

		if(var6 > this.depth) {
			var6 = this.depth;
		}

		if(var11 > this.height) {
			var11 = this.height;
		}

		for(var3 = var3; var3 < var4; ++var3) {
			for(int var8 = var5; var8 < var6; ++var8) {
				for(int var9 = var7; var9 < var11; ++var9) {
					Tile var10 = Tile.tiles[this.getTile(var3, var8, var9)];
					if(var10 != null && var10.getLiquidType() == var2) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public HitResult clip(Vec3 var1, Vec3 var2) {
		if(!Float.isNaN(var1.x) && !Float.isNaN(var1.y) && !Float.isNaN(var1.z)) {
			if(!Float.isNaN(var2.x) && !Float.isNaN(var2.y) && !Float.isNaN(var2.z)) {
				int var3 = (int)Math.floor((double)var2.x);
				int var4 = (int)Math.floor((double)var2.y);
				int var5 = (int)Math.floor((double)var2.z);
				int var6 = (int)Math.floor((double)var1.x);
				int var7 = (int)Math.floor((double)var1.y);
				int var8 = (int)Math.floor((double)var1.z);

				int var50 = 20;
				
				while(!Float.isNaN(var1.x) && !Float.isNaN(var1.y) && !Float.isNaN(var1.z)&& var50-- > 0) {
					if(var6 == var3 && var7 == var4 && var8 == var5) {
						return null;
					}

					float var9 = 999.0F;
					float var10 = 999.0F;
					float var11 = 999.0F;
					if(var3 > var6) {
						var9 = (float)var6 + 1.0F;
					}

					if(var3 < var6) {
						var9 = (float)var6;
					}

					if(var4 > var7) {
						var10 = (float)var7 + 1.0F;
					}

					if(var4 < var7) {
						var10 = (float)var7;
					}

					if(var5 > var8) {
						var11 = (float)var8 + 1.0F;
					}

					if(var5 < var8) {
						var11 = (float)var8;
					}

					float var12 = 999.0F;
					float var13 = 999.0F;
					float var14 = 999.0F;
					float var15 = var2.x - var1.x;
					float var16 = var2.y - var1.y;
					float var17 = var2.z - var1.z;
					if(var9 != 999.0F) {
						var12 = (var9 - var1.x) / var15;
					}

					if(var10 != 999.0F) {
						var13 = (var10 - var1.y) / var16;
					}

					if(var11 != 999.0F) {
						var14 = (var11 - var1.z) / var17;
					}

					boolean var18 = false;
					byte var20;
					if(var12 < var13 && var12 < var14) {
						if(var3 > var6) {
							var20 = 4;
						} else {
							var20 = 5;
						}

						var1.x = var9;
						var1.y += var16 * var12;
						var1.z += var17 * var12;
					} else if(var13 < var14) {
						if(var4 > var7) {
							var20 = 0;
						} else {
							var20 = 1;
						}

						var1.x += var15 * var13;
						var1.y = var10;
						var1.z += var17 * var13;
					} else {
						if(var5 > var8) {
							var20 = 2;
						} else {
							var20 = 3;
						}

						var1.x += var15 * var14;
						var1.y += var16 * var14;
						var1.z = var11;
					}

					var6 = (int)Math.floor((double)var1.x);
					if(var20 == 5) {
						--var6;
					}

					var7 = (int)Math.floor((double)var1.y);
					if(var20 == 1) {
						--var7;
					}

					var8 = (int)Math.floor((double)var1.z);
					if(var20 == 3) {
						--var8;
					}

					int var19 = this.getTile(var6, var7, var8);
					if(var19 > 0 && Tile.tiles[var19].getLiquidType() == 0) {
						return new HitResult(0, var6, var7, var8, var20);
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
