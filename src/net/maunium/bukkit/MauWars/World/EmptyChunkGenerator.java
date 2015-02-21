package net.maunium.bukkit.MauWars.World;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

public class EmptyChunkGenerator extends ChunkGenerator {
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, 200, 0);
	}
	
	@Override
	public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
		short[][] bb = new short[world.getMaxHeight() / 16][];
		short[] bbb = new short[4096];
		Arrays.fill(bbb, (short) 0);
		Arrays.fill(bb, bbb);
		
		for (int xx = 0; xx < 16; xx++)
			for (int zz = 0; zz < 16; zz++)
				biomes.setBiome(xx, zz, Biome.PLAINS);
		
		return bb;
	}
	
	@Override
	public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
		byte[][] bb = new byte[world.getMaxHeight() / 16][];
		byte[] bbb = new byte[4096];
		Arrays.fill(bbb, (byte) 0);
		Arrays.fill(bb, bbb);
		
		for (int xx = 0; xx < 16; xx++)
			for (int zz = 0; zz < 16; zz++)
				biomes.setBiome(xx, zz, Biome.PLAINS);
		
		return bb;
	}
	
	@Override
	public byte[] generate(World world, Random random, int x, int z) {
		byte[] bb = new byte[32768];
		Arrays.fill(bb, (byte) 0);
		return bb;
	}
}
