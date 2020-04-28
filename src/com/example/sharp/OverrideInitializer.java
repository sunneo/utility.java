package com.example.sharp;

/**
 * design to achieve Property-overriding statement in C# use constructor to make
 * sure that IOverride.override is invoked right after constructor. for example,
 * the following code can override SomeProperty.get <code>
 * OverrideInitializer overrideInit = new OverrideInitializer(new IOverride(){
 *    public void override(){
 *       SomeProperty = new Property<>(){
 *          public PROPERTY_TYPE get(){
 *          }
 *       };
 *    }
 * });
 * </code>
 *
 */
public class OverrideInitializer {
    public OverrideInitializer(IOverride override) {
        override.override();
    }
}
