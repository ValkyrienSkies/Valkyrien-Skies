package com.jackredcreeper.cannon.proxy;

import com.jackredcreeper.cannon.init.ModBlocks;
import com.jackredcreeper.cannon.init.ModItems;

public class ClientProxy implements CommonProxy {
	
	@Override
	public void init() {
		ModItems.registerRenders();
		ModBlocks.registerRenders();
		
	}
}
