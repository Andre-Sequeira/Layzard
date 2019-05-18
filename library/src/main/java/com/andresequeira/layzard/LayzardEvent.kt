package com.andresequeira.layzard

import android.os.Bundle
import android.view.View
import kotlin.reflect.KFunction

data class LayzardEvent<T>(
    val listenerFun: KFunction<T>,
    val impl: LayzardListener<Layzard>.(layzard: Layzard, params: Array<out Any>) -> T
) {

    fun call(listener: LayzardListener<Layzard>, layzard: Layzard, params: Array<out Any>? = null) {
        listener.impl(layzard, params ?: emptyParams)
    }

    companion object {

        val emptyParams = emptyArray<Any>()

        @JvmField
        val PRE_RE_CREATE =
            LayzardEvent(LayzardListener<Layzard>::onPreReCreate) { layzard, _ ->
                onPreReCreate(layzard)
            }

        @JvmField
        val RE_CREATE = LayzardEvent(LayzardListener<Layzard>::onReCreate) { layzard, _ ->
            onReCreate(layzard)
        }

        @JvmField
        val PRE_CREATE = LayzardEvent(LayzardListener<Layzard>::onPreCreate) { layzard, _ ->
            onPreCreate(layzard)
        }

        @JvmField
        val CREATE = LayzardEvent(LayzardListener<Layzard>::onCreate) { layzard, _ ->
            onCreate(layzard)
        }
        @JvmField
        val RESTORE = LayzardEvent(LayzardListener<Layzard>::onRestoreState) { layzard, params ->
            onRestoreState(layzard, params[0] as Bundle)
        }
        @JvmField
        val PRE_CREATE_VIEW =
            LayzardEvent(LayzardListener<Layzard>::onPreCreateView) { layzard, _ ->
                onPreCreateView(layzard)
            }
        @JvmField
        val CREATE_VIEW =
            LayzardEvent(LayzardListener<Layzard>::onCreateView) { layzard, params ->
                onCreateView(layzard, params[0] as View)
            }
        @JvmField
        val PRE_RE_BIND = LayzardEvent(LayzardListener<Layzard>::onPreRebind) { layzard, _ ->
            onPreRebind(layzard)
        }
        @JvmField
        val RE_BIND = LayzardEvent(LayzardListener<Layzard>::onRebind) { layzard, _ ->
            onRebind(layzard)
        }
        @JvmField
        val PRE_BIND = LayzardEvent(LayzardListener<Layzard>::onPreBind) { layzard, _ ->
            onPreBind(layzard)
        }
        @JvmField
        val BIND = LayzardEvent(LayzardListener<Layzard>::onBind) { layzard, _ ->
            onBind(layzard)
        }
        @JvmField
        val BOUND = LayzardEvent(LayzardListener<Layzard>::onBound) { layzard, _ ->
            onBound(layzard)
        }
        @JvmField
        val PRE_UNBIND = LayzardEvent(LayzardListener<Layzard>::onPreUnbind) { layzard, _ ->
            onPreUnbind(layzard)
        }
        @JvmField
        val UNBIND = LayzardEvent(LayzardListener<Layzard>::onUnbind) { layzard, _ ->
            onUnbind(layzard)
        }
        @JvmField
        val PRE_DESTROY_VIEW = LayzardEvent(LayzardListener<Layzard>::onPreDestroyView) { layzard, params ->
            onPreDestroyView(layzard, params[0] as View)
        }
        @JvmField
        val DESTROY_VIEW = LayzardEvent(LayzardListener<Layzard>::onDestroyView) { layzard, params ->
            onDestroyView(layzard, params[0] as View)
        }
        @JvmField
        val SAVE = LayzardEvent(LayzardListener<Layzard>::onSaveState) { layzard, params ->
            onSaveState(layzard, params[0] as Bundle)
        }
        @JvmField
        val PRE_DESTROY = LayzardEvent(LayzardListener<Layzard>::onPreDestroy) { layzard, _ ->
            onPreDestroy(layzard)
        }
        @JvmField
        val DESTROY = LayzardEvent(LayzardListener<Layzard>::onDestroy) { layzard, _ ->
            onDestroy(layzard)
        }
        @JvmField
        val BACK = LayzardEvent(LayzardListener<Layzard>::onHandleBack) { layzard, _ ->
            onHandleBack(layzard)
        }
    }
}