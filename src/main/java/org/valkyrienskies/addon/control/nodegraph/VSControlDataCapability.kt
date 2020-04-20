package org.valkyrienskies.addon.control.nodegraph

import org.valkyrienskies.mod.common.capability.framework.VSDefaultCapability

class VSControlDataCapability : VSDefaultCapability<VSControlData>(VSControlData::class.java, { VSControlData() })