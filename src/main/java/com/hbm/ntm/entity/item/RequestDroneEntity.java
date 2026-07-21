package com.hbm.ntm.entity.item;

import com.hbm.ntm.blockentity.DroneLogisticsBlockEntity;
import com.hbm.ntm.drone.DroneFilter;
import com.hbm.ntm.item.DroneItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Server-authoritative programmed delivery run used by drone docks. */
public class RequestDroneEntity extends DroneBaseEntity {
    private final List<Step> program=new ArrayList<>(); private ItemStack held=ItemStack.EMPTY; private int actionDelay;
    public RequestDroneEntity(EntityType<? extends RequestDroneEntity> type,Level level){super(type,level);}
    @Override public void setTarget(double x,double y,double z){super.setTarget(x,y+1.0D,z);}
    @Override public double speed(){return .625D;}
    public void addRoute(List<BlockPos> route){route.forEach(this::addPosition);} public void addPosition(BlockPos pos){program.add(new Step("pos",pos,null));}
    public void addPickup(DroneFilter filter){program.add(new Step("pickup",null,filter));} public void addUnload(){program.add(new Step("unload",null,null));} public void addDock(){program.add(new Step("dock",null,null));}
    @Override public void tick(){super.tick();if(level().isClientSide||getDeltaMovement().length()>=.01D)return;if(actionDelay>0){actionDelay--;return;}if(program.isEmpty()){selfDestructWithoutCargo();return;}Step step=program.remove(0);switch(step.type){case "pos"->setTarget(step.pos.getX()+.5,step.pos.getY(),step.pos.getZ()+.5);case "pickup"->{pickup(step.filter);actionDelay=5;}case "unload"->{unload();actionDelay=5;}case "dock"->dock();default->{}}}
    /**
     * EntityRequestDrone ray-traced exactly four blocks down before each station action.
     * A vertical block-entity scan would incorrectly act through a roof or another station.
     */
    private DroneLogisticsBlockEntity below(DroneLogisticsBlockEntity.Kind kind) {
        Vec3 start = position();
        BlockHitResult hit = level().clip(new ClipContext(start, start.add(0.0D, -4.0D, 0.0D),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.BLOCK) return null;
        if (level().getBlockEntity(hit.getBlockPos()) instanceof DroneLogisticsBlockEntity entity
                && entity.kind() == kind) return entity;
        return null;
    }
    private void pickup(DroneFilter filter){DroneLogisticsBlockEntity provider=below(DroneLogisticsBlockEntity.Kind.PROVIDER);if(provider==null||!held.isEmpty())return;for(int i=0;i<9;i++){ItemStack stack=provider.items().getStackInSlot(i);if(filter.matches(stack)){held=provider.items().extractItem(i,stack.getCount(),false);setAppearance(1);level().playSound(null,getX(),getY(),getZ(),ModSounds.ITEM_UNPACK.get(),net.minecraft.sounds.SoundSource.BLOCKS,.5F,.75F);break;}}}
    private void unload() {
        DroneLogisticsBlockEntity requester = below(DroneLogisticsBlockEntity.Kind.REQUESTER);
        if (requester == null || held.isEmpty()) return;

        // EntityRequestDrone merged by item + metadata only.  Like the legacy inventory
        // code, this deliberately does not require matching NBT/components before growing
        // an existing stack; modern ItemStackHandler insertion would be stricter here.
        for (int i = 9; i < 18 && !held.isEmpty(); i++) {
            ItemStack stock = requester.items().getStackInSlot(i);
            // EntityRequestDrone required both item identity and legacy item damage to
            // match before merging. ItemStack.isSameItem only checks the modern item ID,
            // which would incorrectly mix damage-backed legacy variants.
            if (stock.isEmpty() || !stock.is(held.getItem())
                    || stock.getDamageValue() != held.getDamageValue()) continue;
            int transferred = Math.min(stock.getMaxStackSize() - stock.getCount(), held.getCount());
            if (transferred > 0) {
                stock.grow(transferred);
                held.shrink(transferred);
                requester.items().setStackInSlot(i, stock);
            }
        }
        for (int i = 9; i < 18 && !held.isEmpty(); i++) {
            if (requester.items().getStackInSlot(i).isEmpty()) {
                requester.items().setStackInSlot(i, held.copy());
                held = ItemStack.EMPTY;
            }
        }
        if (held.isEmpty()) {
            setAppearance(0);
            level().playSound(null, getX(), getY(), getZ(), ModSounds.ITEM_UNPACK.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, .5F, .75F);
        }
    }
    private void dock(){DroneLogisticsBlockEntity dock=below(DroneLogisticsBlockEntity.Kind.DOCK);ItemStack drone=DroneItem.withType(new ItemStack(ModItems.DRONE.get()),DroneItem.DroneType.REQUEST);if(dock!=null&&dock.insertRequestDrone(drone,held)){level().playSound(null,dock.getBlockPos(),ModSounds.BLOCK_STORAGE_CLOSE.get(),net.minecraft.sounds.SoundSource.BLOCKS,2.0F,1.0F);discard();return;}returnDrone();}
    /**
     * EntityRequestDrone's empty-program branch self-destructed and returned only the
     * request drone item.  It was distinct from a failed DOCK step, which also dropped
     * held cargo; do not collapse those two legacy failure contracts.
     */
    private void selfDestructWithoutCargo(){if(!level().isClientSide){spawnAtLocation(DroneItem.withType(new ItemStack(ModItems.DRONE.get()),DroneItem.DroneType.REQUEST));discard();}}
    private void returnDrone(){if(!level().isClientSide){if(!held.isEmpty())spawnAtLocation(held);spawnAtLocation(DroneItem.withType(new ItemStack(ModItems.DRONE.get()),DroneItem.DroneType.REQUEST));discard();}}
    @Override public boolean hurt(DamageSource source,float amount){
        // EntityRequestDrone#hitByEntity ignored all later hits after setDead(), so a
        // duplicate same-tick attack cannot return its held item and request drone twice.
        if(isRemoved())return false;
        if(!level().isClientSide&&source.getEntity() instanceof Player)returnDrone();
        return false;
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(!held.isEmpty())tag.put("held",held.save(new CompoundTag()));ListTag list=new ListTag();for(Step step:program)list.add(step.save());tag.put("program",list);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);held=ItemStack.of(tag.getCompound("held"));actionDelay=5;program.clear();for(var raw:tag.getList("program",10))program.add(Step.load((CompoundTag)raw));}
    private record Step(String type,BlockPos pos,DroneFilter filter){CompoundTag save(){CompoundTag tag=new CompoundTag();tag.putString("type",type);if(pos!=null)tag.putLong("pos",pos.asLong());if(filter!=null)tag.put("filter",filter.save());return tag;}static Step load(CompoundTag tag){return new Step(tag.getString("type"),tag.contains("pos")?BlockPos.of(tag.getLong("pos")):null,tag.contains("filter")?DroneFilter.load(tag.getCompound("filter")):null);}}
}
