package com.mojang.minecraft.level;

import com.mojang.minecraft.Minecraft;

import net.lax1dude.eaglercraft.internal.vfs2.VFile2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class LevelIO {
	private Minecraft minecraft;

	public LevelIO(Minecraft var1) {
		this.minecraft = var1;
	}

	public final boolean load(Level var1, VFile2 var2) {
		this.minecraft.beginLevelLoading("Loading level");
		this.minecraft.levelLoadUpdate("Reading..");

		try {
			DataInputStream var11 = new DataInputStream(new GZIPInputStream(var2.getInputStream()));
			int var12 = var11.readInt();
			if(var12 != 656127880) {
				return false;
			} else {
				byte var13 = var11.readByte();
				if(var13 > 1) {
					return false;
				} else {
					String var14 = var11.readUTF();
					String var3 = var11.readUTF();
					long var8 = var11.readLong();
					short var4 = var11.readShort();
					short var5 = var11.readShort();
					short var6 = var11.readShort();
					byte[] var7 = new byte[var4 * var5 * var6];
					var11.readFully(var7);
					var11.close();
					var1.setData(var4, var6, var5, var7);
					var1.name = var14;
					var1.creator = var3;
					var1.createTime = var8;
					return true;
				}
			}
		} catch (Exception var10) {
			var10.printStackTrace();
			(new StringBuilder()).append("Failed to load level: ").append(var10.toString()).toString();
			return false;
		}
	}

	public final boolean loadLegacy(Level var1, VFile2 var2) {
		this.minecraft.beginLevelLoading("Loading level");
		this.minecraft.levelLoadUpdate("Reading..");

		try {
			DataInputStream var6 = new DataInputStream(new GZIPInputStream(var2.getInputStream()));
			String var7 = "--";
			String var3 = "unknown";
			byte[] var4 = new byte[256 << 8 << 6];
			var6.readFully(var4);
			var6.close();
			var1.setData(256, 64, 256, var4);
			var1.name = var7;
			var1.creator = var3;
			var1.createTime = 0L;
			return true;
		} catch (Exception var5) {
			var5.printStackTrace();
			(new StringBuilder()).append("Failed to load level: ").append(var5.toString()).toString();
			return false;
		}
	}

	public static void save(Level var0, VFile2 var1) {
		try {
			DataOutputStream var3 = new DataOutputStream(new GZIPOutputStream(var1.getOutputStream()));
			var3.writeInt(656127880);
			var3.writeByte(1);
			var3.writeUTF(var0.name);
			var3.writeUTF(var0.creator);
			var3.writeLong(var0.createTime);
			var3.writeShort(var0.width);
			var3.writeShort(var0.height);
			var3.writeShort(var0.depth);
			var3.write(var0.blocks);
			var3.close();
		} catch (Exception var2) {
			var2.printStackTrace();
		}
	}
}
