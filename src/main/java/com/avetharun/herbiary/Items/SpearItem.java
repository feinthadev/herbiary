package com.avetharun.herbiary.Items;

import com.avetharun.herbiary.Items.ItemEntities.HerbiarySpearItemEntity;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.item.Vanishable;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class SpearItem extends TridentItem implements Vanishable {

    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
    public SpearItem(Settings settings) {
        super(settings);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID,
                "Tool modifier", 4, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID,
                "Tool modifier", -3.5, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }
    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 12) {
                if (!world.isClient) {
                    int damagedState = stack.getMaxDamage() - stack.getDamage();
                    stack.damage(10, playerEntity, (p) -> {
                        p.sendToolBreakStatus(user.getActiveHand());
                    });
                    boolean returnBroken = damagedState == 10 || damagedState == 0;
                    if (returnBroken) {return;}
                    HerbiarySpearItemEntity tridentLikeEntity = new HerbiarySpearItemEntity(world, playerEntity, stack.copy());
                    tridentLikeEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 2.1F, 1.0F);
                    tridentLikeEntity.setPosition(playerEntity.getEyePos());
                    tridentLikeEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                    world.spawnEntity(tridentLikeEntity);
                    playerEntity.getInventory().removeOne(stack);

                }
            }
        }
    }
}
