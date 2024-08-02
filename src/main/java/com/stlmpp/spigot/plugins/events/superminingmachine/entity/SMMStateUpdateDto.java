package com.stlmpp.spigot.plugins.events.superminingmachine.entity;

import org.jetbrains.annotations.Nullable;

public record SMMStateUpdateDto(
    int isRunning,
    int hasFinished,
    @Nullable String lastBlock,
    int boostTimes,
    double boostFactor) {}
