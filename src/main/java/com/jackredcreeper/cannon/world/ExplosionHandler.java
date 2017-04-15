package com.jackredcreeper.cannon.world;

import java.util.List;

import com.jackredcreeper.cannon.init.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.jackredcreeper.cannon.entities.EntityCannonball;
import com.jackredcreeper.cannon.entities.EntityExplosiveball;
import com.jackredcreeper.cannon.entities.EntityGrapeshot;
import com.jackredcreeper.cannon.entities.EntitySolidball;
public class ExplosionHandler {
	

	
	@SubscribeEvent
    public void expDet(ExplosionEvent.Detonate event) {
				
        for (int k2 = 0; k2 < event.getAffectedEntities().size(); ++k2)
        {
        Entity entity = (Entity)event.getAffectedEntities().get(k2);
        if (entity instanceof EntityGrapeshot | entity instanceof EntitySolidball |
        		entity instanceof EntityCannonball | entity instanceof EntityExplosiveball) {
        	
        	event.getAffectedEntities().remove(k2);
        	}
        }
	}
}
