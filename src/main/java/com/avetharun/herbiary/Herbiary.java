package com.avetharun.herbiary;

import com.avetharun.herbiary.Items.FlintIgniter;
import com.avetharun.herbiary.Items.UnlockableNamedItem;
import com.avetharun.herbiary.command.LearnCommand;
import com.avetharun.herbiary.entity.ModEntityTypes;
import com.avetharun.herbiary.hUtil.HerbDescriptor;
import com.avetharun.herbiary.hUtil.ModStatusEffects.Bleed;
import com.avetharun.herbiary.hUtil.Season;
import com.avetharun.herbiary.hUtil.alib;
import com.avetharun.herbiary.packet.FlintIgniterIgnitePacket;
import com.avetharun.herbiary.packet.HerbiaryBlockStateInitPacket;
import com.avetharun.herbiary.packet.SwitchArrowTypePacket;
import com.avetharun.herbiary.packet.UnlockItemNamePacket;
import com.avetharun.herbiary.recipes.RecipesUtil;
import com.avetharun.herbiary.screens.BackpackScreenHandler;
import com.avetharun.herbiary.screens.WorkstationScreenHandler;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Herbiary implements ModInitializer {
    public static RegistryKey<Registry<HerbDescriptor>> HERB_DESCRIPTOR_KEY;
    public static Registry<HerbDescriptor> HERB_DESCRIPTORS;
    public static HerbDescriptor EMPTY_OR_UNKNOWN_DESCRIPTOR= HerbDescriptor.Builder.create()
            .name("unknown")
            .data("{\"text\":\"You haven't found this herb yet.\"}")
            .build();;
    public static List<Block> AllowedHerbiaryBreakables = new ArrayList<>();
    public static final RegistryKey<PointOfInterestType> POI_NEST_KEY = RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, new Identifier("nest"));
    public static final PointOfInterestType POI_NEST = alib.registerPOIType(POI_NEST_KEY, alib.getStatesOfBlock(ModItems.BIRD_NEST_BLOCK.getLeft()), 1, 1);

    public static void updateGameRules(MinecraftServer minecraftServer) {
        sendAllPlayersBreakables(minecraftServer.getPlayerManager());
    }
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_VANILLA_RECIPES_UNLOCK =
            GameRuleRegistry.register("allowVanillaRecipesUnlock", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_HERB_PLACEMENTS =
            GameRuleRegistry.register("allowHerbPlacements", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false,
                    ((minecraftServer, booleanRule) -> {
                        updateGameRules(minecraftServer);
                    }
            )));

    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_VANILLA_BLOCK_BREAKING =
            GameRuleRegistry.register("allowVanillaBlockBreaking", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false,
                    ((minecraftServer, booleanRule) -> {
                        updateGameRules(minecraftServer);
                    }
            )));
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_VANILLA_MOB_SPAWNING = GameRuleRegistry.register("allowVanillaMobSpawning", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_VANILLA_RECIPES_CRAFT =
            GameRuleRegistry.register("allowVanillaRecipesCraft", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static String MOD_ID = "al_herbiary";
    public static Item OWL_SPAWN_EGG;
    public static Item MOUSE_SPAWN_EGG;
    public static final StatusEffect EFFECT_BLEED = new Bleed();
    public static int SeasonLength = 24/*days*/;
    public static final int DayLength = 24000;
    public static double clientRuntime = 0;
    public static Season getCurrentSeasonWorld(World world) {
        int worldTime = Math.toIntExact(world.getTimeOfDay()) / DayLength;
        int seasonTime = (worldTime / SeasonLength);
        return CURRENT_SEASON = Season.values()[seasonTime % 4];
    }
//    private static Item overrideItem(Item toOverride, Item newItem){
//        return Registry.register(Registries.ITEM, Registries.ITEM.getRawId(toOverride), Registries.ITEM.getId(toOverride).getPath(), newItem);
//    }
//
//    private static Block overrideBlock(Block toOverride, Block newBlock){
//        BlockItem newBlockItem = new BlockItem(newBlock, new FabricItemSettings());
//        overrideItem(toOverride.asItem(), newBlockItem);
//        return Registry.register(Registries.BLOCK, Registries.BLOCK.getRawId(toOverride), Registries.BLOCK.getId(toOverride).getPath(), newBlock);
//    }
    public static Season CURRENT_SEASON = Season.SPRING;
    public static Identifier HERBIARY_FIELD_GUIDE_BOOK_ID = new Identifier("al_herbiary", "field_guide");

    public static Identifier BACKPACK_UPDATE_PACKET_ID = new Identifier("al_herbiary", "backpack_update");
    public static Identifier IGNITER_IGNITE_PACKET_ID = new Identifier("al_herbiary", "c2s_igniter_ignited");
    public static Identifier SET_ARROW_TYPE_PACKET_ID = new Identifier("al_herbiary", "c2s_set_bow_type");
    public static Identifier HERBIARY_SYNC_PACKET_ID = new Identifier("al_herbiary", "sync");
    public static Identifier UNLOCK_ITEM_PACKET_ID = new Identifier("al_herbiary", "s2c_unlock_item_name");
    public static Identifier C2S_TRY_LEARN_ITEM_PACKET_ID = new Identifier("al_herbiary", "c2s_try_unlock_item");
    public static TagKey<Block> LAMPS = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "lamps"));
    public static TagKey<Block> MUSHROOM_PLACEABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "mushroom_placeable"));
    public static TagKey<Block> CAMPFIRE_SIGNAL_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "campfire_signal_blocks"));
    public static TagKey<Block> BIRD_NEST_PLACEABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "bird_nest_placeable"));
    public static TagKey<Block> ALLOWED_VANILLA_BREAKABLES = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "allowed_breakables"));
    public static TagKey<Block> NEST_COLLECTABLES = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "nest_collectables"));
    public static TagKey<Block> PARTIALLY_SUBMERGED_PLACEABLES = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "partially_submerged_placeables"));
    public static TagKey<Block> FARMLAND = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "farmland"));
    public static TagKey<Block> SHARPENING_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("al_herbiary", "blocks_that_can_sharpen"));

    public static TagKey<Item> ALLOWED_ITEMS_ON_TOOL_RACK = TagKey.of(RegistryKeys.ITEM, new Identifier("al_herbiary", "allowed_tool_rack_items"));
    public static TagKey<Item> TOOLS = ALLOWED_ITEMS_ON_TOOL_RACK;

    public static TagKey<Item> KNIVES = TagKey.of(RegistryKeys.ITEM, new Identifier("al_herbiary", "knives"));
    public static TagKey<Item> CAMPFIRE_PLACEABLE_ITEMS = TagKey.of(RegistryKeys.ITEM, new Identifier("al_herbiary", "campfire_placeable_items"));
    public static TagKey<Item> BOWS = TagKey.of(RegistryKeys.ITEM, new Identifier("al_herbiary", "bows"));
    public static TagKey<Item> ITEMS_THAT_BURN = TagKey.of(RegistryKeys.ITEM, new Identifier("al_herbiary", "items_that_burn"));
    public static TagKey<Item> HEAT_BLOCKING_ITEMS = TagKey.of(RegistryKeys.ITEM, new Identifier("al_herbiary", "heat_blocking_items"));

    private static PacketByteBuf gameRuleTick(HerbiaryBlockStateInitPacket p, MinecraftServer server) {
        alib.GetAllBlocksInTagAnd(ALLOWED_VANILLA_BREAKABLES, (pair) -> {
            p.appendBlock(pair.getLeft());
        });
        p.allow_vanilla_breaking = server.getGameRules().getBoolean(ALLOW_VANILLA_BLOCK_BREAKING);
        p.allow_herb_placement = server.getGameRules().getBoolean(ALLOW_HERB_PLACEMENTS);
        p.allow_mining_ores_and_stone = true;

        PacketByteBuf out = PacketByteBufs.create();
        p.write(out);
        return out;
    }

    public static void sendAllPlayersBreakables(PlayerManager manager) {
        var out = gameRuleTick(new HerbiaryBlockStateInitPacket(), manager.getServer());
        manager.getPlayerList().forEach(player->{
            ServerPlayNetworking.send(player, Herbiary.HERBIARY_SYNC_PACKET_ID, out);
        });
    }
    public static void sendPlayerBreakables(ServerPlayerEntity player) {
        assert player != null && player.getServer() != null;
        var out = gameRuleTick(new HerbiaryBlockStateInitPacket(), player.getServer());
        ServerPlayNetworking.send(player, Herbiary.HERBIARY_SYNC_PACKET_ID, out);
    }
    public static final TagKey<Item> ELYTRI = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "elytra"));
    public static DefaultParticleType FLINT_SPARK = FabricParticleTypes.simple();
    public static DefaultParticleType FLINT_SPARK_SMOKE = FabricParticleTypes.simple();
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access) {}
    static boolean ALLOW_VANILLA_RECIPE_CRAFT_last = false;
    public static final class ModCredit implements StringIdentifiable {
        String modId = "unknown";
        String modName;
        String modUrl;
        String modVersion;
        ArrayList<String> modAuthors = new ArrayList<>();
        public static ModCredit deserialize(JsonObject modCreditRef) {
            ModCredit credit = new ModCredit();
            credit.modName = JsonHelper.getString(modCreditRef, "name", "Name not specified");
            credit.modUrl = JsonHelper.getString(modCreditRef, "url", "URL not specified");
            credit.modVersion = JsonHelper.getString(modCreditRef, "version", "Version not specified");
            if (modCreditRef.has("authors") && modCreditRef.get("authors").isJsonArray()) {
                for (var elem : modCreditRef.getAsJsonArray("authors")) {
                    credit.modAuthors.add(elem.getAsString());
                }
            }
            return credit;
        }
        public static ArrayList<ModCredit> getCredits(JsonObject root) {
            ArrayList<ModCredit> credits = new ArrayList<>();
            var modsO = root.getAsJsonObject("mods");
            for (var key : modsO.keySet()) {
                ModCredit credit = deserialize(modsO.getAsJsonObject(key));
                credit.modId = key;
                credits.add(credit);
            }
            return credits;
        }
        public static ArrayList<ModCredit> getLibCredits(JsonObject root) {
            ArrayList<ModCredit> credits = new ArrayList<>();
            var modsO = root.getAsJsonObject("libraries");
            for (var key : modsO.keySet()) {
                ModCredit credit = deserialize(modsO.getAsJsonObject(key));
                credit.modId = key;
                credits.add(credit);
            }
            return credits;
        }

        @Override
        public String asString() {
            return modId;
        }
    }
    public static JsonObject CREDITS_FILE;
    public static ArrayList<ModCredit> MOD_CREDITS = new ArrayList<>();
    public static ArrayList<ModCredit> LIB_CREDITS = new ArrayList<>();
    public static class CreditsScreen extends BaseOwoScreen<FlowLayout> {

        @Override
        protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
            return OwoUIAdapter.create(this, Containers::verticalFlow);
        }

        @Override
        protected void build(FlowLayout rootComponent) {
            assert MinecraftClient.getInstance().currentScreen != null;
            rootComponent.surface(Surface.OPTIONS_BACKGROUND);
            var fl0 = Containers.verticalFlow(Sizing.expand(), Sizing.content());
            fl0.padding(Insets.bottom(32));
            var modContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Text.literal("Mods"), true);
            for (var credit : MOD_CREDITS) {
                var mod = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal(credit.modName).formatted(Formatting.BOLD), false);
                if (!credit.modAuthors.isEmpty()) {
                    var authorComponent = Containers.collapsible(Sizing.content(), Sizing.content(), Text.of("Authors"), false);
                    for (var author : credit.modAuthors) {
                        authorComponent.child(Components.label(Text.of(author)));
                    }
                    mod.child(authorComponent);
                }
                mod.padding(Insets.bottom(1));
                boolean modrinth = credit.modUrl.contains("modrinth");
                boolean curseforge = credit.modUrl.contains("curseforge");
                if (!credit.modUrl.isEmpty()) {
                    mod.child(Components.button(Text.of("View Mod on " + (modrinth ? "Modrinth" : curseforge ? "Curseforge" : "the author's website")), buttonComponent -> {
                        Util.getOperatingSystem().open(credit.modUrl);
                    }));
                }
                mod.padding(Insets.bottom(2));
                modContainer.child(mod);
