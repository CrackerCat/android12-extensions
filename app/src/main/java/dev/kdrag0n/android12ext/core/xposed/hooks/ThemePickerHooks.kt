package dev.kdrag0n.android12ext.core.xposed.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage
import dev.kdrag0n.android12ext.core.xposed.hookMethod
import dev.kdrag0n.android12ext.monet.colors.Srgb
import dev.kdrag0n.android12ext.monet.theme.DynamicColorScheme
import dev.kdrag0n.android12ext.monet.theme.MaterialYouTargets
import java.util.concurrent.atomic.AtomicInteger

class ThemePickerHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
) {
    private val lastCamColor = AtomicInteger()

    fun applyColorScheme(chromaMultiplier: Float, accurateShades: Boolean) {
        val hook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                lastCamColor.set(param.args[0] as Int)
            }
        }
        lpparam.hookMethod(
            "com.android.internal.graphics.cam.Cam",
            hook,
            "fromInt",
            Int::class.java,
        )

        val hook2 = object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam): IntArray {
                val chroma = param.args[1] as Float
                val theme = DynamicColorScheme(
                    targets = MaterialYouTargets(chromaMultiplier),
                    seedColor = Srgb(lastCamColor.get()),
                    chromaFactor = chromaMultiplier,
                    accurateShades = accurateShades,
                )

                val swatch = when {
                    chroma >= 48.0f -> theme.accent1
                    chroma == 16.0f -> theme.accent2
                    chroma == 32.0f -> theme.accent3
                    chroma == 4.0f -> theme.neutral1
                    chroma == 8.0f -> theme.neutral2
                    else -> error("Unknown chroma $chroma")
                }

                return swatch.entries
                    .sortedBy { it.key }
                    .map { it.value.toLinearSrgb().toSrgb().quantize8() or (0xff shl 24) }
                    .toIntArray()
            }
        }
        lpparam.hookMethod(
            "com.google.material.monet.Shades",
            hook2,
            "of",
            Float::class.java,
            Float::class.java,
        )
    }
}
