package org.valkyrienskies.mod.common.tileentity.behaviour

import net.minecraft.tileentity.TileEntity
import java.lang.reflect.Constructor

/**
 * I apologise, I know that it's a meme
 * but yes, it really does create behaviour factories
 */
object BehaviourFactoryFactory {

    /**
     * Create a TileEntityBehaviourFactory for the specified class,
     * using the specified constructor function
     */
    fun <T: TileEntityBehaviour> create(clazz: Class<T>, constructor: (TileEntity) -> T): TileEntityBehaviourFactory<T> {
        return (object : TileEntityBehaviourFactory<T> {
            override fun createBehaviour(owner: TileEntity): T = constructor(owner)

            override val clazz: Class<T> = clazz
        })
    }

    /**
     * Creates a TileEntityBehaviourFactory for the specified Behaviour class.
     * Note that it must have either a no-args constructor or single-arg constructor
     * that accepts a TileEntity. Otherwise, use [create]
     */
    fun <T: TileEntityBehaviour> create(clazz: Class<T>): TileEntityBehaviourFactory<T> {
        val teConstructor: Constructor<T>? = try {
             clazz.getConstructor(TileEntity::class.java)
        } catch (e: NoSuchMethodException) {
            null
        }

        val noArgsConstructor: Constructor<T>? = try {
            clazz.getConstructor()
        } catch (e: NoSuchMethodException) {
            null
        }

        if (teConstructor == null && noArgsConstructor == null) {
            throw IllegalArgumentException("The class must have a no-args constructor or a constructor that " +
                    "accepts a single tile entity!")
        }

        return (object : TileEntityBehaviourFactory<T> {
            override fun createBehaviour(owner: TileEntity): T {
                return if (teConstructor != null) teConstructor.newInstance(owner)
                else noArgsConstructor!!.newInstance()
            }

            override val clazz: Class<T> = clazz
        })
    }

}