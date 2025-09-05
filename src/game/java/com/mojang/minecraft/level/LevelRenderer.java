package com.mojang.minecraft.level;

import com.mojang.minecraft.HitResult;
import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;
import com.mojang.util.GLAllocation;

import net.lax1dude.eaglercraft.internal.buffer.IntBuffer;

import java.util.Arrays;
import org.lwjgl.opengl.GL11;

public final class LevelRenderer {
	public Level level;
	public Chunk[] chunks;
	private Chunk[] sortedChunks;
	private int xChunks;
	private int yChunks;
	private int zChunks;
	private Textures textures;
	public int surroundLists;
	public int drawDistance = 0;
	private IntBuffer dummyBuffer = GLAllocation.createIntBuffer(1024);
	private float lX = 0.0F;
	private float lY = 0.0F;
	private float lZ = 0.0F;

	public LevelRenderer(Level var1, Textures var2) {
		this.level = var1;
		this.textures = var2;
		var1.levelListeners.add(this);
		this.surroundLists = GL11.glGenLists(2);
		this.allChanged();
	}

	public final void allChanged() {
		this.lX = -900000.0F;
		this.lY = -900000.0F;
		this.lZ = -900000.0F;
		int var1;
		if(this.chunks != null) {
			for(var1 = 0; var1 < this.chunks.length; ++var1) {
				this.chunks[var1].reset2();
			}
		}

		this.xChunks = (this.level.width + 16 - 1) / 16;
		this.yChunks = (this.level.depth + 16 - 1) / 16;
		this.zChunks = (this.level.height + 16 - 1) / 16;
		this.chunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
		this.sortedChunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];

		int var4;
		int var5;
		int var6;
		int var7;
		for(var1 = 0; var1 < this.xChunks; ++var1) {
			for(int var2 = 0; var2 < this.yChunks; ++var2) {
				for(int var3 = 0; var3 < this.zChunks; ++var3) {
					var4 = var1 << 4;
					var5 = var2 << 4;
					var6 = var3 << 4;
					var7 = var1 + 1 << 4;
					int var8 = var2 + 1 << 4;
					int var9 = var3 + 1 << 4;
					if(var7 > this.level.width) {
						var7 = this.level.width;
					}

					if(var8 > this.level.depth) {
						var8 = this.level.depth;
					}

					if(var9 > this.level.height) {
						var9 = this.level.height;
					}

					this.chunks[(var1 + var2 * this.xChunks) * this.zChunks + var3] = new Chunk(this.level, var4, var5, var6, var7, var8, var9);
					this.sortedChunks[(var1 + var2 * this.xChunks) * this.zChunks + var3] = this.chunks[(var1 + var2 * this.xChunks) * this.zChunks + var3];
				}
			}
		}

