package com.avetharun.herbiary.rei;

import com.avetharun.herbiary.ModItems;
import com.avetharun.herbiary.recipes.RecipesUtil;
import com.avetharun.herbiary.recipes.WorkstationRecipe;
import com.avetharun.herbiary.screens.WorkstationScreen;
import me.shedaniel.math.Dimension;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HerbiaryReiPlugin implements REIClientPlugin {
    public static class HerbiaryWorkstationDisplay extends BasicDisplay {
        public HerbiaryWorkstationDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
            super(inputs, outputs);
        }
        public HerbiaryWorkstationDisplay(RecipeEntry<WorkstationRecipe> recipe) {
            super(getInputList(recipe.value()), Collections.singletonList(EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, recipe.value().result))));
            this.tool = Collections.singletonList(EntryIngredients.ofIngredient(recipe.value().requiredTool));
            this.heatNeeded = recipe.value().requireHeat;
        }

        private static List<EntryIngredient> getInputList(WorkstationRecipe recipe) {
            if (recipe == null) return Collections.emptyList();
            return EntryIngredients.ofIngredients(recipe.getIngredients());
        }
        boolean heatNeeded = false;

        public boolean requiresHeatForRecipe() {
            return heatNeeded;
        }
        List<EntryIngredient> tool;
        @Override
        public List<EntryIngredient> getRequiredEntries() {
            return tool;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return HerbiaryWorkstationCategory.WORKSTATION_CATEGORY;
        }
    }
    public static class HerbiaryWorkstationCategory implements DisplayCategory<HerbiaryWorkstationDisplay> {

        private static final Identifier TEXTURE = new Identifier("al_herbiary",  "textures/gui/container/workstation_rei.png");
        public static final CategoryIdentifier<HerbiaryWorkstationDisplay> WORKSTATION_CATEGORY = CategoryIdentifier.of("al_herbiary", "workstation");
        @Override
        public CategoryIdentifier<? extends HerbiaryWorkstationDisplay> getCategoryIdentifier() {
            return WORKSTATION_CATEGORY;
        }

        @Override
        public int getFixedDisplaysPerPage() {
            return 1;
        }

        @Override
        public List<Widget> setupDisplay(HerbiaryWorkstationDisplay display, Rectangle bounds) {
            final Point startPoint = new Point(bounds.getCenterX() - 130 / 2, bounds.getCenterY() - 25);
            List<Widget> widgets= new LinkedList<>();
            widgets.add(Widgets.createTexturedWidget(TEXTURE, new Rectangle(startPoint, new Dimension(130, 75))));
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 37, startPoint.y + 17)).entries(display.getRequiredEntries().get(0))); // tool slot
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 37, startPoint.y + 39)).entries(display.getInputEntries().get(0))); // ingredient slot
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 79, startPoint.y + 28)).entries(display.getOutputEntries().get(0))); // output slot
            if (display.requiresHeatForRecipe()) {
                Text t = Text.translatable("generic.workstation.heat_required").formatted(Formatting.ITALIC, Formatting.GOLD);
                var rect = new Rectangle(bounds.getCenterX() - 8, bounds.getCenterY() + 16, 19, 13);
                widgets.add(Widgets.createTexturedWidget(TEXTURE, rect, 0, 132));
                widgets.add(Widgets.createTooltip(rect, t, Text.translatable("rei.workstation.heat_required").formatted(Formatting.GOLD,Formatting.ITALIC)));
            }
            return widgets;

        }

        @Override
        public Text getTitle() {
            return Text.translatable("container.al_herbiary.workstation");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(ModItems.MORTAR.getRight().getDefaultStack());
        }
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        REIClientPlugin.super.registerCategories(registry);
        registry.add(new HerbiaryWorkstationCategory());
        registry.addWorkstations(HerbiaryWorkstationCategory.WORKSTATION_CATEGORY, EntryStacks.of(ModItems.MORTAR.getLeft()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        REIClientPlugin.super.registerDisplays(registry);
        registry.registerRecipeFiller(WorkstationRecipe.class, RecipesUtil.MORTAR_RECIPE_TYPE, HerbiaryWorkstationDisplay::new);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        REIClientPlugin.super.registerScreens(registry);
        registry.registerClickArea(screen -> new Rectangle(75, 40, 130, 10), WorkstationScreen.class, HerbiaryWorkstationCategory.WORKSTATION_CATEGORY);
    }
}
