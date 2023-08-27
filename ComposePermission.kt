package com.example.testproject

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat


enum class Status{
    INITIAL,GRANTED_ALREADY,DENIED_WITH_RATIONALE,NOT_ASKED,DENIED_WITH_NEVER_ASK
}

@Composable
fun requestMultiplePermission(permissions:List<String>, onChangedStatus:(statusList:Map<String, Status>) -> Unit)
        : ManagedActivityResultLauncher<Array<String>, *> {
    val activity = LocalContext.current.requiredActivity()
    val allStatus = mutableMapOf<String, Status>()
    permissions.forEach {
        val status = if (shouldAskPermission(it, activity)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                Status.DENIED_WITH_RATIONALE
            } else {
                if (PermissionPreferences(activity).isFirstTimeAsking(it)) {
                    Status.NOT_ASKED
                } else {
                    Status.DENIED_WITH_NEVER_ASK
                }
            }
        } else {
            Status.GRANTED_ALREADY
        }
        allStatus[it] = status
    }
    onChangedStatus(allStatus)

    return rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = { result ->
        val permissionsStatus = mutableMapOf<String, Status>()
        result.forEach {
            if (it.value.not()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it.key)) {
                    permissionsStatus[it.key] = Status.DENIED_WITH_RATIONALE
                    PermissionPreferences(activity).firstTimeAsking(it.key,false)
                } else {
                    permissionsStatus[it.key] = Status.DENIED_WITH_NEVER_ASK
                }
            } else {
                permissionsStatus[it.key] = Status.GRANTED_ALREADY
            }
        }
        onChangedStatus(permissionsStatus)

    })
}

@Composable
fun requestPermission(permission: String, onChangedStatus:(status: Status) -> Unit)
        : ManagedActivityResultLauncher<String, *> {
    val activity = LocalContext.current.requiredActivity()

    val initial = if (shouldAskPermission(permission, activity)) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Status.DENIED_WITH_RATIONALE
        } else {
            if (PermissionPreferences(activity).isFirstTimeAsking(permission)) {
                Status.NOT_ASKED
            } else {
                Status.DENIED_WITH_NEVER_ASK
            }
        }
    } else {
        Status.GRANTED_ALREADY
    }
    onChangedStatus(initial)

    return rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted->

        if (isGranted.not()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                onChangedStatus(Status.DENIED_WITH_RATIONALE)
                PermissionPreferences(activity).firstTimeAsking(permission,false)
            } else {
                onChangedStatus(Status.DENIED_WITH_NEVER_ASK)
            }
        } else {
            onChangedStatus(Status.GRANTED_ALREADY)
        }
    })
}


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
private fun shouldAskPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

private fun shouldAskPermission(permission: String, activity: Activity): Boolean {
    if (shouldAskPermission()) {
        val permissionResult = ActivityCompat.checkSelfPermission(activity, permission)
        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            return true
        }
    }
    return false
}




private fun Context.requiredActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}




class PermissionPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences
    private var editor: SharedPreferences.Editor? = null
    private val preference = "permissions_Settings"
    fun firstTimeAsking(permission: String?, isFirstTime: Boolean) {
        doEdit()
        editor?.putBoolean(permission, isFirstTime)
        doCommit()
    }

    fun isFirstTimeAsking(permission: String?): Boolean {
        return sharedPreferences.getBoolean(permission, true)
    }

    private fun doEdit() {
        if (editor == null) {
            editor = sharedPreferences.edit()
        }
    }

    private fun doCommit() {
        if (editor != null) {
            editor?.commit()
            editor = null
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE)
    }
}