		for(var1 = 0; var1 < this.chunks.length; ++var1) {
			this.chunks[var1].reset();
		}

	}
	public void compileSurroundingGround() {
		int var4;
		int var5;
		int var6;
		int var7;
		LevelRenderer var10 = this;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.loadTexture("/rock.png", GL11.GL_NEAREST));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Tesselator var11 = Tesselator.instance;
		float var13 = 32.0F - 2.0F;
		var4 = 128;
		if(128 > this.level.width) {
			var4 = this.level.width;
		}

		if(var4 > this.level.height) {
			var4 = this.level.height;
		}

		var5 = 2048 / var4;
		var11.begin();

		float var15;
		for(var6 = -var4 * var5; var6 < var10.level.width + var4 * var5; var6 += var4) {
			for(var7 = -var4 * var5; var7 < var10.level.height + var4 * var5; var7 += var4) {
				var15 = var13;
				if(var6 >= 0 && var7 >= 0 && var6 < var10.level.width && var7 < var10.level.height) {
					var15 = 0.0F;
				}

				var11.vertexUV((float)var6, var15, (float)(var7 + var4), 0.0F, (float)var4);
				var11.vertexUV((float)(var6 + var4), var15, (float)(var7 + var4), (float)var4, (float)var4);
				var11.vertexUV((float)(var6 + var4), var15, (float)var7, (float)var4, 0.0F);
				var11.vertexUV((float)var6, var15, (float)var7, 0.0F, 0.0F);
			}
		}

		var11.end();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, var10.textures.loadTexture("/rock.png", GL11.GL_NEAREST));
		GL11.glColor3f(0.8F, 0.8F, 0.8F);
		var11.begin();

		for(var6 = 0; var6 < var10.level.width; var6 += var4) {
			var11.vertexUV((float)var6, 0.0F, 0.0F, 0.0F, 0.0F);
			var11.vertexUV((float)(var6 + var4), 0.0F, 0.0F, (float)var4, 0.0F);
			var11.vertexUV((float)(var6 + var4), var13, 0.0F, (float)var4, var13);
			var11.vertexUV((float)var6, var13, 0.0F, 0.0F, var13);
			var11.vertexUV((float)var6, var13, (float)var10.level.height, 0.0F, var13);
			var11.vertexUV((float)(var6 + var4), var13, (float)var10.level.height, (float)var4, var13);
			var11.vertexUV((float)(var6 + var4), 0.0F, (float)var10.level.height, (float)var4, 0.0F);
			var11.vertexUV((float)var6, 0.0F, (float)var10.level.height, 0.0F, 0.0F);
		}

		GL11.glColor3f(0.6F, 0.6F, 0.6F);

		for(var6 = 0; var6 < var10.level.height; var6 += var4) {
			var11.vertexUV(0.0F, var13, (float)var6, 0.0F, 0.0F);
			var11.vertexUV(0.0F, var13, (float)(var6 + var4), (float)var4, 0.0F);
			var11.vertexUV(0.0F, 0.0F, (float)(var6 + var4), (float)var4, var13);
			var11.vertexUV(0.0F, 0.0F, (float)var6, 0.0F, var13);
			var11.vertexUV((float)var10.level.width, 0.0F, (float)var6, 0.0F, var13);
			var11.vertexUV((float)var10.level.width, 0.0F, (float)(var6 + var4), (float)var4, var13);
			var11.vertexUV((float)var10.level.width, var13, (float)(var6 + var4), (float)var4, 0.0F);
			var11.vertexUV((float)var10.level.width, var13, (float)var6, 0.0F, 0.0F);
		}

		var11.end();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	public void compileSurroundingWater() {
		int var4;
		int var5;
		int var6;
		int var7;
		float var15;
		LevelRenderer var10 = this;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.loadTexture("/water.png", GL11.GL_NEAREST));
		float var12 = 32.0F;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tesselator var14 = Tesselator.instance;
		var4 = 128;
		if(128 > this.level.width) {
			var4 = this.level.width;
		}

		if(var4 > this.level.height) {
			var4 = this.level.height;
		}

		var5 = 2048 / var4;
		var14.begin();

		for(var6 = -var4 * var5; var6 < var10.level.width + var4 * var5; var6 += var4) {
			for(var7 = -var4 * var5; var7 < var10.level.height + var4 * var5; var7 += var4) {
				var15 = var12 - 0.1F;
				if(var6 < 0 || var7 < 0 || var6 >= var10.level.width || var7 >= var10.level.height) {
					var14.vertexUV((float)var6, var15, (float)(var7 + var4), 0.0F, (float)var4);
					var14.vertexUV((float)(var6 + var4), var15, (float)(var7 + var4), (float)var4, (float)var4);
					var14.vertexUV((float)(var6 + var4), var15, (float)var7, (float)var4, 0.0F);
					var14.vertexUV((float)var6, var15, (float)var7, 0.0F, 0.0F);
					var14.vertexUV((float)var6, var15, (float)var7, 0.0F, 0.0F);
					var14.vertexUV((float)(var6 + var4), var15, (float)var7, (float)var4, 0.0F);
					var14.vertexUV((float)(var6 + var4), var15, (float)(var7 + var4), (float)var4, (float)var4);
					var14.vertexUV((float)var6, var15, (float)(var7 + var4), 0.0F, (float)var4);
				}
			}
		}

		var14.end();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	public final void render(Player var1, int var2) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.loadTexture("/terrain.png", GL11.GL_NEAREST));
		float var3 = var1.x - this.lX;
		float var4 = var1.y - this.lY;
		float var5 = var1.z - this.lZ;
		if(var3 * var3 + var4 * var4 + var5 * var5 > 64.0F) {
			this.lX = var1.x;
			this.lY = var1.y;
			this.lZ = var1.z;
			Arrays.sort(this.sortedChunks, new DistanceSorter(var1));
		}

		this.dummyBuffer.clear();

		for(int var6 = 0; var6 < this.sortedChunks.length; ++var6) {
			if(this.sortedChunks[var6].visible && !this.sortedChunks[var6].canRender) {
				var4 = (float)(256 / (1 << this.drawDistance));
				if(this.drawDistance == 0 || this.sortedChunks[var6].compare(var1) < var4 * var4) {
					int var7 = this.sortedChunks[var6].render(var2);
					this.dummyBuffer.put(var7);
					if(this.dummyBuffer.remaining() == 0) {
						this.dummyBuffer.flip();
						GL11.glCallLists(this.dummyBuffer);
						this.dummyBuffer.clear();
					}
				}
			}
		}

		if(this.dummyBuffer.position() > 0) {
			this.dummyBuffer.flip();
			GL11.glCallLists(this.dummyBuffer);
		}

		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public final void renderHit(Player var1, HitResult var2, int var3, int var4) {
		Tesselator var5 = Tesselator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, ((float)Math.sin((double)System.currentTimeMillis() / 100.0D) * 0.2F + 0.4F) * 0.5F);
		if(var3 == 0) {
			var5.begin();

			for(var3 = 0; var3 < 6; ++var3) {
				Tile.renderFaceNoTexture(var1, var5, var2.x, var2.y, var2.z, var3);
			}

			var5.end();
		} else {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			float var8 = (float)Math.sin((double)System.currentTimeMillis() / 100.0D) * 0.2F + 0.8F;
			GL11.glColor4f(var8, var8, var8, (float)Math.sin((double)System.currentTimeMillis() / 200.0D) * 0.2F + 0.5F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			int var7 = this.textures.loadTexture("/terrain.png", 9728);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, var7);
			var7 = var2.x;
			var3 = var2.y;
			int var6 = var2.z;
			if(var2.f == 0) {
				--var3;
			}

			if(var2.f == 1) {
				++var3;
			}

			if(var2.f == 2) {
				--var6;
			}

			if(var2.f == 3) {
				++var6;
			}

			if(var2.f == 4) {
				--var7;
			}

			if(var2.f == 5) {
				++var7;
			}

			var5.begin();
			var5.noColor();
			Tile.tiles[var4].render(var5, this.level, 0, var7, var3, var6);
			Tile.tiles[var4].render(var5, this.level, 1, var7, var3, var6);
			var5.end();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}

	public static void renderHitOutline(HitResult var0, int var1) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		float var2 = (float)var0.x;
		float var3 = (float)var0.y;
		float var4 = (float)var0.z;
		if(var1 == 1) {
			if(var0.f == 0) {
				--var3;
			}

			if(var0.f == 1) {
				++var3;
			}

			if(var0.f == 2) {
				--var4;
			}

			if(var0.f == 3) {
				++var4;
			}

			if(var0.f == 4) {
				--var2;
			}

			if(var0.f == 5) {
				++var2;
			}
		}

		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex3f(var2, var3, var4);
		GL11.glVertex3f(var2 + 1.0F, var3, var4);
		GL11.glVertex3f(var2 + 1.0F, var3, var4 + 1.0F);
		GL11.glVertex3f(var2, var3, var4 + 1.0F);
		GL11.glVertex3f(var2, var3, var4);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex3f(var2, var3 + 1.0F, var4);
		GL11.glVertex3f(var2 + 1.0F, var3 + 1.0F, var4);
		GL11.glVertex3f(var2 + 1.0F, var3 + 1.0F, var4 + 1.0F);
		GL11.glVertex3f(var2, var3 + 1.0F, var4 + 1.0F);
		GL11.glVertex3f(var2, var3 + 1.0F, var4);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(var2, var3, var4);
		GL11.glVertex3f(var2, var3 + 1.0F, var4);
		GL11.glVertex3f(var2 + 1.0F, var3, var4);
		GL11.glVertex3f(var2 + 1.0F, var3 + 1.0F, var4);
		GL11.glVertex3f(var2 + 1.0F, var3, var4 + 1.0F);
		GL11.glVertex3f(var2 + 1.0F, var3 + 1.0F, var4 + 1.0F);
		GL11.glVertex3f(var2, var3, var4 + 1.0F);
		GL11.glVertex3f(var2, var3 + 1.0F, var4 + 1.0F);
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
	}

	public final void setDirty(int var1, int var2, int var3, int var4, int var5, int var6) {
		var1 /= 16;
		var4 /= 16;
		var2 /= 16;
		var5 /= 16;
		var3 /= 16;
		var6 /= 16;
		if(var1 < 0) {
			var1 = 0;
		}

		if(var2 < 0) {
			var2 = 0;
		}

		if(var3 < 0) {
			var3 = 0;
		}

		if(var4 >= this.xChunks) {
			var4 = this.xChunks - 1;
		}

		if(var5 >= this.yChunks) {
			var5 = this.yChunks - 1;
		}

		if(var6 >= this.zChunks) {
			var6 = this.zChunks - 1;
		}

		for(var1 = var1; var1 <= var4; ++var1) {
			for(int var7 = var2; var7 <= var5; ++var7) {
				for(int var8 = var3; var8 <= var6; ++var8) {
					this.chunks[(var1 + var7 * this.xChunks) * this.zChunks + var8].setDirty();
				}
			}
		}

	}
}
