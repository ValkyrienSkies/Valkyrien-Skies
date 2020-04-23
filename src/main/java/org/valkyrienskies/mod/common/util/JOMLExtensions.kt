package org.valkyrienskies.mod.common.util

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.joml.Vector3d
import org.joml.Vector3i

fun Vec3d.toJOML(): Vector3d = JOML.convert(this)
fun Vec3i.toJOML(): Vector3i = JOML.convert(this)