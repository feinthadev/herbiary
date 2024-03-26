package com.avetharun.herbiary.mixin.Entity;

import com.avetharun.herbiary.Items.QuiverItem;
import com.avetharun.herbiary.ModItems;
import com.avetharun.herbiary.hUtil.iface.PlayerEntityAccessor;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityAccessor {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method="initDataTracker", at=@At("TAIL"))
    void initDTMixin(CallbackInfo ci){
    }

    @Override
    public boolean isSifting() {
        return isSifting;
    }

    @Override
    public void setSifting(boolean state) {
        isSifting = state;
    }

    @Unique
    public boolean isSifting;
    public QuiverItem.BowArrowType selectedArrowType = QuiverItem.BowArrowType.NORMAL;
    @Mixin(ServerPlayerEntity.class)
    public static abstract class ServerPlayerEntityMixin {
        public ArrayList<String> knownIDs = new ArrayList<>();
        @Inject(method="writeCustomDataToNbt", at=@At("TAIL"))
        void writeCustomData(NbtCompound nbt, CallbackInfo ci) {
            NbtList l = new NbtList();
            for (String id : knownIDs) {
                l.add(NbtString.of(id));
            }
            nbt.put("knownItemNames", l);
        }
        @Inject(method="readCustomDataFromNbt", at=@At("TAIL"))
        void readCustomData(NbtCompound nbt, CallbackInfo ci) {
            if (nbt.contains("knownItemNames")) {
                NbtList l = (NbtList) nbt.get("knownItemNames");
                assert l != null;
                for (int i = 0; i < l.size(); i++) {
                    knownIDs.add(l.getString(i));
                }
            }
        }
    }
    public boolean isLocalPlayer(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) { // Check if running on client-side
            return player == client.player;
        } else { // Running on server-side
            return false;
        }
    }
    @Unique
    public int ticksUntilZipwingAvailable = 0;
    @Inject(method="handleFallDamage", at=@At(shift= At.Shift.BEFORE, value="INVOKE", target = "Lnet/minecraft/entity/LivingEntity;handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z"), cancellable = true)
    void handleFallDamageMixin(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir){
        PlayerEntity e = (PlayerEntity) (Object)this;
        ItemStack s = e.getEquippedStack(EquipmentSlot.CHEST);
        if (s.isEmpty()) {return;}
        int enc = EnchantmentHelper.getLevel(ModItems.GRACEFUL_ENCHANTMENT, s);
        boolean bl1 = s.hasEnchantments() && enc > 0;
        if (damageSource.isOf(DamageTypes.FLY_INTO_WALL) && bl1) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    @Inject(method="<init>", at=@At("TAIL"))
    void init(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        PlayerEntity e = (PlayerEntity) (Object)this;
    }
    boolean isHot = false;
    boolean isCold = false;
    boolean isWet = false;
    boolean coolOff = false;
    int ticksSinceBegunSleeping = 0;

    @Inject(method="tick", at=@At("TAIL"))
    void tickStatuses(CallbackInfo ci) {
        PlayerEntity e = (PlayerEntity) (Object)this;
        if (e instanceof ServerPlayerEntity p) {
            if (e.isSleeping()) {
                ticksSinceBegunSleeping++;
                if (ticksSinceBegunSleeping > 100) {
                    e.wakeUp(true, true);

                    long timeOfDay = e.getWorld().getTimeOfDay();

                    long ticksUntilNextDay = 24000 - (timeOfDay % 24000);

                    long nextDayTime = timeOfDay + ticksUntilNextDay;
                    ServerWorld world = ((ServerPlayerEntity) e).getServerWorld();
                    world.setTimeOfDay(nextDayTime);
                }
            } else {ticksSinceBegunSleeping=0;}
//        if (e.isCreative()) {return;}
        }
    }
    @Shadow public abstract boolean canModifyBlocks();
    // if I can find a better method to allow breaking specific blocks via a gamerule, I will.
    // I don't think server & client get the same datapack, so unless I can make a packet of sorts, this will be the only method.
    @Inject(method="isBlockBreakingRestricted", at=@At("HEAD"), cancellable = true)
    void isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {

        // Until further notice: This will NOT be modified! Replace vanilla blocks instead!!!
//        if (gameMode.isCreative() || !world.isClient) {
//            return;
//        }
//        if (!HerbiaryClient.CanBreakVanillaBlocks && HerbiaryClient.CanMineStoneLikeBlocks && world.getBlockState(pos).isIn(BlockTags.PICKAXE_MINEABLE)) {
//            return;
//        }
//
//        else if (!HerbiaryClient.CanBreakVanillaBlocks) {
//            if (Herbiary.AllowedHerbiaryBreakables.contains(world.getBlockState(pos).getBlock())) {
//                return;
//            } else {
//                cir.setReturnValue(true);
//                cir.cancel();
//            }
//        }
    }
}
