package com.mojang.minecraft.level.levelgen;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.levelgen.synth.Distort;
import com.mojang.minecraft.level.levelgen.synth.PerlinNoise;
import com.mojang.minecraft.level.tile.Tile;
import java.util.ArrayList;
import java.util.Random;

public final class LevelGen {
	private Minecraft minecraft;
	private int width;
	private int height;
	private int depth;
	private Random random = new Random();
	private byte[] blocks;
	private int[] coords = new int[1048576];

	public LevelGen(Minecraft var1) {
		this.minecraft = var1;
	}

	public final boolean generateLevel(Level var1, String var2, int var3, int var4, int var5) {
		this.minecraft.beginLevelLoading("Generating level");
		this.width = 256;
		this.height = 256;
		this.depth = 64;
		this.blocks = new byte[256 << 8 << 6];
		this.minecraft.levelLoadUpdate("Raising..");
		LevelGen var27 = this;
		Distort var8 = new Distort(new PerlinNoise(this.random, 8), new PerlinNoise(this.random, 8));
		Distort var9 = new Distort(new PerlinNoise(this.random, 8), new PerlinNoise(this.random, 8));
		PerlinNoise var10 = new PerlinNoise(this.random, 8);
		int[] var11 = new int[this.width * this.height];

		int var13;
		for(var5 = 0; var5 < var27.width; ++var5) {
			var27.setNextPhase(var5 * 100 / (var27.width - 1));

			for(var13 = 0; var13 < var27.height; ++var13) {
				double var14 = var8.getValue((double)var5, (double)var13) / 8.0D - 8.0D;
				double var16 = var9.getValue((double)var5, (double)var13) / 8.0D + 8.0D;
				double var18 = var10.getValue((double)var5, (double)var13) / 8.0D;
				if(var18 > 2.0D) {
					var16 = var14;
				}

				double var20 = Math.max(var14, var16);
				var20 = (var20 * var20 * var20 / 100.0D + var20 * 3.0D) / 8.0D;
				var11[var5 + var13 * var27.width] = (int)var20;
			}
		}

		this.minecraft.levelLoadUpdate("Eroding..");
		int[] var28 = var11;
		var27 = this;
		var9 = new Distort(new PerlinNoise(this.random, 8), new PerlinNoise(this.random, 8));
		Distort var32 = new Distort(new PerlinNoise(this.random, 8), new PerlinNoise(this.random, 8));

		int var15;
		int var34;
		int var41;
		for(var34 = 0; var34 < var27.width; ++var34) {
			var27.setNextPhase(var34 * 100 / (var27.width - 1));

			for(var5 = 0; var5 < var27.height; ++var5) {
				double var36 = var9.getValue((double)(var34 << 1), (double)(var5 << 1)) / 8.0D;
				var15 = var32.getValue((double)(var34 << 1), (double)(var5 << 1)) > 0.0D ? 1 : 0;
				if(var36 > 2.0D) {
					var41 = var28[var34 + var5 * var27.width];
					var41 = ((var41 - var15) / 2 << 1) + var15;
					var28[var34 + var5 * var27.width] = var41;
				}
			}
		}

		this.minecraft.levelLoadUpdate("Soiling..");
		var28 = var11;
		var27 = this;
		int var31 = this.width;
		int var33 = this.height;
		var34 = this.depth;

		int var17;
		int var37;
		int var44;
		for(var5 = 0; var5 < var31; ++var5) {
			var27.setNextPhase(var5 * 100 / (var27.width - 1));

			for(var13 = 0; var13 < var34; ++var13) {
				for(var37 = 0; var37 < var33; ++var37) {
					var15 = (var13 * var27.height + var37) * var27.width + var5;
					var41 = var28[var5 + var37 * var31] + var34 / 2;
					var17 = var41 - 2;
					var44 = 0;
					if(var13 == var41 && var13 >= var34 / 2 - 1) {
						var44 = Tile.grass.id;
					} else if(var13 <= var41) {
						var44 = Tile.dirt.id;
					}

					if(var13 <= var17) {
						var44 = Tile.rock.id;
					}

					var27.blocks[var15] = (byte)var44;
				}
			}
		}

		this.minecraft.levelLoadUpdate("Carving..");
		var27 = this;
		int var29 = this.width;
		var31 = this.height;
		var33 = this.depth;
		var34 = var29 * var31 * var33 / 256 / 64;

		for(var5 = 0; var5 < var34; ++var5) {
			var27.setNextPhase(var5 * 100 / (var34 - 1));
			float var38 = var27.random.nextFloat() * (float)var29;
			float var39 = var27.random.nextFloat() * (float)var33;
			float var40 = var27.random.nextFloat() * (float)var31;
			var41 = (int)(var27.random.nextFloat() + var27.random.nextFloat() * 150.0F);
			float var43 = (float)((double)var27.random.nextFloat() * Math.PI * 2.0D);
			float var45 = 0.0F;
			float var19 = (float)((double)var27.random.nextFloat() * Math.PI * 2.0D);
			float var46 = 0.0F;

			for(int var21 = 0; var21 < var41; ++var21) {
				var38 = (float)((double)var38 + Math.sin((double)var43) * Math.cos((double)var19));
				var40 = (float)((double)var40 + Math.cos((double)var43) * Math.cos((double)var19));
				var39 = (float)((double)var39 + Math.sin((double)var19));
				var43 += var45 * 0.2F;
				var45 *= 0.9F;
				var45 += var27.random.nextFloat() - var27.random.nextFloat();
				var19 += var46 * 0.5F;
				var19 *= 0.5F;
				var46 *= 0.9F;
				var46 += var27.random.nextFloat() - var27.random.nextFloat();
				float var26 = (float)(Math.sin((double)var21 * Math.PI / (double)var41) * 2.5D + 1.0D);

				for(int var6 = (int)(var38 - var26); var6 <= (int)(var38 + var26); ++var6) {
					for(int var7 = (int)(var39 - var26); var7 <= (int)(var39 + var26); ++var7) {
						for(int var12 = (int)(var40 - var26); var12 <= (int)(var40 + var26); ++var12) {
							float var22 = (float)var6 - var38;
							float var23 = (float)var7 - var39;
							float var24 = (float)var12 - var40;
							var22 = var22 * var22 + var23 * var23 * 2.0F + var24 * var24;
							if(var22 < var26 * var26 && var6 >= 1 && var7 >= 1 && var12 >= 1 && var6 < var27.width - 1 && var7 < var27.depth - 1 && var12 < var27.height - 1) {
								int var47 = (var7 * var27.height + var12) * var27.width + var6;
								if(var27.blocks[var47] == Tile.rock.id) {
									var27.blocks[var47] = 0;
								}
							}
						}
					}
				}
			}
		}

		this.minecraft.levelLoadUpdate("Watering..");
		var27 = this;
		long var30 = System.nanoTime();
		long var35 = 0L;
		var13 = Tile.calmWater.id;
		this.setNextPhase(0);

		for(var37 = 0; var37 < var27.width; ++var37) {
			var35 += var27.floodFillLiquid(var37, var27.depth / 2 - 1, 0, 0, var13);
			var35 += var27.floodFillLiquid(var37, var27.depth / 2 - 1, var27.height - 1, 0, var13);
		}

		for(var37 = 0; var37 < var27.height; ++var37) {
			var35 += var27.floodFillLiquid(0, var27.depth / 2 - 1, var37, 0, var13);
			var35 += var27.floodFillLiquid(var27.width - 1, var27.depth / 2 - 1, var37, 0, var13);
		}

		var37 = var27.width * var27.height / 200;

		for(var15 = 0; var15 < var37; ++var15) {
			if(var15 % 100 == 0) {
				var27.setNextPhase(var15 * 100 / (var37 - 1));
			}

			var41 = var27.random.nextInt(var27.width);
			var17 = var27.depth / 2 - 1 - var27.random.nextInt(3);
			var44 = var27.random.nextInt(var27.height);
			if(var27.blocks[(var17 * var27.height + var44) * var27.width + var41] == 0) {
				var35 += var27.floodFillLiquid(var41, var17, var44, 0, var13);
			}
		}

		var27.setNextPhase(100);
		long var42 = System.nanoTime();
		System.out.println("Flood filled " + var35 + " tiles in " + (double)(var42 - var30) / 1000000.0D + " ms");
		this.minecraft.levelLoadUpdate("Melting..");
		this.addLava();
		var1.setData(256, 64, 256, this.blocks);
		var1.createTime = System.currentTimeMillis();
		var1.creator = var2;
		var1.name = "A Nice World";
		return true;
	}

