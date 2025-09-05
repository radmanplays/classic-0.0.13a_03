package com.mojang.minecraft;

import com.mojang.minecraft.character.Vec3;
import com.mojang.minecraft.character.Zombie;
import com.mojang.minecraft.gui.Font;
import com.mojang.minecraft.gui.PauseScreen;
import com.mojang.minecraft.gui.Screen;
import com.mojang.minecraft.level.Chunk;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.LevelRenderer;
import com.mojang.minecraft.level.levelgen.LevelGen;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleEngine;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.renderer.Frustum;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;
import com.mojang.util.GLAllocation;
import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.EagUtils;

import com.mojang.minecraft.level.DirtyChunkSorter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPOutputStream;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import net.lax1dude.eaglercraft.internal.EnumPlatformType;
import net.lax1dude.eaglercraft.internal.buffer.FloatBuffer;
import net.lax1dude.eaglercraft.internal.buffer.IntBuffer;
import net.lax1dude.eaglercraft.internal.vfs2.VFile2;

public final class Minecraft implements Runnable {
	private boolean fullscreen = false;
	public int width;
	public int height;
	private FloatBuffer fogColor0 = GLAllocation.createFloatBuffer(4);
	private FloatBuffer fogColor1 = GLAllocation.createFloatBuffer(4);
	private Timer timer = new Timer(20.0F);
	public Level level;
	private LevelRenderer levelRenderer;
	private Player player;
	private int paintTexture = 1;
	private ParticleEngine particleEngine;
	public User user = null;
	private ArrayList entities = new ArrayList();
	private int yMouseAxis = 1;
	private Textures textures;
	public Font font;
	private int editMode = 0;
	private Screen screen = null;
	public LevelIO levelIo = new LevelIO(this);
	private LevelGen levelGen = new LevelGen(this);
	volatile boolean running = false;
	private String fpsString = "";
	private boolean mouseGrabbed = false;
	private IntBuffer viewportBuffer = GLAllocation.createIntBuffer(16);
	private IntBuffer selectBuffer = GLAllocation.createIntBuffer(2000);
	private HitResult hitResult = null;
	private FloatBuffer lb = GLAllocation.createFloatBuffer(16);
	private String title = "";
	private String text = "";
	private long prevFrameTime = System.currentTimeMillis();
	
	public Minecraft(int var2, int var3, boolean var4) {
		this.width = width;
		this.height = height;
		this.fullscreen = false;
		this.textures = new Textures();
	}
	
	public final void setScreen(Screen var1) {
		if(this.screen != null) {
			this.screen.closeScreen();
		}

		this.screen = var1;
		if(var1 != null) {
			int var2 = this.width * 240 / this.height;
			int var3 = this.height * 240 / this.height;
			var1.init(this, var2, var3);
		}

	}
	
