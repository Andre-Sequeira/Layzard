package com.andresequeira.layzard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.andresequeira.layzard.LayzardEvent.Companion.BIND
import com.andresequeira.layzard.LayzardEvent.Companion.BOUND
import com.andresequeira.layzard.LayzardEvent.Companion.CREATE
import com.andresequeira.layzard.LayzardEvent.Companion.CREATE_VIEW
import com.andresequeira.layzard.LayzardEvent.Companion.DESTROY
import com.andresequeira.layzard.LayzardEvent.Companion.DESTROY_VIEW
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_BIND
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_CREATE
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_CREATE_VIEW
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_DESTROY
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_DESTROY_VIEW
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_RE_BIND
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_RE_CREATE
import com.andresequeira.layzard.LayzardEvent.Companion.PRE_UNBIND
import com.andresequeira.layzard.LayzardEvent.Companion.RESTORE
import com.andresequeira.layzard.LayzardEvent.Companion.RE_BIND
import com.andresequeira.layzard.LayzardEvent.Companion.RE_CREATE
import com.andresequeira.layzard.LayzardEvent.Companion.SAVE
import com.andresequeira.layzard.LayzardEvent.Companion.UNBIND
import java.util.*

/**
 * TODO: set result mechanism
 * idea -> receiveResult(Bundle) : Bundle(Int requestData, Bundle result)
 * -> navigator toRequester(Bundle result)
 */
abstract class Layzard @JvmOverloads constructor(args: Bundle? = null) : LayzardListener<Layzard> {

    companion object {

        private var debugEnabled: Boolean = false
        private val debugCyclePrefix = "Cycle: "

        fun enabledLogs(enable: Boolean) {
            debugEnabled = enable
        }

        private val commonListeners = LinkedHashSet<LayzardListener<Layzard>>()

        @SafeVarargs
        fun addCommonListeners(vararg listeners: LayzardListener<Layzard>) {
            for (listener in listeners) {
                commonListeners.add(listener)
            }
        }

        fun <LAYOUT : Layzard> newInitializer(layoutClass: Class<LAYOUT>): LayzardInitializer<LAYOUT> {
            return LayzardInitializer(layoutClass)
        }

        inline fun <reified LAYOUT : Layzard> newInitializer(): LayzardInitializer<LAYOUT> {
            return LayzardInitializer(LAYOUT::class.java)
        }

        private val KEY_BUNDLE = "Layzard.args"
        private val KEY_ID = "Layzard.id"
        private val KEY_LAYOUT_ARGS = "Layzard.args"
        private val KEY_SAVED_STATE = "Layzard.savedState"
        private val KEY_FIRST_BIND = "Layzard.firstBind"
    }

    private var logCycle: Boolean = false

    val isDebugCycleEnabled: Boolean
        get() = debugEnabled && logCycle

    private val requiredArgKeys = LinkedList<String>()

    private var host: Any? = null

    private var context: Context? = null
    var instanceId: String? = null
        private set

    private var instanceState: Bundle? = null

    var view: View? = null
        private set

    var isTop: Boolean = false
        private set
    var isRoot: Boolean = false
        private set

    var isCreated: Boolean = false
        private set
    var isRestore: Boolean = false
        private set

    var isBound: Boolean = false
        private set
    var isFirstBind = true
        private set

    var isArgsRequired: Boolean = false
        private set

    private var recreating: Boolean = false
    var isCreating: Boolean = false
        private set
    var isBinding: Boolean = false
        private set

    private var args: Bundle? = null

    private val pendingPreLifecycleListeners = LinkedHashSet<LayzardListener<Layzard>>()
    private val pendingLifecycleListeners = LinkedHashSet<LayzardListener<Layzard>>()

    private val dispatcher = EventDispatcher<Layzard>()
    private val childrenHandler = ChildrenHandler()

    val activity: Activity?
        get() = context as? Activity
    //endregion

    abstract val layoutResId: Int

    //region RESOURCE HELPER
    val resources: Resources
        get() = getContext()!!.resources

    val children: Map<String, Layzard>
        get() = childrenHandler.layoutsMap