//                fl1 = (ScrollContainer<FlowLayout>) fl1.child(mod);
            }
            rootComponent.child(Components.spacer());
            var libContainer = Containers.collapsible(Sizing.fill(), Sizing.content(), Text.literal("Libraries").formatted(Formatting.ITALIC), false);
            for (var lib : LIB_CREDITS) {

                var mod = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal(lib.modName).formatted(Formatting.BOLD), false);
                if (!lib.modAuthors.isEmpty()) {
                    var authorComponent = Containers.collapsible(Sizing.content(), Sizing.content(), Text.of("Authors"), false);
                    for (var author : lib.modAuthors) {
                        authorComponent.child(Components.label(Text.of(author)));
                    }
                    mod.child(authorComponent);
                }
                mod.padding(Insets.bottom(1));
                boolean modrinth = lib.modUrl.contains("modrinth");
                boolean curseforge = lib.modUrl.contains("curseforge");
                if (!lib.modUrl.isEmpty()) {
                    mod.child(Components.button(Text.of("View Mod on " + (modrinth ? "Modrinth" : curseforge ? "Curseforge" : "the author's website")), buttonComponent -> {
                        Util.getOperatingSystem().open(lib.modUrl);
                    }));
                }
                mod.padding(Insets.bottom(2));
                libContainer.child(mod);
            }
            fl0.child(modContainer);
            fl0.child(libContainer);
            var fl1 = Containers.verticalScroll(Sizing.fill(), Sizing.fill(), fl0);
            var fl2 = Containers.verticalFlow(Sizing.fill(), Sizing.content()).child(Components.button(Text.literal("Back"), buttonComponent -> MinecraftClient.getInstance().currentScreen.close()).verticalSizing(Sizing.fixed(12)))
                    .surface(Surface.VANILLA_TRANSLUCENT);
            rootComponent.child(fl2);
            rootComponent.child(fl1);

        }
    }
    @Override
    public void onInitialize() {
        BiomeModifications.create(new Identifier("al_herbiary:modify_overworld")).add(ModificationPhase.POST_PROCESSING, BiomeSelectors.foundInOverworld(),
            (biomeSelectionContext, biomeModificationContext) -> {
                biomeModificationContext.getSpawnSettings().clearSpawns(SpawnGroup.MONSTER);
            }
        );
        BiomeModifications.create(new Identifier("al_herbiary:modify_nether")).add(ModificationPhase.POST_PROCESSING, BiomeSelectors.foundInOverworld(),
            (biomeSelectionContext, biomeModificationContext) -> {
                if (BiomeSelectors.spawnsOneOf(Set.of(EntityType.PIGLIN)).test(biomeSelectionContext)) {
                    biomeModificationContext.getSpawnSettings().addSpawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.ZOMBIE, Weight.of(4), 1, 2));
                }
            }
        );
        BiomeModifications.create(new Identifier("al_herbiary:modify_end")).add(ModificationPhase.POST_PROCESSING, BiomeSelectors.foundInTheEnd(),
            (biomeSelectionContext, biomeModificationContext) -> {
                biomeModificationContext.getSpawnSettings().addSpawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.CREEPER, Weight.of(4), 1, 2));
            }
        );
        RegistryOverrides.Override();
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_FOREST), GenerationStep.Feature.VEGETAL_DECORATION, RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("al_herbiary","blueberry_bush")));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LearnCommand.register(dispatcher,registryAccess);
        });
        
        ModItems.registerModItemData();
        ModEntityTypes.InitializeModEntityTypes();
        OWL_SPAWN_EGG = Registry.register(Registries.ITEM, new Identifier("al_herbiary", "owl_spawn_egg"), new SpawnEggItem(ModEntityTypes.OWL_ENTITY_TYPE,
                0x1A1A1A,
                0xF2ADA1,
                (new Item.Settings())));

        OWL_SPAWN_EGG = Registry.register(Registries.ITEM, new Identifier("al_herbiary", "field_mouse_spawn_egg"), new SpawnEggItem(ModEntityTypes.FIELD_MOUSE_ENTITY_TYPE,
                0x9c9375,
                0x726951,
                (new Item.Settings())));

        Registry.register(Registries.STATUS_EFFECT, new Identifier("al_herbiary", "bleed"), EFFECT_BLEED);
        Registry.register(Registries.SOUND_EVENT, Herbiary.SPEAR_THROWN_ID, Herbiary.SPEAR_THROWN);
        Registry.register(Registries.SOUND_EVENT, Herbiary.SPEAR_LAND_ID, Herbiary.SPEAR_LAND);
        Registry.register(Registries.SOUND_EVENT, Herbiary.FLINT_SUCCEED_ID, Herbiary.FLINT_SUCCEED);
        Registry.register(Registries.SOUND_EVENT, Herbiary.UNZIP_TENT_ID, Herbiary.UNZIP_TENT);
        Registry.register(Registries.SOUND_EVENT, Herbiary.FLINT_FAIL_ID, Herbiary.FLINT_FAIL);
        Registry.register(Registries.SOUND_EVENT, Herbiary.ROCK_STRIKE_ID, Herbiary.ROCK_STRIKE);


        RecipesUtil.WORKSTATION_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, RecipesUtil.MORTAR_ID, new ScreenHandlerType<>(WorkstationScreenHandler.Factory::create, FeatureSet.of(FeatureFlags.VANILLA)));

        BackpackScreenHandler.BACKPACK_SCREEN_HANDLER_TYPE = Registry.register(Registries.SCREEN_HANDLER, BackpackScreenHandler.SID, new ScreenHandlerType<>(BackpackScreenHandler::new, FeatureSet.of(FeatureFlags.VANILLA)));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sendPlayerBreakables(handler.player);

            UnlockItemNamePacket p = new UnlockItemNamePacket();
            PacketByteBuf buf = PacketByteBufs.create();
            ArrayList<String> m_PlayerKnowns = alib.getMixinField(handler.player, "knownIDs");
            m_PlayerKnowns.addAll(p.unlockedTranslationKeys);
            p.write(buf);
            ServerPlayNetworking.send(handler.player, Herbiary.UNLOCK_ITEM_PACKET_ID, buf);

        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            sendAllPlayersBreakables(server.getPlayerManager());

            for (RecipeEntry<?> value : server.getRecipeManager().values()) {
                if (Objects.equals(value.id().getNamespace(), "minecraft") && !server.getGameRules().getBoolean(ALLOW_VANILLA_RECIPES_CRAFT)) {
                    server.getPlayerManager().getPlayerList().forEach(entity -> {
                        entity.lockRecipes(List.of(value));
                    });
                }
            }
        });
        ServerTickEvents.START_SERVER_TICK.register(server -> {
        });
        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> ActionResult.SUCCESS);
        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, sleepingDirection) -> {
            var ents = alib.getEntitiesOfTypeInRange(entity.getWorld(), sleepingPos, 1.25f, ModEntityTypes.TENT_ENTITY_TYPE);
            if (ents.size() > 0) {
                return ents.get(0).getFacing().rotateClockwise(Direction.Axis.Y);
            }
            return sleepingDirection;
        });
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            // spawn owls
            getCurrentSeasonWorld(world);
        });
        HERB_DESCRIPTOR_KEY = RegistryKey.ofRegistry(new Identifier("herb_descriptor"));
        HERB_DESCRIPTORS = FabricRegistryBuilder.createSimple(HerbDescriptor.class, new Identifier("herb_descriptors")).buildAndRegister();
        Registry.register(HERB_DESCRIPTORS, new Identifier("empty"), EMPTY_OR_UNKNOWN_DESCRIPTOR);
        ServerPlayNetworking.registerGlobalReceiver(IGNITER_IGNITE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            FlintIgniterIgnitePacket p = new FlintIgniterIgnitePacket(buf);
            if (p.succeeded && player.getBlockPos().getSquaredDistance(p.pos.toCenterPos()) < 9*9) {
                FlintIgniter.HandlePlacement(p.pos, player.getWorld(), player, p.hand, p.result, p.stack);
            }
            p.stack.damage(1, player, entity -> {
                entity.sendToolBreakStatus(p.hand);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_ARROW_TYPE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            SwitchArrowTypePacket p = new SwitchArrowTypePacket(buf);
            alib.setMixinField(player,"selectedArrowType", p.wantedArrowType);
            // forward to client for client-sided rendering and gui
            var pack_resp = new OverlayMessageS2CPacket(Text.of("Set arrow type to " + p.wantedArrowType.getName()));
            player.networkHandler.sendPacket(pack_resp);
        });
        ServerPlayNetworking.registerGlobalReceiver(C2S_TRY_LEARN_ITEM_PACKET_ID, (server, player, handler, buf, responseSender)->{
            ItemStack stack = buf.readItemStack();
            if (alib.playerHasItem(player, stack)) {
                UnlockableNamedItem.Unlock(player, false, stack);
            };
        });
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("al_herbiary", "flint_spark"), FLINT_SPARK);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("al_herbiary", "flint_smoke"), FLINT_SPARK_SMOKE);
    }
    public static final FeatureFlag HERBIARY_FANTASY;
    public static final ItemGroup HERBIARY_FANTASY_BLOCKS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.INKY_MUSHROOM_BLOCK.getRight()))
            .displayName(Text.translatable("itemGroup.al_herbiary.fantasy"))
            .build();
    static {
        FeatureManager.Builder builder = new FeatureManager.Builder("main");
        HERBIARY_FANTASY = builder.addFlag(new Identifier("c", "fantasy"));
    }



    public static final Identifier SPEAR_THROWN_ID;
    public static final Identifier SPEAR_LAND_ID;
    public static final Identifier FLINT_FAIL_ID;
    public static final Identifier FLINT_SUCCEED_ID;
    public static final Identifier UNZIP_TENT_ID;
    public static final Identifier ROCK_STRIKE_ID;
    public static SoundEvent ROCK_STRIKE;
    public static SoundEvent UNZIP_TENT;
    public static SoundEvent FLINT_SUCCEED;
    public static SoundEvent FLINT_FAIL;
    public static SoundEvent SPEAR_LAND;
    public static SoundEvent SPEAR_THROWN;
    static {
        SPEAR_THROWN_ID = new Identifier("al_herbiary:spear_thrown");
        SPEAR_THROWN = SoundEvent.of(SPEAR_THROWN_ID);
        SPEAR_LAND_ID = new Identifier("al_herbiary:spear_land");
        SPEAR_LAND = SoundEvent.of(SPEAR_LAND_ID);
        FLINT_FAIL_ID = new Identifier("al_herbiary:flint_fail");
        FLINT_FAIL = SoundEvent.of(FLINT_FAIL_ID);
        FLINT_SUCCEED_ID = new Identifier("al_herbiary:flint_succeed");
        FLINT_SUCCEED = SoundEvent.of(FLINT_SUCCEED_ID);
        UNZIP_TENT_ID = new Identifier("al_herbiary:unzip_tent");
        UNZIP_TENT = SoundEvent.of(UNZIP_TENT_ID);
        ROCK_STRIKE_ID = new Identifier("al_herbiary:rock_strike");
        ROCK_STRIKE = SoundEvent.of(ROCK_STRIKE_ID);
//        OVERRIDE_VANILLA_ITEM.put(new Identifier("minecraft:flint_and_steel"), );
    }

}
