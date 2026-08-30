package com.hbm.ntm.item;

import com.hbm.ntm.block.NTMAnvilBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** Item carrier for the shared 1.7.10 anvil inventory renderer. */
public final class NTMAnvilBlockItem extends BlockItem {
    private final NTMAnvilBlock anvil;

    public NTMAnvilBlockItem(NTMAnvilBlock anvil, Properties properties) {
        super(anvil, properties);
        this.anvil = anvil;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Tier " + anvil.tier() + " Anvil").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptAnvil", consumer);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (context.getLevel().isClientSide && result.consumesAction()) {
            // The vanilla BlockItem feedback is normally authoritative on the server.  Replaying
            // the registered event locally keeps anvil placement audible for this client too.
            context.getLevel().playLocalSound(context.getClickedPos(), SoundEvents.ANVIL_PLACE,
                    SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        return result;
    }
}
