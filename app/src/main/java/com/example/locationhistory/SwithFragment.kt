package com.example.locationhistory

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class SwitchFragment: Fragment() {
    // Check whether you can get your location
    private fun checkLocationAvailable() {
        // Activityがnullなら何もしない
        val act = activity ?: return

        // 端末の設定を確認してもらうためのリクエストオブジェクト
        val checkRequest = LocationSettingsRequest.Builder().addLocationRequest(getLocationRequest()).build()

        // 端末の設定の確認を要求する
        val checkTask = LocationServices.getSettingsClient(act).checkLocationSettings(checkRequest)

        // 設定確認タスクにコールバックを渡す
        checkTask.addOnCompleteListener { response ->
            try {
                response.getResult(ApiException::class.java)
                // 位置情報は取得可能。パーミッション確認に進む。
                checkLocationPermission()
            } catch (exception: ApiException) {
                // 位置情報を取得できないなら例外が投げられる
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    // 解決可能
                    val resolvable = exception as ResolvableApiException
                    resolvable.startResolutionForResult(requireActivity(), 1)
                } else {
                    // 解決不可能
                    showErrorMessage()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // ユーザーが設定変更してくれた
            checkLocationPermission()
        } else {
            // 設定を変更してくれなかった
            showErrorMessage()
        }
    }

    // 位置情報取得のパーミッションを得ているかチェックする（Check whether you have permission to get location.）
    private fun checkLocationPermission() {

    }

    // Show error and  close activity.
    private fun showErrorMessage() {
        Toast.makeText(context, "位置情報を取得できません", Toast.LENGTH_SHORT).show()
        activity?.finish()
    }

    // 位置情報のリクエスト設定を返す
    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = 300000
            fastestInterval = 60000
            priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        return locationRequest
    }
}