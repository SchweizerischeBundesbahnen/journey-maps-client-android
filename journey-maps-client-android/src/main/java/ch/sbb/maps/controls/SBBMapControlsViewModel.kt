package ch.sbb.maps.controls

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.sbb.maps.map.SBBMap
import kotlinx.coroutines.launch

internal class SBBMapControlsViewModel(
    private val sbbMap: SBBMap,
) : ViewModel() {
    private var isSatelliteView = MutableLiveData(false)
    internal val isMyLocationEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    internal val isLocationPermissionGranted: MutableLiveData<Boolean> = MutableLiveData(false)

    internal fun toggleSatelliteView() {
        viewModelScope.launch {
            isSatelliteView.value = isSatelliteView.value?.not()
            sbbMap.toggleSatelliteView(isSatelliteView.value!!)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    internal fun toggleMyLocation() {
        isMyLocationEnabled.value = isMyLocationEnabled.value?.not()
        setMyLocationEnabled(isMyLocationEnabled.value!!)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    internal fun setMyLocationEnabled(userLocationEnabled: Boolean) {
        viewModelScope.launch {
            if (userLocationEnabled) {
                isMyLocationEnabled.value = sbbMap.enableMapLocation()
            } else {
                sbbMap.disableMapLocation()
                isMyLocationEnabled.value = false
            }
        }
    }

    internal fun updateMapFloorLayers(selectedFloor: Int) {
        viewModelScope.launch {
            sbbMap.updateSelectedFloor(selectedFloor)
        }
    }

    internal class Factory(
        private val sbbMap: SBBMap,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SBBMapControlsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SBBMapControlsViewModel(sbbMap) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }
}
