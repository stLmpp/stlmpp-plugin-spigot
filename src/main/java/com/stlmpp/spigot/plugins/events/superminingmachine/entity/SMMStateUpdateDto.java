package com.stlmpp.spigot.plugins.events.superminingmachine.entity;

public record SMMStateUpdateDto(
    int isRunning, int hasFinished, String lastBlock, int boostTimes, double boostFactor) {}
