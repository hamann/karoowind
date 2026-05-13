package dev.hamann.karoowind

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class KarooWindExtension : KarooExtension("karoowind", "1.0") {

    private lateinit var karooSystem: KarooSystemService
    private lateinit var headwindManager: HeadwindManager
    private var serviceJob: Job? = null

    override val types: List<DataTypeImpl> by lazy {
        listOf(HeadwindDataType(headwindManager, extension))
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        karooSystem = KarooSystemService(applicationContext)
        headwindManager = HeadwindManager(applicationContext)

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.connect { connected ->
                if (connected) Timber.i("Connected to Karoo system")
            }
            launch {
                karooSystem.streamDataFlow(DataType.Type.HEART_RATE)
                    .mapNotNull { (it as? StreamState.Streaming)?.dataPoint?.singleValue?.toInt() }
                    .collect { hr ->
                        Timber.d("Heart rate: $hr bpm")
                        headwindManager.setSpeedFromHeartRate(hr)
                    }
            }
            headwindManager.connect()
        }
    }

    override fun onBonusAction(actionId: String) {
        when (actionId) {
            "fan-up" -> headwindManager.adjustSpeed(+HeadwindManager.SPEED_STEP)
            "fan-down" -> headwindManager.adjustSpeed(-HeadwindManager.SPEED_STEP)
        }
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        serviceJob = null
        karooSystem.disconnect()
        headwindManager.disconnect()
        super.onDestroy()
    }
}
