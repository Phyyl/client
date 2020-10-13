package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.gui.clickGui.KamiClickGui
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.TimerUtils
import org.lwjgl.input.Keyboard
import kotlin.math.round

@Module.Info(
        name = "ClickGUI",
        description = "Opens the Click GUI",
        showOnArray = Module.ShowOnArray.OFF,
        category = Module.Category.CLIENT,
        alwaysListening = true
)
object ClickGUI : Module() {
    private val scaleSetting = register(Settings.integerBuilder("Scale").withValue(100).withRange(10, 400).build())
    val blur = register(Settings.floatBuilder("BlurRadius").withValue(4.0f).withRange(0f, 32f).withStep(0.5f))

    private var prevScale = scaleSetting.value / 100.0f
    private var scale = prevScale
    private val settingTimer = TimerUtils.StopTimer()

    fun resetScale() {
        scaleSetting.value = 100
        prevScale = 1.0f
        scale = 1.0f
    }

    fun getScaleFactorFloat() = (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0f

    fun getScaleFactor() = (prevScale + (scale - prevScale) * mc.renderPartialTicks) * 2.0

    override fun onUpdate(event: SafeTickEvent) {
        prevScale = scale
        if (settingTimer.stop() > 500L) {
            val diff = scale - getRoundedScale()
            when {
                diff < -0.025 -> scale += 0.025f
                diff > 0.025 -> scale -= 0.025f
                else -> scale = getRoundedScale()
            }
        }
    }

    private fun getRoundedScale(): Float {
        return round((scaleSetting.value / 100.0f) / 0.1f) * 0.1f
    }

    override fun onEnable() {
        if (mc.currentScreen !is KamiClickGui) {
            mc.displayGuiScreen(KamiClickGui)
            KamiEventBus.subscribe(KamiClickGui)
        }
    }

    override fun onDisable() {
        if (mc.currentScreen is KamiClickGui) {
            mc.displayGuiScreen(null)
            KamiEventBus.unsubscribe(KamiClickGui)
        }
    }

    init {
        bind.value.key = Keyboard.KEY_Y
        scaleSetting.settingListener = Setting.SettingListeners {
            settingTimer.reset()
        }
    }
}
