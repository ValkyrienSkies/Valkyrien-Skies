# Tile Entity Behaviours

How do behaviours work? Because java doesn't support multiple inheritance,
we sometimes want to compose multiple types of tile entities into one. 
The **Tile Entity Behaviour System** enables us compose multiple properties into one 
tile entity, such as providing force on  a ship.

## Using Behaviours

### Java

```java
class MyTileEntity extends BehaviourControlledTileEntity {
    MyTileEntity() {
        super(BehaviourToAdd.Factory, MyBehaviour.Factory);
    }
}
```

### Kotlin

```kotlin
class MyTileEntity : BehaviourControlledTileEntity(
    BehaviourToAdd.Factory, MyBehaviour.Factory
)
```

## Accessing Behaviours

If you know it's a behaviour controlled tile entity...
```java
BehaviourControlledTileEntity te = ...;
MyBehaviour behaviour = te.getBehaviour(MyBehaviour.class);
```
Otherwise,
```java
TileEntity te = ...
// Returns null if it doesn't have it
MyBehaviour behaviour = TEBehaviours.getBehaviour(te, MyBehaviour.class);
```

## Creating Behaviours

You must extend `TileEntityBehaviour`. 
Behaviours currently can override the following methods from that class:
```kotlin
open fun writeToNBT(): NBTTagCompound? = null
open fun readFromNBT(compound: NBTTagCompound) {}
open fun getUpdateTag(): NBTTagCompound? = null
open fun handleUpdateTag(tag: NBTTagCompound) {}
open fun getUpdatePacket(): NBTTagCompound? = null
open fun onDataPacket(compound: NBTTagCompound) {}
```

Note that each behaviour gets its own NBT, so it's not possible to read the data
of other behaviours or to have naming conflicts. Behaviours can also
access their owning TileEntity with the `owner` field.

Here is an example behaviour that stores some data by manually writing it to NBT

```kotlin
class MyBehaviour(owner: TileEntity) : TileEntityBehaviour(owner) {
    var counter = 0

    override fun writeToNBT(): NBTTagCompound? {
        val tag = NBTTagCompound()
        tag.setInteger("counter", counter)
    }
    override fun readFromNBT(tag: NBTTagCompound) {
        counter = tag.getInteger("counter")
    }

    companion object {
        @JvmStatic
        val Factory = BehaviourFactoryFactory.create(MyBehaviour::class.java)
    }

}
```

Note that behaviours need a factory to be used. Since it's mostly boilerplate,
you can automatically create one for any behaviour with only a TileEntity in
the constructor using `BehaviourFactoryFactory.create`.

Manually writing to NBT for a lot of fields is tedious. So, you can extend 
`TileEntityBehaviourWithData`.

```kotlin
class MyBehaviour(owner: TileEntity) : 
    TileEntityBehaviourWithData(owner, MyData(), Data::class.java) {
    
    fun increment() {
        // Access the Data instance using 'data'
        data.counter++
    }

    // The data is automatically serialized with Jackson
    // and accessible through the 'data' variable
    // readFromNBT and writeFromNBT are implemented for you
    private class MyData {
        var counter = 0
    }

    companion object {
        @JvmStatic
        val Factory = BehaviourFactoryFactory.create(MyBehaviour::class.java)
    }

}
```