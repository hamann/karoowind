package dev.hamann.karoowind

import io.hammerhead.karooext.KarooExtension
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class KarooWindExtension : KarooExtension("karoowind", BuildConfig.VERSION_NAME) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var karooSystem: KarooSystemService
    private lateinit var headwindManager: HeadwindManager

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        karooSystem = KarooSystemService(applicationContext)
        headwindManager = HeadwindManager(applicationContext)
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        karooSystem.connect { connected ->
            if (connected) {
                Timber.i("Connected to Karoo system")
                startObservingRideData()
            }
        }
        headwindManager.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startObservingRideData() {
        karooSystem.streamDataFlow(DataType.Type.HEART_RATE)
            .onEach { streamState ->
                if (streamState is StreamState.Streaming) {
                    val hr = streamState.dataPoint.singleValue?.toInt() ?: return@onEach
                    Timber.d("Heart rate: $hr bpm")
                    headwindManager.setSpeedFromHeartRate(hr)
                }
            }
            .launchIn(scope)
    }

    override fun onDestroy() {
        headwindManager.disconnect()
        karooSystem.disconnect()
        super.onDestroy()
    }
}