	private static void checkGlError(String string) {
		int errorCode = GL11.glGetError();
		if(errorCode != 0) {
			String errorString = GLU.gluErrorString(errorCode);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + string);
			System.out.println(errorCode + ": " + errorString);
			throw new RuntimeException(errorCode + ": " + errorString);

		}

	}
	
	private void attemptSaveLevel() {
		try {
			LevelIO.save(this.level, new VFile2("level.dat"));
		} catch (Exception var1) {
			var1.printStackTrace();
		}
	}

	public final void destroy() {
		this.attemptSaveLevel();
		EagRuntime.destroy();
	}

	public final void run() {
		this.running = true;
		try {
			Minecraft var4 = this;
			float var8 = 0.5F;
			float var9 = 0.8F;
			this.fogColor0.put(new float[]{var8, var9, 1.0F, 1.0F});
			this.fogColor0.flip();
			this.fogColor1.put(new float[]{(float)14 / 255.0F, (float)11 / 255.0F, (float)10 / 255.0F, 1.0F});
			this.fogColor1.flip();
			if(this.fullscreen) {
				Display.toggleFullscreen();
				this.width = Display.getWidth();
				this.height = Display.getHeight();
			} else {
				this.width = Display.getWidth();
				this.height = Display.getHeight();
			}
			
			Display.setTitle("Minecraft 0.0.13a_03");

			Display.create();
			Keyboard.create();
			Mouse.create();
			checkGlError("Pre startup");
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glClearColor(var8, var9, 1.0F, 0.0F);
			GL11.glClearDepth(1.0D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			GL11.glCullFace(GL11.GL_BACK);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			checkGlError("Startup");
			this.font = new Font("/default.gif", this.textures);
			IntBuffer var1 = GLAllocation.createIntBuffer(256);
			var1.clear().limit(256);
			GL11.glViewport(0, 0, this.width, this.height);
			this.level = new Level();
			boolean var2 = false;

			try {
				var2 = var4.levelIo.load(var4.level, new VFile2("level.dat"));
				if(!var2) {
					var2 = var4.levelIo.loadLegacy(var4.level, new VFile2("level.dat"));
				}
			} catch (Exception var19) {
				var2 = false;
			}

			if(!var2) {
				String var26 = this.user != null ? this.user.name : "anonymous";
				this.levelGen.generateLevel(this.level, var26, 256, 256, 64);
			}
			this.levelRenderer = new LevelRenderer(this.level, this.textures);
			this.player = new Player(this.level);
			this.particleEngine = new ParticleEngine(this.level, this.textures);
		} catch (Exception var9) {
			var9.printStackTrace();
			System.out.println("Failed to start Minecraft");
			destroy();
		}
		
		long var25 = System.currentTimeMillis();
		int var3 = 0;

		try {
			while(this.running) {
					if(Display.isCloseRequested()) {
						this.running = false;
					}

					Timer var27 = this.timer;
					long var7 = System.nanoTime();
					long var29 = var7 - var27.lastTime;
					var27.lastTime = var7;
					if(var29 < 0L) {
						var29 = 0L;
					}

					if(var29 > 1000000000L) {
						var29 = 1000000000L;
					}

					var27.fps += (float)var29 * var27.timeScale * var27.ticksPerSecond / 1.0E9F;
					var27.ticks = (int)var27.fps;
					if(var27.ticks > 100) {
						var27.ticks = 100;
					}

					var27.fps -= (float)var27.ticks;
					var27.a = var27.fps;

					for(int var28 = 0; var28 < this.timer.ticks; ++var28) {
						this.tick();
					}

					checkGlError("Pre render");
					this.render(this.timer.a);
					checkGlError("Post render");
					++var3;

					while(System.currentTimeMillis() >= var25 + 1000L) {
						this.fpsString = var3 + " fps, " + Chunk.updates + " chunk updates";
						Chunk.updates = 0;
						var25 += 1000L;
						var3 = 0;
					}
			}

			return;
		} catch (Exception var22) {
			var22.printStackTrace();
		} finally {
			this.destroy();
		}

	}

	public final void grabMouse() {
		if(!this.mouseGrabbed) {
			this.mouseGrabbed = true;
			Mouse.setGrabbed(true);
			this.setScreen((Screen)null);
		}
	}
	
	private void releaseMouse() {
		if(this.mouseGrabbed) {
			Player var1 = this.player;

			for(int var2 = 0; var2 < 10; ++var2) {
				var1.keys[var2] = false;
			}
			this.mouseGrabbed = false;
			Mouse.setGrabbed(false);
			this.setScreen(new PauseScreen());
		}
	}
	
	private int saveCountdown = 600;

	private void levelSave() {
	    if (level == null) return;

	    saveCountdown--;
	    if (saveCountdown <= 0) {
	        this.attemptSaveLevel();
	        saveCountdown = 600;
	    }
	}
	
	private void tick() {
		int var4;
		int var12;
		int var13;
		int var14;
		if(this.screen == null) {
			label223:
			while(true) {
				if(Mouse.isMouseGrabbed() || Mouse.isActuallyGrabbed()) {
					this.mouseGrabbed = true;
				}
				boolean var3;
				while(Mouse.next()) {
					if(!this.mouseGrabbed && Mouse.getEventButtonState()) {
						this.grabMouse();
					} else {
						if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
							if(this.editMode == 0) {
								if(this.hitResult != null) {
									Tile var2 = Tile.tiles[this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z)];
									var3 = this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
									if(var2 != null && var3) {
										var2.destroy(this.level, this.hitResult.x, this.hitResult.y, this.hitResult.z, this.particleEngine);
									}
								}
							} else if(this.hitResult != null) {
								label230: {
									var12 = this.hitResult.x;
									var13 = this.hitResult.y;
									var4 = this.hitResult.z;
									if(this.hitResult.f == 0) {
										--var13;
									}

									if(this.hitResult.f == 1) {
										++var13;
									}

									if(this.hitResult.f == 2) {
										--var4;
									}

									if(this.hitResult.f == 3) {
										++var4;
									}

									if(this.hitResult.f == 4) {
										--var12;
									}

									if(this.hitResult.f == 5) {
										++var12;
									}

									AABB var5 = Tile.tiles[this.paintTexture].getAABB(var12, var13, var4);
									if(var5 != null) {
										AABB var7 = var5;
										Minecraft var6 = this;
										boolean var10000;
										if(this.player.bb.intersects(var5)) {
											var10000 = false;
										} else {
											var14 = 0;

											while(true) {
												if(var14 >= var6.entities.size()) {
													var10000 = true;
													break;
												}

												if(((Entity)var6.entities.get(var14)).bb.intersects(var7)) {
													var10000 = false;
													break;
												}

												++var14;
											}
										}

										if(!var10000) {
											break label230;
										}
									}

									this.level.setTile(var12, var13, var4, this.paintTexture);
								}
							}
						}

						if(Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
							this.editMode = (this.editMode + 1) % 2;
						}
					}
				}

				while(true) {
					if(!Keyboard.next()) {
						break label223;
					}

					Player var19 = this.player;
					int var10001 = Keyboard.getEventKey();
					var3 = Keyboard.getEventKeyState();
					var12 = var10001;
					Player var1 = var19;
					byte var15 = -1;
					if(var12 == 200 || var12 == 17) {
						var15 = 0;
					}

					if(var12 == 208 || var12 == 31) {
						var15 = 1;
					}

					if(var12 == 203 || var12 == 30) {
						var15 = 2;
					}

					if(var12 == 205 || var12 == 32) {
						var15 = 3;
					}

					if(var12 == 57 || var12 == 219) {
						var15 = 4;
					}

					if(var15 >= 0) {
						var1.keys[var15] = var3;
					}

					if(Keyboard.getEventKeyState()) {
						if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
							this.releaseMouse();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
							this.attemptSaveLevel();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_R) {
							this.player.resetPos();
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_1) {
							this.paintTexture = 1;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_2) {
							this.paintTexture = 3;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_3) {
							this.paintTexture = 4;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_4) {
							this.paintTexture = 5;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_6) {
							this.paintTexture = 6;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_Y) {
							this.yMouseAxis = -this.yMouseAxis;
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_G) {
							this.entities.add(new Zombie(this.level, this.textures, this.player.x, this.player.y, this.player.z));
						}

						if(Keyboard.getEventKey() == Keyboard.KEY_F) {
							LevelRenderer var8 = this.levelRenderer;
							var8.drawDistance = (var8.drawDistance + 1) % 4;
						}
					}
				}
			}
		}

		if(this.screen != null) {
			this.screen.updateEvents();
			if(this.screen != null) {
				this.screen.tick();
			}
		}

		Level var9 = this.level;
		var9.unprocessed += var9.width * var9.height * var9.depth;
		var12 = var9.unprocessed / 200;
		var9.unprocessed -= var12 * 200;

		for(var13 = 0; var13 < var12; ++var13) {
			var9.randValue = var9.randValue * 1664525 + 1013904223;
			var4 = var9.randValue >> 16 & var9.width - 1;
			var9.randValue = var9.randValue * 1664525 + 1013904223;
			var14 = var9.randValue >> 16 & var9.depth - 1;
			var9.randValue = var9.randValue * 1664525 + 1013904223;
			int var17 = var9.randValue >> 16 & var9.height - 1;
			byte var18 = var9.blocks[(var14 * var9.height + var17) * var9.width + var4];
			if(Tile.shouldTick[var18]) {
				Tile.tiles[var18].tick(var9, var4, var14, var17, var9.random);
			}
		}

		ParticleEngine var10 = this.particleEngine;

		for(var12 = 0; var12 < var10.particles.size(); ++var12) {
			Particle var16 = (Particle)var10.particles.get(var12);
			var16.tick();
			if(var16.removed) {
				var10.particles.remove(var12--);
			}
		}

		for(int var11 = 0; var11 < this.entities.size(); ++var11) {
			((Entity)this.entities.get(var11)).tick();
			if(((Entity)this.entities.get(var11)).removed) {
				this.entities.remove(var11--);
			}
		}

		this.player.tick();
		levelSave();
	}

	private void orientCamera(float var1) {
		GL11.glTranslatef(0.0F, 0.0F, -0.3F);
		GL11.glRotatef(this.player.xRot, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(this.player.yRot, 0.0F, 1.0F, 0.0F);
		float var2 = this.player.xo + (this.player.x - this.player.xo) * var1;
		float var3 = this.player.yo + (this.player.y - this.player.yo) * var1;
		float var4 = this.player.zo + (this.player.z - this.player.zo) * var1;
		GL11.glTranslatef(-var2, -var3, -var4);
	}

	private void render(float var1) {
		if(!Display.isActive()) {
			this.releaseMouse();
		}
		if (Display.wasResized()) {
			this.width = Display.getWidth();
			this.height = Display.getHeight();
			
			if(this.screen != null) {
				Screen sc = this.screen;
				this.setScreen((Screen)null);
				this.setScreen(sc);
			}
		}
		GL11.glViewport(0, 0, this.width, this.height);
		float var5;
		if(this.mouseGrabbed) {
			float var2 = 0.0F;
			float var3 = 0.0F;
			var2 = (float)Mouse.getDX();
			var3 = (float)Mouse.getDY();
			var5 = var3 * (float)this.yMouseAxis;
			Player var19 = this.player;
			var19.yRot = (float)((double)var19.yRot + (double)var2 * 0.15D);
			var19.xRot = (float)((double)var19.xRot - (double)var5 * 0.15D);
			if(var19.xRot < -90.0F) {
				var19.xRot = -90.0F;
			}

			if(var19.xRot > 90.0F) {
				var19.xRot = 90.0F;
			}
		}

		this.checkGlError("Set viewport");
		float pitch = this.player.xRot;
		float yaw = this.player.yRot;

		double px = this.player.x;
		double py = this.player.y;
		double pz = this.player.z;

		Vec3 cameraPos = new Vec3((float)px, (float)py, (float)pz);

		float cosYaw = (float)Math.cos(-Math.toRadians(yaw) - Math.PI);
		float sinYaw = (float)Math.sin(-Math.toRadians(yaw) - Math.PI);
		float cosPitch = (float)Math.cos(-Math.toRadians(pitch));
		float sinPitch = (float)Math.sin(-Math.toRadians(pitch));

		float dirX = sinYaw * cosPitch;
		float dirY = sinPitch;
		float dirZ = cosYaw * cosPitch;
		float reachDistance = 3.0F;
		if (pitch > 60.0F) {
		    reachDistance += 1.0F;
		}
		if (pitch >= 55.0F && pitch <= 60.0F) {
		    reachDistance += 2.0F;
		}
		Vec3 reachVec = new Vec3(
		    cameraPos.x + dirX * reachDistance,
		    cameraPos.y + dirY * reachDistance,
		    cameraPos.z + dirZ * reachDistance
		);

		this.hitResult = this.level.clip(cameraPos, reachVec);
		this.checkGlError("Picked");
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, 1024.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
	    if (!Display.isActive() || !Mouse.isMouseGrabbed() || !Mouse.isActuallyGrabbed()) {
	        if (System.currentTimeMillis() - prevFrameTime > 250L) {
	            if (this.screen == null) {
	            	releaseMouse();
	            }
	        }
	    } else {
	        prevFrameTime = System.currentTimeMillis();
	    }
	    
		this.orientCamera(var1);
		checkGlError("Set up camera");
		GL11.glEnable(GL11.GL_CULL_FACE);
		Frustum var18 = Frustum.getFrustum();
		Frustum var22 = var18;
		LevelRenderer var23 = this.levelRenderer;

		for(int var24 = 0; var24 < var23.chunks.length; ++var24) {
			var23.chunks[var24].visible = var22.cubeInFrustum(var23.chunks[var24].aabb);
		}

		Player var4 = this.player;
		var23 = this.levelRenderer;
		LevelRenderer var31 = var23;
		ArrayList var32 = null;

		for(int var9 = 0; var9 < var31.chunks.length; ++var9) {
			Chunk var37 = var31.chunks[var9];
			if(var37.isDirty()) {
				if(var32 == null) {
					var32 = new ArrayList();
				}

				var32.add(var37);
			}
		}

		ArrayList var30 = var32;
		if(var32 != null) {
			Collections.sort(var32, new DirtyChunkSorter(var4));

			for(int var6 = 0; var6 < 4 && var6 < var30.size(); ++var6) {
				((Chunk)var30.get(var6)).rebuild();
			}
		}

		checkGlError("Update chunks");
		this.setupFog(0);
		GL11.glEnable(GL11.GL_FOG);
		this.levelRenderer.render(this.player, 0);
		checkGlError("Rendered level");

		Entity var25;
		int var26;
		for(var26 = 0; var26 < this.entities.size(); ++var26) {
			var25 = (Entity)this.entities.get(var26);
			if(var25.isLit() && var18.cubeInFrustum(var25.bb)) {
				((Entity)this.entities.get(var26)).render(var1);
			}
		}

		checkGlError("Rendered entities");
		this.particleEngine.render(this.player, var1, 0);
		checkGlError("Rendered particles");
		this.setupFog(1);
		this.levelRenderer.render(this.player, 1);

		for(var26 = 0; var26 < this.entities.size(); ++var26) {
			var25 = (Entity)this.entities.get(var26);
			if(!var25.isLit() && var18.cubeInFrustum(var25.bb)) {
				((Entity)this.entities.get(var26)).render(var1);
			}
		}

		this.particleEngine.render(this.player, var1, 1);
		this.levelRenderer.compileSurroundingGround();
		if(this.hitResult != null) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			this.levelRenderer.renderHit(this.player, this.hitResult, this.editMode, this.paintTexture);
			LevelRenderer.renderHitOutline(this.hitResult, this.editMode);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		this.setupFog(0);
		this.levelRenderer.compileSurroundingWater();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColorMask(false, false, false, false);
		this.levelRenderer.render(this.player, 2);
		GL11.glColorMask(true, true, true, true);
		this.levelRenderer.render(this.player, 2);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_FOG);
		if(this.hitResult != null) {
			GL11.glDepthFunc(GL11.GL_LESS);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
//			this.levelRenderer.renderHit(this.player, this.hitResult, this.editMode, this.paintTexture);
			LevelRenderer.renderHitOutline(this.hitResult, this.editMode);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		}
	    if (this.height == 0) {
	        return;
	    }
		int var27 = this.width * 240 / this.height;
		int var24 = this.height * 240 / this.height;
		int var6 = Mouse.getX() * var27 / this.width;
		int var7 = var24 - Mouse.getY() * var24 / this.height - 1;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)var27, (double)var24, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		checkGlError("GUI: Init");
		GL11.glPushMatrix();
		GL11.glTranslatef((float)(var27 - 16), 16.0F, -50.0F);
		Tesselator var34 = Tesselator.instance;
		GL11.glScalef(16.0F, 16.0F, 16.0F);
		GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
		GL11.glScalef(-1.0F, -1.0F, -1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int var9 = this.textures.loadTexture("/terrain.png", 9728);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, var9);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		var34.begin();
		Tile.tiles[this.paintTexture].render(var34, this.level, 0, -2, 0, 0);
		var34.end();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		checkGlError("GUI: Draw selected");
		this.font.drawShadow("0.0.13a_03", 2, 2, 16777215);
		this.font.drawShadow(this.fpsString, 2, 12, 16777215);
		checkGlError("GUI: Draw text");
		int var10 = var27 / 2;
		int var20 = var24 / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		var34.begin();
		var34.vertex((float)(var10 + 1), (float)(var20 - 4), 0.0F);
		var34.vertex((float)var10, (float)(var20 - 4), 0.0F);
		var34.vertex((float)var10, (float)(var20 + 5), 0.0F);
		var34.vertex((float)(var10 + 1), (float)(var20 + 5), 0.0F);
		var34.vertex((float)(var10 + 5), (float)var20, 0.0F);
		var34.vertex((float)(var10 - 4), (float)var20, 0.0F);
		var34.vertex((float)(var10 - 4), (float)(var20 + 1), 0.0F);
		var34.vertex((float)(var10 + 5), (float)(var20 + 1), 0.0F);
		var34.end();
		checkGlError("GUI: Draw crosshair");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if(this.screen != null) {
			this.screen.render(var6, var7);
		}

		checkGlError("Rendered gui");
		Display.update();
	}
	
	private void setupFog(int var1) {
		Tile var2 = Tile.tiles[this.level.getTile((int)this.player.x, (int)(this.player.y + 0.12F), (int)this.player.z)];
		if(var2 != null && var2.getLiquidType() == 1) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
			GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(0.02F, 0.02F, 0.2F, 1.0F));