	private void setNextPhase(int var1) {
		this.minecraft.setLoadingProgress(var1);
	}

	private void addLava() {
		int var1 = 0;
		int var2 = this.width * this.height * this.depth / 10000;

		for(int var3 = 0; var3 < var2; ++var3) {
			if(var3 % 100 == 0) {
				this.setNextPhase(var3 * 100 / (var2 - 1));
			}

			int var4 = this.random.nextInt(this.width);
			int var5 = this.random.nextInt(this.depth / 2 - 4);
			int var6 = this.random.nextInt(this.height);
			if(this.blocks[(var5 * this.height + var6) * this.width + var4] == 0) {
				++var1;
				this.floodFillLiquid(var4, var5, var6, 0, Tile.calmLava.id);
			}
		}

		this.setNextPhase(100);
		System.out.println("LavaCount: " + var1);
	}

	private long floodFillLiquid(int var1, int var2, int var3, int var4, int var5) {
		byte var20 = (byte)var5;
		ArrayList var21 = new ArrayList();
		byte var6 = 0;
		int var7 = 1;

		int var8;
		for(var8 = 1; 1 << var7 < this.width; ++var7) {
		}

		while(1 << var8 < this.height) {
			++var8;
		}

		int var9 = this.height - 1;
		int var10 = this.width - 1;
		int var22 = var6 + 1;
		this.coords[0] = ((var2 << var8) + var3 << var7) + var1;
		long var13 = 0L;
		var1 = this.width * this.height;

		while(var22 > 0) {
			--var22;
			var2 = this.coords[var22];
			if(var22 == 0 && var21.size() > 0) {
				System.out.println("IT HAPPENED!");
				this.coords = (int[])var21.remove(var21.size() - 1);
				var22 = this.coords.length;
			}

			var3 = var2 >> var7 & var9;
			int var11 = var2 >> var7 + var8;
			int var12 = var2 & var10;

			int var15;
			for(var15 = var12; var12 > 0 && this.blocks[var2 - 1] == 0; --var2) {
				--var12;
			}

			while(var15 < this.width && this.blocks[var2 + var15 - var12] == 0) {
				++var15;
			}

			int var16 = var2 >> var7 & var9;
			int var17 = var2 >> var7 + var8;
			if(var16 != var3 || var17 != var11) {
				System.out.println("hoooly fuck");
			}

			boolean var23 = false;
			boolean var24 = false;
			boolean var18 = false;
			var13 += (long)(var15 - var12);

			for(var12 = var12; var12 < var15; ++var12) {
				this.blocks[var2] = var20;
				boolean var19;
				if(var3 > 0) {
					var19 = this.blocks[var2 - this.width] == 0;
					if(var19 && !var23) {
						if(var22 == this.coords.length) {
							var21.add(this.coords);
							this.coords = new int[1048576];
							var22 = 0;
						}

						this.coords[var22++] = var2 - this.width;
					}

					var23 = var19;
				}

				if(var3 < this.height - 1) {
					var19 = this.blocks[var2 + this.width] == 0;
					if(var19 && !var24) {
						if(var22 == this.coords.length) {
							var21.add(this.coords);
							this.coords = new int[1048576];
							var22 = 0;
						}

						this.coords[var22++] = var2 + this.width;
					}

					var24 = var19;
				}

				if(var11 > 0) {
					byte var25 = this.blocks[var2 - var1];
					if((var20 == Tile.lava.id || var20 == Tile.calmLava.id) && (var25 == Tile.water.id || var25 == Tile.calmWater.id)) {
						this.blocks[var2 - var1] = (byte)Tile.rock.id;
					}

					var19 = var25 == 0;
					if(var19 && !var18) {
						if(var22 == this.coords.length) {
							var21.add(this.coords);
							this.coords = new int[1048576];
							var22 = 0;
						}

						this.coords[var22++] = var2 - var1;
					}

					var18 = var19;
				}

				++var2;
			}
		}

		return var13;
	}
}
