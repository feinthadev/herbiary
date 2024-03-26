package com.avetharun.herbiary.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Pickable {
    void onPick(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit);
}
