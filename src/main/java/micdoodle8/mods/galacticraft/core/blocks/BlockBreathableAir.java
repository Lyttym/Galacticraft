package micdoodle8.mods.galacticraft.core.blocks;

import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.fluid.OxygenPressureProtocol;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBreathableAir extends BlockAir
{
    public BlockBreathableAir(String assetName)
    {
        this.setResistance(1000.0F);
        this.setHardness(0.0F);
        this.setUnlocalizedName(assetName);
    }

    @Override
    public boolean canReplace(World world, BlockPos pos, EnumFacing side, ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return true;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        final Block block = worldIn.getBlockState(pos).getBlock();
        if (block == this || block == GCBlocks.brightBreatheableAir)
        {
            return false;
        }
        else
        {
            return block instanceof BlockAir;
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess worldIn, BlockPos pos, BlockPos neighborBlockPos)
    {
        IBlockState neighborBlock = worldIn.getBlockState(neighborBlockPos);
        if (Blocks.AIR != neighborBlock && neighborBlock != GCBlocks.brightAir)
        //Do nothing if an air neighbour was replaced (probably because replacing with breatheableAir)
        //but do a check if replacing breatheableAir as that could be dividing a sealed space
        {
            OxygenPressureProtocol.onEdgeBlockUpdated((World) worldIn, pos);
        }
    }
}