//			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(0.3F, 0.3F, 0.5F, 1.0F));
		} else if(var2 != null && var2.getLiquidType() == 2) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
			GL11.glFog(GL11.GL_FOG_COLOR, this.getBuffer(0.6F, 0.1F, 0.0F, 1.0F));
//			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(0.4F, 0.3F, 0.3F, 1.0F));
		} else if(var1 == 0) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			GL11.glFogf(GL11.GL_FOG_DENSITY, 0.001F);
			GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor0);
//			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
		} else if(var1 == 1) {
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
			GL11.glFogf(GL11.GL_FOG_DENSITY, 0.05F);
			GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor1);
			float var3 = 0.6F;
//			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, this.getBuffer(var3, var3, var3, 1.0F));
		}

//		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
//		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	private FloatBuffer getBuffer(float a, float b, float c, float d) {
		this.lb.clear();
		this.lb.put(a).put(b).put(c).put(d);
		this.lb.flip();
		return this.lb;
	}

	public final void beginLevelLoading(String var1) {
		this.title = var1;
	    if (this.height == 0) {
	        return;
	    }
		int var3 = this.width * 240 / this.height;
		int var2 = this.height * 240 / this.height;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, (double)var3, (double)var2, 0.0D, 100.0D, 300.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
	}

	public final void levelLoadUpdate(String var1) {
		this.text = var1;
		this.setLoadingProgress(-1);
	}

	public final void setLoadingProgress(int var1) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    if (this.height == 0) {
	        return;
	    }
		int var2 = this.width * 240 / this.height;
		int var3 = this.height * 240 / this.height;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		Tesselator var4 = Tesselator.instance;
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		int var5 = this.textures.loadTexture("/dirt.png", 9728);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, var5);
		float var8 = 32.0F;
		var4.begin();
		var4.color(4210752);
		var4.vertexUV(0.0F, (float)var3, 0.0F, 0.0F, (float)var3 / var8);
		var4.vertexUV((float)var2, (float)var3, 0.0F, (float)var2 / var8, (float)var3 / var8);
		var4.vertexUV((float)var2, 0.0F, 0.0F, (float)var2 / var8, 0.0F);
		var4.vertexUV(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		var4.end();
		if(var1 >= 0) {
			var5 = var2 / 2 - 50;
			int var6 = var3 / 2 + 16;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			var4.begin();
			var4.color(8421504);
			var4.vertex((float)var5, (float)var6, 0.0F);
			var4.vertex((float)var5, (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + 100), (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + 100), (float)var6, 0.0F);
			var4.color(8454016);
			var4.vertex((float)var5, (float)var6, 0.0F);
			var4.vertex((float)var5, (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + var1), (float)(var6 + 2), 0.0F);
			var4.vertex((float)(var5 + var1), (float)var6, 0.0F);
			var4.end();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		this.font.drawShadow(this.title, (var2 - this.font.width(this.title)) / 2, var3 / 2 - 4 - 16, 16777215);
		this.font.drawShadow(this.text, (var2 - this.font.width(this.text)) / 2, var3 / 2 - 4 + 8, 16777215);
		Display.update();
	}

	public final void generateNewLevel() {
		String var1 = this.user != null ? this.user.name : "anonymous";
		this.levelGen.generateLevel(this.level, var1, 256, 256, 64);
		this.player.resetPos();

		while(0 < this.entities.size()) {
			this.entities.remove(0);
		}

	}
}
