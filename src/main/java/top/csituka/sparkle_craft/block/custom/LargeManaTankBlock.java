package top.csituka.sparkle_craft.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.entity.LargeManaTankBlockEntity;
import top.csituka.sparkle_craft.block.entity.ModBlockEntities;

public class LargeManaTankBlock extends ManaTankBlock {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART = EnumProperty.of("part", Part.class);
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape NORTH_CONNECTION_SHAPE =
            Block.createCuboidShape(4, 4, 0, 12, 12, 4);
    private static final VoxelShape EAST_CONNECTION_SHAPE =
            Block.createCuboidShape(12, 4, 4, 16, 12, 12);
    private static final VoxelShape SOUTH_CONNECTION_SHAPE =
            Block.createCuboidShape(4, 4, 12, 12, 12, 16);
    private static final VoxelShape WEST_CONNECTION_SHAPE =
            Block.createCuboidShape(0, 4, 4, 4, 12, 12);

    public LargeManaTankBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART, Part.CONTROLLER)
                .with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) {
            return null;
        }

        Direction facing = context.getHorizontalPlayerFacing();
        BlockPos controllerPos = context.getBlockPos();
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (Part part : Part.values()) {
                if (half == DoubleBlockHalf.LOWER && part == Part.CONTROLLER) {
                    continue;
                }
                BlockPos partPos = getPartPos(controllerPos, facing, part, half);
                if (context.getWorld().isOutOfHeightLimit(partPos)
                        || !context.getWorld().getWorldBorder().contains(partPos)
                        || !context.getWorld().getBlockState(partPos).canReplace(context)) {
                    return null;
                }
            }
        }
        return state.with(FACING, facing)
                .with(PART, Part.CONTROLLER)
                .with(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient) {
            return;
        }

        Direction facing = state.get(FACING);
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (Part part : Part.values()) {
                if (half == DoubleBlockHalf.LOWER && part == Part.CONTROLLER) {
                    continue;
                }
                BlockPos partPos = getPartPos(pos, facing, part, half);
                BlockState partState = getDefaultState()
                        .with(FACING, facing)
                        .with(PART, part)
                        .with(HALF, half);
                for (Direction direction : Direction.Type.HORIZONTAL) {
                    partState = partState.with(getConnectionProperty(direction),
                            world.getBlockState(partPos.offset(direction)).isOf(ModBlocks.MANA_PIPE));
                }
                world.setBlockState(partPos, partState, Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockPos controllerPos = getControllerPos(pos, state);
            Direction facing = state.get(FACING);
            for (BlockPos partPos : getStructurePositions(controllerPos, facing)) {
                if (partPos.equals(pos)) {
                    continue;
                }
                BlockState partState = world.getBlockState(partPos);
                if (partState.isOf(ModBlocks.LARGE_MANA_TANK)
                        && getControllerPos(partPos, partState).equals(controllerPos)) {
                    world.setBlockState(partPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(getControllerPos(pos, state));
        if (blockEntity instanceof LargeManaTankBlockEntity controller) {
            player.openHandledScreen(controller);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LargeManaTankBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        return world.isClient
                ? null
                : checkType(type, ModBlockEntities.LARGE_MANA_TANK,
                LargeManaTankBlockEntity::tick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, PART, HALF);
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

    public static boolean isController(BlockState state) {
        return state.isOf(ModBlocks.LARGE_MANA_TANK)
                && state.get(PART) == Part.CONTROLLER
                && state.get(HALF) == DoubleBlockHalf.LOWER;
    }

    public static BlockPos getControllerPos(BlockPos pos, BlockState state) {
        Direction facing = state.get(FACING);
        Direction right = facing.rotateYClockwise();
        BlockPos lowerPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        return switch (state.get(PART)) {
            case CONTROLLER -> lowerPos;
            case FORWARD -> lowerPos.offset(facing.getOpposite());
            case RIGHT -> lowerPos.offset(right.getOpposite());
            case DIAGONAL -> lowerPos.offset(facing.getOpposite()).offset(right.getOpposite());
        };
    }

    public static BlockPos[] getStructurePositions(BlockPos controllerPos, Direction facing) {
        return new BlockPos[]{
                getPartPos(controllerPos, facing, Part.CONTROLLER, DoubleBlockHalf.LOWER),
                getPartPos(controllerPos, facing, Part.FORWARD, DoubleBlockHalf.LOWER),
                getPartPos(controllerPos, facing, Part.RIGHT, DoubleBlockHalf.LOWER),
                getPartPos(controllerPos, facing, Part.DIAGONAL, DoubleBlockHalf.LOWER),
                getPartPos(controllerPos, facing, Part.CONTROLLER, DoubleBlockHalf.UPPER),
                getPartPos(controllerPos, facing, Part.FORWARD, DoubleBlockHalf.UPPER),
                getPartPos(controllerPos, facing, Part.RIGHT, DoubleBlockHalf.UPPER),
                getPartPos(controllerPos, facing, Part.DIAGONAL, DoubleBlockHalf.UPPER)
        };
    }

    public static BlockPos getPartPos(BlockPos controllerPos, Direction facing, Part part,
                                      DoubleBlockHalf half) {
        BlockPos partPos = controllerPos;
        if (part.forward) {
            partPos = partPos.offset(facing);
        }
        if (part.right) {
            partPos = partPos.offset(facing.rotateYClockwise());
        }
        if (half == DoubleBlockHalf.UPPER) {
            partPos = partPos.up();
        }
        return partPos;
    }

    private static VoxelShape getShape(BlockState state) {
        BlockPos controller = BlockPos.ORIGIN;
        BlockPos partPos = getPartPos(controller, state.get(FACING), state.get(PART),
                DoubleBlockHalf.LOWER);
        BlockPos[] positions = getStructurePositions(controller, state.get(FACING));
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        double shapeMinX = partPos.getX() == minX ? 2 : 0;
        double shapeMaxX = partPos.getX() == maxX ? 14 : 16;
        double shapeMinZ = partPos.getZ() == minZ ? 2 : 0;
        double shapeMaxZ = partPos.getZ() == maxZ ? 14 : 16;
        VoxelShape shape = Block.createCuboidShape(
                shapeMinX, 0, shapeMinZ, shapeMaxX, 16, shapeMaxZ);
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

    public enum Part implements StringIdentifiable {
        CONTROLLER("controller", false, false),
        FORWARD("forward", true, false),
        RIGHT("right", false, true),
        DIAGONAL("diagonal", true, true);

        private final String name;
        private final boolean forward;
        private final boolean right;

        Part(String name, boolean forward, boolean right) {
            this.name = name;
            this.forward = forward;
            this.right = right;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
