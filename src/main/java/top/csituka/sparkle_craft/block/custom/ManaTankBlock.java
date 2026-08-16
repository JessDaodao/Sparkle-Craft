package top.csituka.sparkle_craft.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
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
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;

public class ManaTankBlock extends BlockWithEntity {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;

    private static final VoxelShape CORE_SHAPE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);
    private static final VoxelShape NORTH_CONNECTION_SHAPE =
            Block.createCuboidShape(4, 4, 0, 12, 12, 4);
    private static final VoxelShape EAST_CONNECTION_SHAPE =
            Block.createCuboidShape(12, 4, 4, 16, 12, 12);
    private static final VoxelShape SOUTH_CONNECTION_SHAPE =
            Block.createCuboidShape(4, 4, 12, 12, 12, 16);
    private static final VoxelShape WEST_CONNECTION_SHAPE =
            Block.createCuboidShape(0, 4, 4, 4, 12, 12);

    public ManaTankBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                state = state.with(getConnectionProperty(direction),
                        isConnectedToPipe(world, pos, direction));
            }
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
                                                 BlockState neighborState, WorldAccess world,
                                                 BlockPos pos, BlockPos neighborPos) {
        if (!direction.getAxis().isHorizontal()) {
            return state;
        }
        return state.with(getConnectionProperty(direction), neighborState.isOf(ModBlocks.MANA_PIPE));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ManaTankBlockEntity(pos, state);
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

    public static BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("Mana tanks only support horizontal connections");
        };
    }

    private static boolean isConnectedToPipe(WorldAccess world, BlockPos pos, Direction direction) {
        return world.getBlockState(pos.offset(direction)).isOf(ModBlocks.MANA_PIPE);
    }

    private static VoxelShape getShape(BlockState state) {
        VoxelShape shape = CORE_SHAPE;
        if (state.get(NORTH)) {
            shape = VoxelShapes.union(shape, NORTH_CONNECTION_SHAPE);
        }
        if (state.get(EAST)) {
            shape = VoxelShapes.union(shape, EAST_CONNECTION_SHAPE);
        }
        if (state.get(SOUTH)) {
            shape = VoxelShapes.union(shape, SOUTH_CONNECTION_SHAPE);
        }
        if (state.get(WEST)) {
            shape = VoxelShapes.union(shape, WEST_CONNECTION_SHAPE);
        }
        return shape;
    }
}
