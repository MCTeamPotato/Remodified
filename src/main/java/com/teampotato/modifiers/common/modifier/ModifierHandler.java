package com.teampotato.modifiers.common.modifier;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

public class ModifierHandler {
    public static final long COMMON_SEGMENT_CURIO = (0x7a6ca76cL) << 32;
    public static final long COMMON_SEGMENT_EQUIPMENT = 0x9225d5c4fd8d434bL;
    public static final String tagName = "itemModifier";
    public static final String bookTagName = "bookModifier";

    public static boolean canHaveModifiers(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxStackSize() <= 1;
    }

    @Nullable
    public static Modifier rollModifier(ItemStack stack, Random random) {
        if (!canHaveModifiers(stack)) return null;
        if (Modifiers.curioPool.isApplicable.test(stack)) return Modifiers.curioPool.roll(random);
        if (Modifiers.toolPool.isApplicable.test(stack)) return Modifiers.toolPool.roll(random);
        if (Modifiers.bowPool.isApplicable.test(stack)) return Modifiers.bowPool.roll(random);
        if (Modifiers.shieldPool.isApplicable.test(stack)) return Modifiers.shieldPool.roll(random);
        if (Modifiers.armorPool.isApplicable.test(stack)) return Modifiers.armorPool.roll(random);
        return null;
    }

    public static void setModifier(ItemStack stack, Modifier modifier) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(tagName);
        tag.putString(tagName, modifier.name.toString());
    }

    public static boolean hasModifier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(tagName);
    }

    @Nullable
    public static Modifier getModifier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        if (!tag.contains(tagName)) return null;
        return Modifiers.MODIFIERS.get(new ResourceLocation(tag.getString(tagName)));
    }

    public static UUID getCurioUuid(String identifier, int slot, int attributeIndex) {
        // There's probably a smarter way to do deterministic UUIDs but whatever :shrug:
        long second = (((long) attributeIndex)<<32) | ((long) identifier.hashCode());
        long first = COMMON_SEGMENT_CURIO | (((long) slot) << 32);
        return new UUID(first, second);
    }

    public static void applyCurioModifier(LivingEntity entity, Modifier modifier, String slotIdentifier, int index) {
        if (modifier.type == ModifierType.HELD) {
            return;
        }
        for (int i = 0; i < modifier.modifiers.size(); i++) {
            Pair<Attribute, Modifier.AttributeModifierSupplier> entry = modifier.modifiers.get(i);
            UUID id = getCurioUuid(slotIdentifier, index, i);
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null && instance.getModifier(id) == null) {
                instance.addTransientModifier(entry.getValue().getAttributeModifier(id, "curio_modifier_"+modifier.debugName));
            }
        }
    }

    public static void removeCurioModifier(LivingEntity entity, Modifier modifier, String slotIdentifier, int index) {
        for (int i = 0; i < modifier.modifiers.size(); i++) {
            Pair<Attribute, Modifier.AttributeModifierSupplier> entry = modifier.modifiers.get(i);
            UUID id = getCurioUuid(slotIdentifier, index, i);
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null) {
                instance.removeModifier(id);
            }
        }
    }

    public static UUID getEquipmentUuid(EquipmentSlot slot, int attributeIndex) {
        long second = (((long) attributeIndex)<<32) | ((long) slot.hashCode());
        return new UUID(COMMON_SEGMENT_EQUIPMENT, second);
    }

    public static void applyEquipmentModifier(LivingEntity entity, @NotNull Modifier modifier, EquipmentSlot type) {
        if (modifier.type == ModifierType.HELD && type.getType() == EquipmentSlot.Type.ARMOR || modifier.type == ModifierType.EQUIPPED && type.getType() == EquipmentSlot.Type.HAND) {
            return;
        }
        for (int i = 0; i < modifier.modifiers.size(); i++) {
            Pair<Attribute, Modifier.AttributeModifierSupplier> entry = modifier.modifiers.get(i);
            UUID id = getEquipmentUuid(type, i);
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null && instance.getModifier(id) == null) {
                instance.addTransientModifier(entry.getValue().getAttributeModifier(id, "equipment_modifier_" + modifier.debugName));
            }
        }
    }

    public static void removeEquipmentModifier(LivingEntity entity, Modifier modifier, EquipmentSlot type) {
        for (int i = 0; i < modifier.modifiers.size(); i++) {
            Pair<Attribute, Modifier.AttributeModifierSupplier> entry = modifier.modifiers.get(i);
            UUID id = getEquipmentUuid(type, i);
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null) {
                instance.removeModifier(id);
            }
        }
    }
}

