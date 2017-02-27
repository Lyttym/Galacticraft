package micdoodle8.mods.galacticraft.core.blocks;

import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConductor;
import micdoodle8.mods.galacticraft.api.transmission.tile.INetworkConnection;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.items.IShiftDescription;
import micdoodle8.mods.galacticraft.core.tile.TileEntityAluminumWire;
import micdoodle8.mods.galacticraft.core.tile.TileEntityFluidPipe;
import micdoodle8.mods.galacticraft.core.util.CompatibilityManager;
import micdoodle8.mods.galacticraft.core.util.EnumSortCategoryBlock;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class BlockEnclosed extends Block implements IPartialSealableBlock, ITileEntityProvider, IShiftDescription, ISortableBlock
{
    public static Item[] pipeItemsBC = new Item[6];
    public static Block blockPipeBC = null;
    public static Method onBlockNeighbourChangeIC2 = null;

    public static final PropertyEnum TYPE = PropertyEnum.create("type", EnumEnclosedBlockType.class);

    public enum EnumEnclosedBlockType implements IStringSerializable
    {
//        copper(1, 1, 0.25F, 0.2D, 128),
//        glass(0, 0, 0.25F, 0.025D, 8192),
//        gold(2, 1, 0.1875F, 0.4D, 512),
//        iron(3, 1, 0.375F, 0.8D, 2048),
//        tin(1, 1, 0.25F, 0.2D, 32),
//        detector(0, 2147483647, 0.5F, 0.5D, 8192),
//        splitter(0, 2147483647, 0.5F, 0.5D, 8192);
        IC2_HV_CABLE(0, "iron", 1, "enclosed_hv_cable"),
        OXYGEN_PIPE(1, "enclosed_fluid_pipe"),
        IC2_COPPER_CABLE(2, "copper", 1, "enclosed_copper_cable"),
        IC2_GOLD_CABLE(3, "gold", 1, "enclosed_gold_cable"),
        TE_CONDUIT(4, "enclosed_te_conduit"), //CURRENTLY UNUSED
        IC2_GLASS_FIBRE_CABLE(5, "glass", 0, "enclosed_glass_fibre_cable"),
        IC2_LV_CABLE(6, "tin", 1, "enclosed_lv_cable"),
        BC_ITEM_STONEPIPE(7, "PipeItemsStone", "enclosed_pipe_items_stone"),
        BC_ITEM_COBBLESTONEPIPE(8, "PipeItemsCobblestone", "enclosed_pipe_items_cobblestone"),
        BC_FLUIDS_STONEPIPE(9, "PipeFluidsStone", "enclosed_pipe_fluids_stone"),
        BC_FLUIDS_COBBLESTONEPIPE(10, "PipeFluidsCobblestone", "enclosed_pipe_fluids_cobblestone"),
        BC_POWER_STONEPIPE(11, "PipePowerStone", "enclosed_pipe_power_stone"),
        BC_POWER_GOLDPIPE(12, "PipePowerGold", "enclosed_pipe_power_gold"),
        ME_CABLE(13, "enclosed_me_cable"),
        ALUMINUM_WIRE(14, "enclosed_aluminum_wire"),
        ALUMINUM_WIRE_HEAVY(15, "enclosed_heavy_aluminum_wire");

        private final int meta;
        private final String name;
        private final String ic2Enum;
        private final int ic2Insulation;
        private final String bcPipeType;

        EnumEnclosedBlockType(int meta, String bcPipeType, String name)
        {
            this(meta, null, -1, bcPipeType, name);
        }

        EnumEnclosedBlockType(int meta, String name)
        {
            this(meta, null, -1, null, name);
        }

        EnumEnclosedBlockType(int meta, String ic2Enum, int ic2Insulation, String name)
        {
            this(meta, ic2Enum, ic2Insulation, null, name);
        }

        EnumEnclosedBlockType(int meta, String ic2Enum, int ic2Insulation, String bcPipeType, String name)
        {
            this.meta = meta;
            this.ic2Enum = ic2Enum;
            this.ic2Insulation = ic2Insulation;
            this.bcPipeType = bcPipeType;
            this.name = name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public String getIc2Enum()
        {
            return ic2Enum;
        }

        public int getIc2Insulation()
        {
            return ic2Insulation;
        }

        public String getBCPipeType()
        {
            return bcPipeType;
        }

        public static EnumEnclosedBlockType byMetadata(int meta)
        {
            return values()[meta];
        }

        @Override
        public String getName()
        {
            return this.name;
        }
    }

    public BlockEnclosed(String assetName)
    {
        super(Material.clay);
        this.setResistance(0.2F);
        this.setHardness(0.4f);
        this.setStepSound(Block.soundTypeStone);
        this.setUnlocalizedName(assetName);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
        par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.ALUMINUM_WIRE.getMeta()));
        par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.ALUMINUM_WIRE_HEAVY.getMeta()));
        par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.OXYGEN_PIPE.getMeta()));

        if (CompatibilityManager.isTELoaded() || GCBlocks.registeringSorted)
        {
            // par3List.add(new ItemStack(par1, 1, 0));
        }

        if (CompatibilityManager.isIc2Loaded() || GCBlocks.registeringSorted)
        {
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.IC2_COPPER_CABLE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.IC2_GOLD_CABLE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.IC2_HV_CABLE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.IC2_GLASS_FIBRE_CABLE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.IC2_LV_CABLE.getMeta()));
        }

        if (CompatibilityManager.isBCraftLoaded() || GCBlocks.registeringSorted)
        {
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.BC_ITEM_COBBLESTONEPIPE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.BC_ITEM_STONEPIPE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.BC_FLUIDS_COBBLESTONEPIPE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.BC_FLUIDS_STONEPIPE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.BC_POWER_STONEPIPE.getMeta()));
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.BC_POWER_GOLDPIPE.getMeta()));
        }

        if (CompatibilityManager.isAppEngLoaded() || GCBlocks.registeringSorted)
        {
            par3List.add(new ItemStack(par1, 1, EnumEnclosedBlockType.ME_CABLE.getMeta()));
        }
    }

    public static void initialiseBC()
    {
        for (int i = 0; i < 6; i++)
        {
            try
            {
                Class<?> clazzBC = Class.forName("buildcraft.BuildCraftTransport");
                String pipeName = EnumEnclosedBlockType.values()[i + 7].getBCPipeType();
                pipeName = pipeName.substring(0, 1).toLowerCase() + pipeName.substring(1);
                pipeItemsBC[i] = (Item) clazzBC.getField(pipeName).get(null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        //Now update the cached classes in CompatibilityManager
        //This is needed for BCCompat compatibility, as that overrides the basic BlockGenericPipe
        if (pipeItemsBC[0] != null)
        {
            try
            {
                Class<?> clazzBC = Class.forName("buildcraft.BuildCraftTransport");
                blockPipeBC = (Block) clazzBC.getField("genericPipeBlock").get(null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn()
    {
        return GalacticraftCore.galacticraftBlocksTab;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getBlock().getMetaFromState(state);
    }

    @Override
    public int getDamageValue(World worldIn, BlockPos pos)
    {
        return getMetaFromState(worldIn.getBlockState(pos));
    }

    @Override
    public void onNeighborChange(IBlockAccess worldIn, BlockPos pos, BlockPos neighborBlockPos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        IBlockState neighborBlock = worldIn.getBlockState(neighborBlockPos);
        int metadata = state.getBlock().getMetaFromState(state);
        final TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (metadata == EnumEnclosedBlockType.TE_CONDUIT.getMeta())
        {
            super.onNeighborChange(worldIn, pos, neighborBlockPos);
        }
        else if (metadata == EnumEnclosedBlockType.OXYGEN_PIPE.getMeta())
        {
            super.onNeighborChange(worldIn, pos, neighborBlockPos);

            if (tileEntity instanceof INetworkConnection)
            {
                ((INetworkConnection) tileEntity).refresh();
            }
        }
        else if (metadata <= 6)
        {
            super.onNeighborChange(worldIn, pos, neighborBlockPos);
            if (CompatibilityManager.isIc2Loaded() && tileEntity != null)
            {
                try
                {
                    onBlockNeighbourChangeIC2.invoke(tileEntity, neighborBlock.getBlock());
                    return;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (metadata <= 12)
        {
            if (CompatibilityManager.isBCraftLoaded())
            {
                if (blockPipeBC != null)
                {
                    try
                    {
                        blockPipeBC.onNeighborChange(worldIn, pos, neighborBlockPos);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            super.onNeighborChange(worldIn, pos, neighborBlockPos);
        }
        else if (metadata <= EnumEnclosedBlockType.ME_CABLE.getMeta())
        {
            super.onNeighborChange(worldIn, pos, neighborBlockPos);
            if (CompatibilityManager.isAppEngLoaded())
            {
//                worldIn.markBlockForUpdate(pos); TODO
            }
        }
        else if (metadata <= EnumEnclosedBlockType.ALUMINUM_WIRE.getMeta())
        {
            super.onNeighborChange(worldIn, pos, neighborBlockPos);
            if (tileEntity instanceof IConductor)
            {
                ((IConductor) tileEntity).refresh();
            }
        }
        else if (metadata <= EnumEnclosedBlockType.ALUMINUM_WIRE_HEAVY.getMeta())
        {
            super.onNeighborChange(worldIn, pos, neighborBlockPos);
            if (tileEntity instanceof IConductor)
            {
                ((IConductor) tileEntity).refresh();
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        if (metadata == EnumEnclosedBlockType.TE_CONDUIT.getMeta())
        {
            //TODO
        }
        else if (metadata == EnumEnclosedBlockType.OXYGEN_PIPE.getMeta())
        {
            return new TileEntityFluidPipe();
        }
        else if (metadata <= 6)
        {
            if (CompatibilityManager.isIc2Loaded())
            {
                try
                {
                    Class<?> clazz = Class.forName("ic2.core.block.wiring.TileEntityCable");
                    Class<?> cableTypeClazz = Class.forName("ic2.core.block.wiring.CableType");
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    Constructor<?> constructor = null;

                    for (Constructor<?> constructor2 : constructors)
                    {
                        constructor = constructor2;

                        if (constructor.getGenericParameterTypes().length == 2)
                        {
                            break;
                        }
                    }

                    constructor.setAccessible(true);

                    Enum[] enums = (Enum[]) cableTypeClazz.getEnumConstants();
                    Enum foundEnum = null;
                    EnumEnclosedBlockType enclosedType = EnumEnclosedBlockType.byMetadata(metadata);

                    for (Enum e : enums)
                    {
                        if (e.name().equals(enclosedType.getIc2Enum()))
                        {
                            foundEnum = e;
                            break;
                        }
                    }

                    return (TileEntity) constructor.newInstance(foundEnum, enclosedType.getIc2Insulation());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (metadata <= 12)
        {
            if (CompatibilityManager.isBCraftLoaded())
            {
                try
                {
                    return blockPipeBC.createTileEntity(world, blockPipeBC.getDefaultState());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (metadata <= EnumEnclosedBlockType.ME_CABLE.getMeta())
        {
//            if (CompatibilityManager.isAppEngLoaded())
//            {
//                try
//                {
//                    Class<?> clazz = Class.forName("appeng.tile.networking.TileCableBus");
//                    return (TileEntity) clazz.newInstance();
//                }
//                catch (Exception e) { e.printStackTrace(); }
//            }
        }
        else if (metadata <= EnumEnclosedBlockType.ALUMINUM_WIRE.getMeta())
        {
            return new TileEntityAluminumWire(1);
        }
        else if (metadata <= EnumEnclosedBlockType.ALUMINUM_WIRE_HEAVY.getMeta())
        {
            return new TileEntityAluminumWire(2);
        }

        return null;
    }

    @Override
    public boolean isSealed(World world, BlockPos pos, EnumFacing direction)
    {
        return true;
    }

    @Override
    public String getShiftDescription(int meta)
    {
        return GCCoreUtil.translate(this.getUnlocalizedName() + ".description");
    }

    @Override
    public boolean showDescription(int meta)
    {
        return true;
    }

    public static void initialiseBCPipe(World world, BlockPos pos, int metadata)
    {
        try
        {
            //------
            //This section makes these three calls to initialise the TileEntity:
            //	Pipe pipe = BlockGenericPipe.createPipe(Item);
            //  tilePipe.initialize(pipe);
            //	and optionally: tilePipe.sendUpdateToClient();

            Item pipeItem = pipeItemsBC[metadata - 7];
            Class<?> clazzBlockPipe = CompatibilityManager.classBCBlockGenericPipe;
            TileEntity tilePipe = world.getTileEntity(pos);
            Class<?> clazzTilePipe = tilePipe.getClass();

            Method createPipe = null;
            for (Method m : clazzBlockPipe.getDeclaredMethods())
            {
                if (m.getName().equals("createPipe") && m.getParameterTypes().length == 1)
                {
                    createPipe = m;
                    break;
                }
            }
            if (createPipe != null)
            {
                Object pipe = createPipe.invoke(null, pipeItem);
                Method initializePipe = null;
                for (Method m : clazzTilePipe.getDeclaredMethods())
                {
                    if (m.getName().equals("initialize") && m.getParameterTypes().length == 1)
                    {
                        initializePipe = m;
                        break;
                    }
                }
                if (initializePipe != null)
                {
                    initializePipe.invoke(tilePipe, pipe);

                    //Legacy compatibility: TileGenericPipe.sendUpdateToClient() is not in recent BC versions
                    Method m = null;
                    try
                    {
                        m = clazzTilePipe.getMethod("sendUpdateToClient");
                    }
                    catch (Exception e)
                    {
                    }
                    if (m != null)
                    {
                        m.invoke(tilePipe);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumEnclosedBlockType type = EnumEnclosedBlockType.byMetadata(meta);
        return this.getDefaultState().withProperty(TYPE, type);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumEnclosedBlockType) state.getValue(TYPE)).getMeta();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE);
    }

    @Override
    public EnumSortCategoryBlock getCategory(int meta)
    {
        return EnumSortCategoryBlock.TRANSMITTER;
    }
}
