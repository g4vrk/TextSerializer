package com.g4vrk.text.serializer.impl;

import com.g4vrk.text.serializer.Serializer;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface String2ComponentSerializer extends Serializer<String, Component> {
}
