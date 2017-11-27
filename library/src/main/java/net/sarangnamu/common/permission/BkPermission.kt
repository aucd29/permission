package net.sarangnamu.common.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 11. 23.. <p/>
 *
 * ```kotlin
 * context.mainRuntimePermission(arrayListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), { res ->
        Log.e("PERMISSION", "res = $res")
   }
 * ```
 * ```kotlin
 * context.runtimePermission(arrayListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), { res ->
        Log.e("PERMISSION", "res = $res")
   }
 * ```
 */

private val KEY_PERMISSION: String
    get() = "permission"
private val KEY_PERMISSION_SHOW_DIALOG: String
    get() = "permission_show_dialog"

////////////////////////////////////////////////////////////////////////////////////
//
// PermissionActivity
//
////////////////////////////////////////////////////////////////////////////////////

class PermissionActivity : AppCompatActivity() {
    companion object {
        lateinit var listener: (Boolean) -> Unit
        var userDialog: AlertDialog? = null
    }

    var permissions: ArrayList<String>? = null
    var requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissions = intent.getStringArrayListExtra(KEY_PERMISSION)
        if (!intent.getBooleanExtra(KEY_PERMISSION_SHOW_DIALOG, false)) {
            requestCode = 0
        }

        permissions?.let { checkPermission() } ?: finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var grantRes = true
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                grantRes = false
                break
            }
        }

        listener(grantRes)

        if (!grantRes && requestCode == 1) {
            showDialog()
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        listener(checkRuntimePermissions(permissions!!))

        when (requestCode) {
            1 -> showDialog()
            else -> finish()
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            ActivityCompat.requestPermissions(this@PermissionActivity, permissions!!.toTypedArray(), requestCode)
        }
    }

    fun showDialog() {
        userDialog?.run { show() } ?: dialog()
    }

    fun dialog() {
        AlertDialog.Builder(this@PermissionActivity).apply {
            setTitle(R.string.permission_title)
            setMessage(R.string.permission_message)
            setCancelable(false)
            setPositiveButton(android.R.string.ok, { d, w ->
                d.dismiss()
                finish()
            })
            setNegativeButton(R.string.permission_setting, { d, w ->
                startActivityForResult(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    setData(Uri.parse("package:$packageName"))
                }, 0)

                d.dismiss()
                finish()
            })
        }.show()
    }
}

////////////////////////////////////////////////////////////////////////////////////
//
// METHODS
//
////////////////////////////////////////////////////////////////////////////////////

fun Context.mainRuntimePermission(permissions: ArrayList<String>, listener: (Boolean) -> Unit) {
    runtimePermissions(false, permissions, listener)
}

fun Context.runtimePermission(permissions: ArrayList<String>, listener: (Boolean) -> Unit) {
    runtimePermissions(true, permissions, listener)
}

private fun Context.runtimePermissions(showDialog: Boolean, permissions: ArrayList<String>, listener: (Boolean) -> Unit) {
    if (!checkRuntimePermissions(permissions)) {
        PermissionActivity.listener = listener
        startActivity(Intent(this, PermissionActivity::class.java).apply {
            putStringArrayListExtra(KEY_PERMISSION, permissions)
            putExtra(KEY_PERMISSION_SHOW_DIALOG, showDialog)
        })
    } else {
        listener(true)
    }
}

private fun Context.checkRuntimePermissions(permissions: ArrayList<String>): Boolean {
    var result = true

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                result = false
                break;
            }
        }
    }

    return result
}
