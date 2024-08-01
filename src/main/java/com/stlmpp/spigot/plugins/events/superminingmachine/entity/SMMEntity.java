package com.stlmpp.spigot.plugins.events.superminingmachine.entity;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public record SMMEntity(
    String id,
    String world,
    String bottomLeft,
    String bottomRight,
    String topLeft,
    String topRight,
    @Nullable String lastBlock,
    Double boostFactor,
    Integer boostTimes,
    Integer isRunning,
    Integer hasFinished,
    List<SMMBlockEntity> blocks) {}
