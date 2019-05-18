package com.andresequeira.layzard

import android.content.Intent
import android.os.Bundle
import android.view.View
import java.util.*

interface LayzardListener<in L : Layzard> {

    companion object {
        private val emptyParams = emptyArray<Any>()
    }

    fun onEvent(event: LayzardEvent<*>, layzard: L, params: Array<out Any> = emptyParams): Boolean {
        return false
    }

    fun onPreReCreate(layzard: L) {}

    fun onReCreate(layzard: L) {}

    fun onPreCreate(layzard: L) {}

    fun onCreate(layzard: L) {}

    fun onRestoreState(layzard: L, bundle: Bundle) {}

    fun onPreCreateView(layzard: L) {}

    fun onCreateView(layzard: L, view: View) {}

    fun onPreRebind(layzard: L) {}

    fun onRebind(layzard: L) {}

    fun onPreBind(layzard: L) {}

    fun onBind(layzard: L) {}

    fun onBound(layzard: L) {}

    fun onPreUnbind(layzard: L) {}

    fun onUnbind(layzard: L) {}

    fun onPreDestroyView(layzard: L, view: View) {}

    fun onDestroyView(layzard: L, view: View) {}

    fun onSaveState(layzard: L, bundle: Bundle) {}

    fun onPreDestroy(layzard: L) {}

    fun onDestroy(layzard: L) {}

    fun onActivityResult(layzard: L, requestCode: Int, resultCode: Int, data: Intent?) {}

    fun onHandleBack(layzard: L): Boolean = false
}