    protected fun logCycle(cycle: String) {
        if (isDebugCycleEnabled) {
            Log.d(javaClass.simpleName, "$debugCyclePrefix $cycle")
        }
    }

    fun setLifecycleLogsEnabled(enable: Boolean) {
        logCycle = enable
    }

    init {
        if (args != null) {
            newArgs<Layzard>(args)
        }
    }

    fun getContext(): Context? {
        return if (view != null) view!!.context else context
    }

    @Suppress("UNCHECKED_CAST")
    fun <L : Layzard> L.addLifecycleListener(listener: LayzardListener<L>): L {
        if (listener === this) {
            return this
        }
        if (dispatcher.isEmpty) {
            pendingLifecycleListeners.add(listener as LayzardListener<Layzard>)
            return this
        }
        this.dispatcher.add(listener as LayzardListener<Layzard>)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    protected fun addPreLifecycleListener(listener: LayzardListener<*>) {
        if (isCreated) {
            return
        }
        pendingPreLifecycleListeners.add(listener as LayzardListener<Layzard>)
    }

    @Suppress("UNCHECKED_CAST")
    fun removeLifecycleListener(listener: LayzardListener<*>) {
        if (listener === this) {
            return
        }
        this.dispatcher.remove(listener as LayzardListener<Layzard>)
    }

    fun setIsTop(top: Boolean): Layzard {
        if (isCreated) {
            return this
        }
        isTop = top
        return this
    }

    fun setIsRoot(root: Boolean): Layzard {
        if (isCreated) {
            return this
        }
        isRoot = root
        return this
    }

    internal fun setHost(host: Any?) {
        if (isCreated || isCreating) {
            throw RuntimeException("This should just be a test but it's here just in case")
        }
        if (host == null) {
            throw RuntimeException("No host provided")
        }
        this.host = host
    }

    @Suppress("UNCHECKED_CAST")
    fun <H> getHost(): H {
        return host as H
    }

    @Suppress("UNCHECKED_CAST")
    fun <H> getTopHost(): H {
        return if (host is Layzard) {

            (host as Layzard).getTopHost<Any>() as H
        } else getHost()
    }

    protected fun requireArgs(vararg keys: String) {
        for (key in keys) {
            requiredArgKeys.add(key)
        }
        isArgsRequired = true
    }

    //region Args

    @Suppress("UNCHECKED_CAST")
    fun <L : Layzard> newArgs(args: Bundle): L {
        if (!isCreated && !isCreating) {
            this.args = args

            return this as L
        }

        for (key in requiredArgKeys) {
            if (!args.containsKey(key)) {
                throw IllegalArgumentException(
                    "Invalid args for layzard: "
                            + javaClass.name + ", missing key: " + key
                )
            }

            val arg = args.get(key)
            if (key.endsWith("ID") && arg is Long) {
                if (arg <= 0) {
                    throw IllegalArgumentException(
                        "Invalid args for layzard: "
                                + javaClass.name + ", invalid id: " + arg + " ,for key: " + key
                    )
                }
            }
        }

        val b = onNewArgs(args)
        if (!b) {
            throw IllegalArgumentException("Invalid args for layzard: " + javaClass.name)
        }
        this.args = args

        reCreate(args)


        return this as L
    }

    fun getArgs(): Bundle {
        return args!!.clone() as Bundle
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getArg(key: String): T {
        return args!!.get(key) as T
    }

    private fun processArgs(args: Bundle?) {
        args?.let { newArgs<Layzard>(it) } ?: if (this.args != null) {
            newArgs<Layzard>(this.args!!)
        }
        if (isArgsRequired && this.args == null) {
            throw IllegalArgumentException(this.javaClass.name + " args required")
        }
    }

    private fun getCheckLayoutArgs(bundle: Bundle?): Bundle {
        return getLayoutArgs(bundle!!) ?: throw RuntimeException("No layzard args inside this bundle.")
    }

    fun getLayoutArgs(bundle: Bundle): Bundle? {
        return bundle.getBundle(KEY_LAYOUT_ARGS)
    }

    fun checkLayoutArgs(bundle: Bundle): Boolean {
        return getLayoutArgs(bundle) != null
    }
    //endregion

    private fun getInstanceStateBundleKey(id: String?): String {
        return String.format("%s_%s", KEY_BUNDLE, id)
    }

    //region ReCreate
    fun reCreate(bundle: Bundle?) {
        dispatch(PRE_RE_CREATE)

        recreating = true
        reCreate1(bundle)

        dispatch(RE_CREATE)
    }

    @CallSuper
    protected fun reCreate1(bundle: Bundle?) {
        val wasBound = isBound
        unbind()
        val view = view
        destroyView()
        destroy()

        val wasInit = isCreated

        if (wasInit) {
            create(context!!, bundle)

            if (view != null) {
                createView(view)
            }

            if (wasBound) {
                bind()
            }
        }
        recreating = false
    }
    //endregion

    //region Create
    fun createWith(outerBundle: Bundle?, context: Context) {
        create(context, getCheckLayoutArgs(outerBundle))
    }

    fun create(context: Context, args: Bundle?) {
        check(isCreated) {
            "Already created"
        }

        checkNotNull(host) {
            "Host was not set"
        }

        if (!recreating) {
            dispatcher.addAll(pendingPreLifecycleListeners)
            dispatcher.add(this)
            dispatcher.add(childrenHandler)
            dispatcher.addAll(pendingLifecycleListeners)
            dispatcher.addAll(commonListeners)
        }
        childrenHandler.init()
        dispatcher.init()

        this.context = context.applicationContext

        //new instance, assign a new id
        if (instanceId == null) {
            instanceId = UUID.randomUUID().toString()
        }

        dispatch(PRE_CREATE)
        isCreating = true

        logCycle("create")

        processArgs(args)
        create()
        dispatch(CREATE)

        restoreInstanceState(instanceState)
        instanceState = null
    }

    @CallSuper
    protected fun create() {
        isCreating = false
        isCreated = true
    }
    //endregion

    //region Save
    fun save(outerBundle: Bundle, id: String?) {
        outerBundle.putBundle(getInstanceStateBundleKey(id), save())
    }

    fun save(): Bundle {
        logCycle("save")

        val layoutBundle = Bundle()
        val instanceState = Bundle()
        saveState(instanceState)

        dispatch(SAVE, instanceState)
        if (instanceState.size() > 0) {
            layoutBundle.putBundle(KEY_SAVED_STATE, instanceState)
        }

        layoutBundle.putBoolean(KEY_FIRST_BIND, isFirstBind)

        layoutBundle.putBundle(KEY_LAYOUT_ARGS, args)
        layoutBundle.putString(KEY_ID, instanceId)

        isRestore = false

        return layoutBundle
    }

    @CallSuper
    protected fun saveState(instanceState: Bundle) {

    }
    //endregion

    //region Restore
    fun restoreWith(outerBundle: Bundle, context: Context, outerInstanceState: Bundle, id: String) {
        restoreWith(outerInstanceState, id)
        createWith(outerBundle, context)
    }

    fun restoreWith(context: Context, layoutArgs: Bundle?, outerInstanceState: Bundle?, id: String?) {
        if (outerInstanceState != null) {
            restoreWith(outerInstanceState, id)
        }
        create(context, layoutArgs)
    }

    fun restoreWith(outerInstanceState: Bundle, id: String?) {
        val bundle = outerInstanceState.getBundle(getInstanceStateBundleKey(id)) ?: throw RuntimeException()
        restore(bundle)
    }

    fun restore(layoutBundle: Bundle) {
        if (isRestore) {
            return
        }
        logCycle("restore")
        instanceId = layoutBundle.getString(KEY_ID)
        isFirstBind = layoutBundle.getBoolean(KEY_FIRST_BIND)

        instanceState = layoutBundle.getBundle(KEY_SAVED_STATE)
        isRestore = true

        if (!isCreated) {
            return
        }

        restoreInstanceState(instanceState)
    }

    private fun restoreInstanceState(instanceState: Bundle?) {
        if (instanceState != null) {
            restoreState(instanceState)
            dispatch(RESTORE, instanceState)
        }
    }

    @CallSuper
    protected fun restoreState(instanceState: Bundle) {

    }

    //endregion

    //region CreateView
    @JvmOverloads
    fun createView(parent: ViewGroup, bind: Boolean = true): View? {
        return createView(LayoutInflater.from(parent.context), parent, bind)
    }

    fun createView(inflater: LayoutInflater, parent: ViewGroup?): View? {
        return createView(inflater, parent, true)
    }

    fun createView(inflater: LayoutInflater, parent: ViewGroup?, performBind: Boolean): View? {
        if (view != null) {
            return view
        }

        if (!isCreated) {
            create(inflater.context, null)
        }

        logCycle("createView")

        dispatch(PRE_CREATE_VIEW)
        createView(inflater.inflate(layoutResId, parent, false))
        dispatch(CREATE_VIEW, view!!)
        if (performBind) {
            bind()
        }
        return view
    }

    @CallSuper
    protected fun createView(v: View) {
        this.view = v
    }
    //endregion

    //region Bind
    fun bind() {
        logCycle("bind")

        if (isBound) {
            return
        }
        dispatch(PRE_BIND)

        isBinding = true
        bind(isFirstBind)

        dispatch(BIND)

        bound(isFirstBind)
        dispatch(BOUND)
        isFirstBind = false
    }

    @CallSuper
    protected fun bind(firstBind: Boolean) {
        isBinding = false
        isBound = true
    }

    @CallSuper
    protected fun bound(firstBind: Boolean) {

    }
    //endregion

    //region Unbind
    fun unbind() {
        if (!isBound) {
            return
        }

        logCycle("unbind")

        dispatch(PRE_UNBIND)
        unbind1()

        dispatch(UNBIND)
    }

    @CallSuper
    protected fun unbind1() {
        isBound = false
    }
    //endregion

    //region DestroyView
    fun destroyView() {
        unbind()

        if (view == null) {
            return
        }

        val view1 = view!!
        dispatch(PRE_DESTROY_VIEW, view1)
        destroyView(view1)
        logCycle("destroyView")
        dispatch(DESTROY_VIEW, view1)
    }

    @CallSuper
    protected fun destroyView(view: View) {
        this.view = null
    }
    //endregion

    //region Destroy
    fun destroy() {
        destroyView()

        if (!isCreated) {
            return
        }

        dispatch(PRE_DESTROY)
        destroy1()
        logCycle("destroy")
        dispatch(DESTROY)

        if (!recreating) {
            this.dispatcher.clear()
        }
    }

    protected fun destroy1() {
        isCreated = false
        context = null
        if (!recreating) {
            host = null
        }
    }

    protected fun onNewArgs(args: Bundle): Boolean {
        return true
    }

    fun handleBack(): Boolean {
        return if (dispatcher.onHandleBack(this)) {
            true
        } else false
    }

    fun getString(@StringRes resId: Int): String {
        return getContext()!!.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getContext()!!.getString(resId, *formatArgs)
    }
    //endregion

    protected fun <L : Layzard> initInside(
        initializer: LayzardInitializer<L>,
        parent: ViewGroup,
        onCreateListener: LayzardListener<L>? = null
    ) {
        initInside(initializer, parent, onCreateListener)
    }

    protected fun <L : Layzard> initInside(
        initializer: LayzardInitializer<L>, @IdRes parentViewId: Int,
        onCreateListener: LayzardListener<L>? = null
    ) {
        initInside(initializer, parentViewId, onCreateListener)
    }

    private fun <L : Layzard> initInside(
        initializer: LayzardInitializer<L>,
        @IdRes parentViewId: Int = 0,
        parent: ViewGroup? = null,
        onCreateListener: LayzardListener<L>? = null
    ) {
        val childHandler = ChildHandler(initializer, parentViewId, parent, onCreateListener)
        childrenHandler.add(childHandler)
    }

    fun getChild(tag: String): Layzard? {
        return childrenHandler[tag]?.layzard
    }

    fun getChild(index: Int): Layzard {
        return childrenHandler[index].layzard
    }

    fun <L : Layzard> getChild(layoutClass: Class<L>): L? {
        return childrenHandler[layoutClass]?.layzard
    }

    private fun dispatch(event: LayzardEvent<*>) {
        dispatcher.dispatch(event, this)
    }

    private fun dispatch(event: LayzardEvent<*>, vararg params: Any) {
        dispatcher.dispatch(event, this, *params)
    }

    override fun onCreate(layzard: Layzard) {
        view!!
    }

    open class EventDispatcher<L : Layzard> : LayzardListener<L> {

        internal var lifecycleListeners = LinkedHashSet<LayzardListener<L>>()
        internal var listeners: ArrayList<LayzardListener<L>>? = null

        private var backup: ArrayList<LayzardListener<L>>? = null

        val isEmpty: Boolean
            get() = lifecycleListeners.isEmpty()

        open fun add(listener: LayzardListener<L>) {
            lifecycleListeners.add(listener)
            listeners?.add(listener)
        }

        fun addAll(items: Collection<LayzardListener<L>>) {
            for (item in items) {
                add(item)
            }
        }

        open fun remove(listener: LayzardListener<L>) {
            lifecycleListeners.remove(listener)
            listeners?.remove(listener)
        }

        open fun clear() {
            lifecycleListeners.clear()
            listeners = null
        }

        fun init() {
            listeners = ArrayList(lifecycleListeners)
        }

        internal fun dispatch(event: LayzardEvent<*>, layout: L, vararg params: Any) {
            val size = listeners!!.size
            for (i in 0 until size) {
                val listener = listeners!![i]
                if (!listener.onEvent(event, layout, params)) {
                    event.call(listener as LayzardListener<Layzard>, layout, params)
                }
            }
        }

        internal fun dispatch(event: LayzardEvent<*>, layout: L) {
            val size = listeners!!.size
            for (i in 0 until size) {
                val listener = listeners!![i]
                if (!listener.onEvent(event, layout)) {
                    event.call(listener as LayzardListener<Layzard>, layout)
                }
            }
        }

        private fun throwLayoutCastException(e: ClassCastException, l: LayzardListener<*>) {
            throw RuntimeException(
                "Listener: " + l + "does not match layzard",
                e
            )
        }

        override fun onEvent(event: LayzardEvent<*>, layzard: L, vararg params: Any): Boolean {
            return false
        }

        override fun onPreReCreate(layzard: L) {
            dispatch(PRE_RE_CREATE, layzard)
        }

        override fun onReCreate(layzard: L) {
            dispatch(RE_CREATE, layzard)
        }

        override fun onPreCreate(layzard: L) {
            dispatch(PRE_CREATE, layzard)
        }

        override fun onCreate(layzard: L) {
            dispatch(CREATE, layzard)
        }

        override fun onPreCreateView(layzard: L) {
            dispatch(PRE_CREATE_VIEW, layzard)
        }

        override fun onCreateView(layzard: L, view: View) {
            dispatch(CREATE_VIEW, layzard, view)
        }

        override fun onPreRebind(layzard: L) {
            dispatch(PRE_RE_BIND, layzard)
        }

        override fun onRebind(layzard: L) {
            dispatch(RE_BIND, layzard)
        }

        override fun onPreBind(layzard: L) {
            dispatch(PRE_BIND, layzard)
        }

        override fun onBind(layzard: L) {
            dispatch(BIND, layzard)
        }

        override fun onBound(layzard: L) {
            dispatch(BOUND, layzard)
        }

        override fun onPreUnbind(layzard: L) {
            dispatch(PRE_UNBIND, layzard)
        }

        override fun onUnbind(layzard: L) {
            dispatch(UNBIND, layzard)
        }

        override fun onPreDestroyView(layzard: L, view: View) {
            dispatch(PRE_DESTROY_VIEW, layzard, view)
        }

        override fun onDestroyView(layzard: L, view: View) {
            dispatch(DESTROY_VIEW, layzard, view)
        }

        override fun onPreDestroy(layzard: L) {
            dispatch(PRE_DESTROY, layzard)
        }

        override fun onDestroy(layzard: L) {
            dispatch(DESTROY, layzard)
        }

        override fun onSaveState(layzard: L, bundle: Bundle) {
            dispatch(SAVE, layzard, bundle)
        }

        override fun onRestoreState(layzard: L, bundle: Bundle) {
            dispatch(RESTORE, layzard, bundle)
        }

        override fun onActivityResult(layzard: L, requestCode: Int, resultCode: Int, data: Intent?) {
            //TODO
        }

        override fun onHandleBack(layzard: L): Boolean {
            for (listener in lifecycleListeners) {
                val b = listener.onEvent(LayzardEvent.BACK, layzard)
                if (!b && listener.onHandleBack(layzard)) {
                    return true
                }
            }
            return false
        }
    }

    inner class ChildrenHandler : EventDispatcher<Layzard>() {

        internal var childrenMap = LinkedHashMap<String, ChildHandler<Layzard>>()


        val layoutsMap: Map<String, Layzard>
            get() {
                val result = HashMap<String, Layzard>()
                for ((key, value) in childrenMap) {
                    result[key] = value.layzard
                }
                return result
            }

        override fun add(listener: LayzardListener<Layzard>) {
            throw UnsupportedOperationException()
        }

        override fun remove(listener: LayzardListener<Layzard>) {
            throw UnsupportedOperationException()
        }

        override fun clear() {
            super.clear()
            childrenMap.clear()
        }

        @Suppress("UNCHECKED_CAST")
        fun add(listener: ChildHandler<*>) {
            val tag = listener.initializer.tag
            val childHandler = childrenMap[tag]
            if (childHandler != null) {
                childHandler.layzard.reCreate(childHandler.initializer.args)
                return
            }
            super.add(listener as LayzardListener<Layzard>)
            listener.initializer.index = childrenMap.size
            childrenMap[tag] = (listener as ChildHandler<Layzard>)
        }

        operator fun get(tag: String): ChildHandler<*>? {
            return childrenMap[tag]
        }

        operator fun get(index: Int): ChildHandler<*> {
            return ArrayList(childrenMap.values)[index]
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <L : Layzard> get(aClass: Class<L>): ChildHandler<L>? {
            for (childHandler in childrenMap.values) {
                if (childHandler.initializer.layzardClass == aClass) {
                    return childHandler as ChildHandler<L>
                }
            }
            return null
        }
    }

    inner class ChildHandler<L : Layzard> private constructor(
        internal val initializer: LayzardInitializer<L>,
        private val parentViewId: Int,
        private var parentView: ViewGroup?
    ) : DefaultLayzardListener<L>() {

        val layzard: L = initializer.newLayzard(this@Layzard)

        constructor(
            initializer: LayzardInitializer<L>,
            @IdRes parentViewId: Int = View.NO_ID,
            parentView: ViewGroup? = null,
            listener: LayzardListener<L>? = null
        ) : this(initializer, parentViewId, parentView) {

            listener?.apply {
                layzard.addLifecycleListener(this)
            }

            syncState()
        }

        private fun syncState() {
            if (this@Layzard.isCreated) {
                onCreate(this@Layzard)
            }
            val view = this@Layzard.view
            if (view != null) {
                onCreateView(this@Layzard, view)
            }
            if (isBound || isBinding) {
                onBind(this@Layzard)
            }
        }

        override fun onCreate(layzard: L) {
            if (this.layzard.isCreated) {
                return
            }
            this.layzard.create(this.layzard.getContext()!!, initializer.args)
        }

        override fun onCreateView(layzard: L, view: View) {
            if (parentView == null) {
                parentView = view.findViewById(parentViewId)
            }
            val childView = this.layzard.createView(parentView!!, false)
            parentView!!.addView(childView)
        }

        override fun onBind(layzard: L) {
            this.layzard.bind()
        }

        override fun onPreUnbind(layzard: L) {
            this.layzard.unbind()
        }

        override fun onPreDestroyView(layzard: L, view: View) {
            parentView!!.removeView(this.layzard.view)
            this.layzard.destroyView()
            parentView = null
        }

        override fun onPreDestroy(layzard: L) {
            this.layzard.destroy()
        }

        override fun onSaveState(layzard: L, bundle: Bundle) {
            this.layzard.save(bundle, initializer.tag)
        }

        override fun onRestoreState(layzard: L, bundle: Bundle) {
            this.layzard.restoreWith(bundle, initializer.tag)
        }

        override fun onHandleBack(layzard: L): Boolean {
            return this.layzard.handleBack()
        }
    }
}

fun <L : Layzard> L.newArgs(args: Bundle): L {
    return newArgs(args)
}