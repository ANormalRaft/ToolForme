package net.anormalraft.toolforme.mixin;

import com.moandjiezana.toml.Toml;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class ToolFormeMixinPlugin implements IMixinConfigPlugin {

    private static boolean shieldCrouchCondition;

    //Code taken from the Adorn mod
    private static final Supplier<Boolean> TRUE = () -> true;

    private static final Map<String, Supplier<Boolean>> CONDITIONS = Map.of("net.anormalraft.toolforme.mixin.MinecraftMixin", () -> shieldCrouchCondition, "net.anormalraft.toolforme.mixin.LocalPlayerMixin", () -> shieldCrouchCondition);

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
    }

    @Override
    public void onLoad(String mixinPackage) {
        try {
            File file = new File("./config/toolforme-common.toml");
            Toml toml = new Toml().read(file);
            shieldCrouchCondition = toml.getBoolean("shieldCrouch");
        } catch (Exception e){
            throw new RuntimeException("Could not find the toolforme-common.toml file in config folder", e);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
