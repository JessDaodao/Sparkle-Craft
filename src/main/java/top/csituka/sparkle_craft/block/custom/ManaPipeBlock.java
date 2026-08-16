package top.csituka.sparkle_craft.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.entity.ManaPipeBlockEntity;
import top.csituka.sparkle_craft.block.entity.ModBlockEntities;

public class ManaPipeBlock extends BlockWithEntity {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final BooleanProperty HAS_MANA = BooleanProperty.of("has_mana");

    private static final VoxelShape CORE_SHAPE = Block.createCuboidShape(4, 4, 4, 12, 12, 12);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(4, 4, 0, 12, 12, 4);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(12, 4, 4, 16, 12, 12);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(4, 4, 12, 12, 12, 16);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0, 4, 4, 4, 12, 12);
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(4, 12, 4, 12, 16, 12);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(4, 0, 4, 12, 4, 12);

    public ManaPipeBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
                .with(HAS_MANA, false));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ManaPipeBlockEntity(pos, state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        for (Direction direction : Direction.values()) {
            state = state.with(getConnectionProperty(direction),
                    canConnectTo(world.getBlockState(pos.offset(direction)), direction));
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
                                                 BlockState neighborState, WorldAccess world,
                                                 BlockPos pos, BlockPos neighborPos) {
        return state.with(getConnectionProperty(direction), canConnectTo(neighborState, direction));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, HAS_MANA);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
                                      ShapeContext context) {
        return getShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos,
                                        ShapeContext context) {
        return getShape(state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        return world.isClient
                ? null
                : checkType(type, ModBlockEntities.MANA_PIPE, ManaPipeBlockEntity::tick);
    }

    public static BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private static boolean canConnectTo(BlockState neighborState, Direction directionFromPipe) {
        if (neighborState.isOf(ModBlocks.MANA_PIPE)) {
            return true;
        }
        if (neighborState.isOf(ModBlocks.FLY_BEACON)) {
            return true;
        }
        if (neighborState.isOf(ModBlocks.MANA_TANK)
                || neighborState.isOf(ModBlocks.LARGE_MANA_TANK)) {
            return directionFromPipe.getAxis().isHorizontal();
        }
        return neighborState.isOf(ModBlocks.CRYSTAL_MANA_EXTRACTOR)
                && CrystalManaExtractorBlock.hasManaOutputOn(neighborState,
                directionFromPipe.getOpposite());
    }

    private static VoxelShape getShape(BlockState state) {
        VoxelShape shape = CORE_SHAPE;
        if (state.get(NORTH)) {
            shape = VoxelShapes.union(shape, NORTH_SHAPE);
        }
        if (state.get(EAST)) {
            shape = VoxelShapes.union(shape, EAST_SHAPE);
        }
        if (state.get(SOUTH)) {
            shape = VoxelShapes.union(shape, SOUTH_SHAPE);
        }
        if (state.get(WEST)) {
            shape = VoxelShapes.union(shape, WEST_SHAPE);
        }
        if (state.get(UP)) {
            shape = VoxelShapes.union(shape, UP_SHAPE);
        }
        if (state.get(DOWN)) {
            shape = VoxelShapes.union(shape, DOWN_SHAPE);
        }
        return shape;
    }
}
