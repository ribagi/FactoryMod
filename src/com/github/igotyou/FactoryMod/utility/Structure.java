package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.utility.Anchor.Orientation;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.Tag;

/**
 * Holds a defined three-dimensional rectangle of specified blocks
 * 
 */
public class Structure {
	
	private byte[][][] blocks;
	//Whether air blocks are a required part of the structure
	private boolean ignoreAir;
	
	public Structure(byte[][][] blocks)
	{
		this.blocks=blocks;
		this.ignoreAir=false;
	}
	/*
	 * Checks if structure exists at a given point 
	 */
	public boolean exists(Location location) {
		for(Orientation orientation:Orientation.values()) {
			if(exists(new Anchor(orientation, location))) { 
				return true;
			}
		}
		return false;
	}
	/*
	 * Checks if structure exists at a given anchor point and given
	 * orientation
	 */
	public boolean exists(Anchor anchor)
	{
		for(int x = anchor.location.getBlockX(); Math.abs(x-anchor.location.getBlockX()) < blocks.length; x+=anchor.getXModifier()) {
			for(int y = anchor.location.getBlockY(); y<anchor.location.getBlockY() + blocks[x].length; y++) {
				for(int z = anchor.location.getBlockZ(); Math.abs(z-anchor.location.getBlockZ()) < blocks[x][y].length; x+=anchor.getZModifier()) {
					//Check if this is not a index contianing air which should be ignored
					if(!(blocks[x][y][z]==0 && ignoreAir)) {
						if(!similiarBlocks(blocks[x][y][z], (new Location(anchor.location.getWorld(),x,y,z)).getBlock().getData())) {
							return false;
						}
					}
				}
			}
		}
		return true;
			
	}
	
	/*
	 * Compares two blocks and checks if they are the same
	 * Works with special cases such as burning furnaces
	 */
	
	private boolean similiarBlocks(byte block, byte otherBlock)
	{
		return block == 61 && otherBlock == 62 || block == 62 && otherBlock == 61 ||block==otherBlock;
	}
	/*
	 * Checks if a given location is contained within structure
	 */
	public boolean locationContainedInStructure(Anchor anchor, Location location) {
		return true;
	}
	
	/*
	 * Returns a set containing blocks that occur at the following offsets
	 * Should add materials of comparable blocks ie lit furnance
	 */
	public Set<Material> materialsOfOffsets(List<Offset> offsets) {
		Set<Material> materials = new HashSet();
		for(Offset offset:offsets) {
			if(validOffset(offset)) {
				if(!(blocks[offset.x][offset.y][offset.z]==0 && ignoreAir)) {
					materials.add(Material.getMaterial(blocks[offset.x][offset.y][offset.z]));
				}
			}
		}
		return materials;
	}
	/*
	 * Checks if the given offset is within the bounds of the structure
	 */
	private boolean validOffset(Offset offset) {
		return offset.x<blocks.length && offset.y<blocks[0].length && offset.z<blocks[0][0].length
			&& offset.x>=0 && offset.y>=0 && offset.z>=0;
	}
	/*
	 * Gets all material use in this schematic
	 */
	public Set<Material> getMaterials(){
		Set<Material> materials=new HashSet<Material>();
		for(short x = 0; x < blocks.length; x++) {
			for(short z = 0; z < blocks[x].length; z++) {
				for(short y = 0; y<blocks[x][z].length; y++) {
					if(!(blocks[x][y][z]==0 && ignoreAir)) {
						materials.add(Material.getMaterial((int) blocks[x][z][y]));
					}
				}
			}		
		}
		return materials;
	}

	/*
	 * Parses a Minecraft schematic file to a structure object
	 */
	
	public static Structure parseSchematic(File file)
	{
		try
		{
			NBTInputStream stream = new NBTInputStream(new FileInputStream(file));
			CompoundTag schematicTag = (CompoundTag) stream.readTag();
			Map<String, Tag> tags = schematicTag.getValue();
			short w = ((ShortTag)tags.get("Width")).getValue();
			short l = ((ShortTag)tags.get("Length")).getValue();
			short h = ((ShortTag)tags.get("Height")).getValue();
			byte[] importedBlocks = ((ByteArrayTag)tags.get("Blocks")).getValue();
			byte[][][] blocks = new byte[w][l][h];
			for(short x = 0; x < w; x++){
				for(short z = 0; z < l; z++){
					for(short y = 0; y < l; y++){
						blocks[x][y][z]=importedBlocks[y * w * l + z * w + x];
					}
				}
			}
			stream.close();
			return new Structure(blocks);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
	
	public int[] getDimensions() {
		return new int[]{blocks.length,blocks[0].length,blocks[0][0].length};
	}
}