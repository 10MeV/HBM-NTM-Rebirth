package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.GlyphidSpawnerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class LegacyGlyphidSpawnerBlock extends BaseEntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);
    public LegacyGlyphidSpawnerBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(VARIANT,0));}
    public static BlockState withLegacyVariant(BlockState s,int v){return s.hasProperty(VARIANT)?s.setValue(VARIANT,Math.max(0,Math.min(2,v))):s;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b){b.add(VARIANT);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GlyphidSpawnerBlockEntity(p,s);}
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=ModBlockEntities.GLYPHID_SPAWNER.get()?null:(a,b,c,d)->GlyphidSpawnerBlockEntity.serverTick(a,b,c,(GlyphidSpawnerBlockEntity)d);}
}
