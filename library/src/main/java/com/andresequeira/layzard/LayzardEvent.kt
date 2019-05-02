package com.andresequeira.layzard

data class LayzardEvent(val name: String) {

    companion object {

        @JvmField
        val PRE_RE_CREATE = LayzardEvent(Layzard::onPreReInit.name)
        @JvmField
        val RE_CREATE = LayzardEvent("LayzardListener#onReCreate")
        @JvmField
        val PRE_CREATE = LayzardEvent("LayzardListener#onPreCreate")
        @JvmField
        val CREATE = LayzardEvent("LayzardListener#onCreate")
        @JvmField
        val RESTORE = LayzardEvent("LayzardListener#onRestoreState")
        @JvmField
        val PRE_CREATE_VIEW = LayzardEvent("LayzardListener#onPreCreateView")
        @JvmField
        val CREATE_VIEW = LayzardEvent("LayzardListener#onCreateView")
        @JvmField
        val PRE_RE_BIND = LayzardEvent("LayzardListener#onPreReBind")
        @JvmField
        val RE_BIND = LayzardEvent("LayzardListener#onReBind")
        @JvmField
        val PRE_BIND = LayzardEvent("LayzardListener#onPreBind")
        @JvmField
        val BIND = LayzardEvent("LayzardListener#onBind")
        @JvmField
        val BOUND = LayzardEvent("LayzardListener#onBound")
        @JvmField
        val PRE_UNBIND = LayzardEvent("LayzardListener#onPreUnbind")
        @JvmField
        val UNBIND = LayzardEvent("LayzardListener#onUnbind")
        @JvmField
        val PRE_DESTROY_VIEW = LayzardEvent("LayzardListener#onPreDestroyView")
        @JvmField
        val DESTROY_VIEW = LayzardEvent("LayzardListener#onDestroyView")
        @JvmField
        val SAVE = LayzardEvent("LayzardListener#onSaveState")
        @JvmField
        val PRE_DESTROY = LayzardEvent("LayzardListener#onPreDestroy")
        @JvmField
        val DESTROY = LayzardEvent("LayzardListener#onDestroy")
        @JvmField
        val BACK = LayzardEvent("LayzardListener#onBack")
    }
}