package com.stlmpp.spigot.plugins.events.superminingmachine.entity;

import org.jetbrains.annotations.Nullable;

public record SMMBlockEntity(@Nullable Integer id, String smmId, String location, String type) {}
