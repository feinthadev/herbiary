package com.avetharun.herbiary.block;

import com.avetharun.herbiary.hUtil.Season;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

// Summer-fall preferred tree mounted mushroom
public class FloorMountedMushroom extends Block {
    public FloorMountedMushroom(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(0.125, 0, 0.125, 0.875, 0.3125, 0.875);
    }
}
