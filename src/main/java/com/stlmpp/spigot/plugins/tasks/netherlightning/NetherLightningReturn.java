package com.stlmpp.spigot.plugins.tasks.netherlightning;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public record NetherLightningReturn(
    @Nullable World world,
    boolean isReal,
    @Nullable Location lightningLocation,
    @Nullable Float explosionPower) {}
