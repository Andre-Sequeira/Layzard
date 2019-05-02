package com.andresequeira.layzard

import android.content.Intent
import android.os.Bundle
import android.view.View

interface LayzardListener<in L : Layzard> {

    fun onEvent(event: LayzardEvent, layout: L, view: View?, bundle: Bundle?): Boolean {
        return false
    }

    fun onPreReInit(layout: L) {}

    fun onReInit(layout: L) {}

    fun onPreInit(layout: L) {}

    fun onInit(layout: L) {}

    fun onRestoreState(layout: L, instanceState: Bundle) {}

    fun onPreInitUi(layout: L) {}

    fun onInitUi(layout: L, view: View) {}

    fun onPreRebind(layout: L) {}

    fun onRebind(layout: L) {}

    fun onPreBind(layout: L) {}

    fun onBind(layout: L) {}

    fun onBound(layout: L) {}

    fun onPreUnbind(layout: L) {}

    fun onUnbind(layout: L) {}

    fun onPreDestroyUi(layout: L, view: View) {}

    fun onDestroyUi(layout: L, view: View) {}

    fun onSaveState(layout: L, bundle: Bundle) {}

    fun onPreDestroy(layout: L) {}

    fun onDestroy(layout: L) {}

    fun onActivityResult(layout: L, requestCode: Int, resultCode: Int, data: Intent?) {}

    fun onHandleBack(layout: L): Boolean = false
}
