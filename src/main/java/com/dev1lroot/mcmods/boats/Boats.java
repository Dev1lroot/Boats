/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats;

import com.dev1lroot.mcmods.boats.init.ModEntities;
import com.dev1lroot.mcmods.boats.init.ModItems;
import com.dev1lroot.mcmods.boats.init.ModMenuTypes;
import net.minecraft.core.UUIDUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.UUID;

@Mod(Boats.MODID)
public class Boats {

    public static final String MODID = "boats";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    // Stores the UUID of the BedBoatEntity the player last slept in or right-clicked.
    // copyOnDeath() ensures this persists through death so the event handler can always find the boat by UUID.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<UUID>>> BED_BOAT_UUID =
        ATTACHMENT_TYPES.register("bed_boat_uuid", () ->
            AttachmentType.<Optional<UUID>>builder(() -> Optional.empty())
                .serialize(UUIDUtil.CODEC.optionalFieldOf("id"))
                .copyOnDeath()
                .build()
        );

    public Boats(IEventBus modEventBus, ModContainer modContainer) {
        ATTACHMENT_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
    }
}
