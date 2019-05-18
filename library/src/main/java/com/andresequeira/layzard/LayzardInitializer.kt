package com.andresequeira.layzard

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import java.util.*

public class LayzardInitializer<L : Layzard> {

    var stack: LayzardInitializer<*>? = null
        private set
    //remove previous stack
    var isRoot: Boolean = false
        private set
    //drop this stack and merge with previous one if it exists
    var isMerge: Boolean = false
        private set

    val layzardClass: Class<L>
    var title: String? = null
        private set
    var args: Bundle? = null
        private set

    private var customTag: String? = null

    internal val tag: String
        get() = getTag()

    var index: Int? = null
        internal set

    var cache: L? = null
        private set

    val stackList: LinkedList<LayzardInitializer<*>>
        get() {

            val initializers = LinkedList<LayzardInitializer<*>>()

            initializers.add(this)

            var stack = this.stack
            while (stack != null) {
                initializers.addFirst(stack)
                stack = stack.stack
            }

            return initializers
        }

    constructor(layzardClass: Class<L>) {
        this.layzardClass = layzardClass
    }

    fun newLayzardCache(host: Any) {
        cache = newLayzard(host)
    }

    fun newLayzard(host: Any): L {
        val layzard = newLayzard()
        layzard.setHost(host)
        return layzard
    }

    protected fun newLayzard(): L {
        try {
            return layzardClass.newInstance()
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }

    }

    //region Parcelable

    constructor(`in`: Parcel, loader: ClassLoader? = null) {
        val parcelable = `in`.readParcelable<Parcelable>(loader)
        if (parcelable != null) {
            stack = unwrap<Layzard>(parcelable)
        }
        isRoot = `in`.readByte().toInt() != 0
        isMerge = `in`.readByte().toInt() != 0

        title = `in`.readString()
        args = `in`.readBundle(loader)
        customTag = `in`.readString()
        if (`in`.readByte().toInt() == 0) {
            index = null
        } else {
            index = `in`.readInt()
        }
        val className = `in`.readString()
        //            if (loader == null) {
        //                return;
        //            }
        try {

            layzardClass = javaClass.classLoader!!
                .loadClass(className) as Class<L>
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

    }

    fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(if (stack != null) stack!!.parcelable() else null, flags)
        dest.writeByte((if (isRoot) 1 else 0).toByte())
        dest.writeByte((if (isMerge) 1 else 0).toByte())

        dest.writeString(title)
        dest.writeBundle(args)
        dest.writeString(customTag)
        if (index == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(index!!)
        }
        dest.writeString(layzardClass.name)
    }

    fun popCache(): L? {
        val tmp = cache
        cache = null
        return tmp
    }

    private class P : Parcelable {

        internal var initializer: LayzardInitializer<*>

        constructor(initializer: LayzardInitializer<*>) {
            this.initializer = initializer
        }

        constructor(`in`: Parcel, loader: ClassLoader?) {
            initializer = LayzardInitializer<Layzard>(`in`, loader)
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            initializer.writeToParcel(dest, flags)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<P> = object : Parcelable.ClassLoaderCreator<P> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader): P {
                    return P(source, loader)
                }

                override fun createFromParcel(`in`: Parcel): P {
                    return P(`in`, null)
                }

                override fun newArray(size: Int): Array<P?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
    //endregion

    fun title(title: String): LayzardInitializer<L> {
        this.title = title
        return this
    }

    fun tag(tag: String): LayzardInitializer<L> {
        this.customTag = tag
        return this
    }

    fun args(args: Bundle): LayzardInitializer<L> {
        this.args = args
        return this
    }

    fun stack(initializer: LayzardInitializer<*>): LayzardInitializer<L> {
        this.stack = initializer
        return this
    }

    fun root(): LayzardInitializer<L> {
        this.isRoot = true
        return this
    }

    fun merge(): LayzardInitializer<L> {
        this.isMerge = true
        return this
    }

    private fun getTag(): String {
        customTag?.let {
            return it
        }
        val builder = StringBuilder()
        index?.let {
            builder.append(it)
            builder.append(":")
        }
        title?.let {
            builder.append(title)
            builder.append(":")
        }
        builder.append(layzardClass.name)
        return builder.toString()
    }

    override fun toString(): String {
        return "Initializer(layzardClass=" + this.layzardClass + ", title=" + this.title + ", args=" + this.args + ")"
    }

    fun parcelable(): Parcelable {
        return P(this)
    }

    private class Unwrapper {

        private val map = HashMap<Class<*>, (Parcelable) -> LayzardInitializer<*>>()

        internal fun add(aClass: Class<*>, function: (Parcelable) -> LayzardInitializer<*>) {
            map[aClass] = function
        }

        @Suppress("UNCHECKED_CAST")
        internal fun <LAYOUT : Layzard> unwrap(parcelable: Parcelable): LayzardInitializer<LAYOUT> {
            for ((key, value) in map) {
                if (key.isAssignableFrom(parcelable.javaClass)) {
                    return value(parcelable) as LayzardInitializer<LAYOUT>
                }
            }
            return parcelable as LayzardInitializer<LAYOUT>
        }

    }

    companion object {

        @JvmStatic
        fun newList(vararg items: LayzardInitializer<*>): ArrayList<LayzardInitializer<*>> {
            val list = ArrayList<LayzardInitializer<*>>(items.size)
            for (item in items) {
                list.add(item)
            }
            return newList(list)
        }

        @JvmStatic
        fun newList(items: Collection<LayzardInitializer<*>>): ArrayList<LayzardInitializer<*>> {
            val list = ArrayList(items)
            for (i in list.indices) {
                list[i].index = i
            }
            return list
        }

        private val unwrapper = Unwrapper()

        init {
            unwrapper.add(P::class.java) {
                (it as P).initializer
            }
        }

        @JvmStatic
        fun addUnwrapper(instanceOfClass: Class<*>, function: (Parcelable) -> LayzardInitializer<*>) {
            unwrapper.add(instanceOfClass, function)
        }

        @JvmStatic
        fun <LAYOUT : Layzard> unwrap(parcelable: Parcelable): LayzardInitializer<LAYOUT> {
            return unwrapper.unwrap(parcelable)
        }

        @JvmName("unwrapMultiple")
        @JvmStatic
        fun <L : Layzard> unwrap(parcelables: Collection<Parcelable>?): ArrayList<LayzardInitializer<L>>? {
            if (parcelables == null) {
                return null
            }
            val initializers =
                ArrayList<LayzardInitializer<L>>(parcelables.size)
            for (parcelable in parcelables) {
                initializers.add(unwrap(parcelable))
            }
            return initializers
        }

        @JvmStatic
        fun wrap(initializers: Collection<LayzardInitializer<*>>): ArrayList<Parcelable> {
            val parcelables = ArrayList<Parcelable>(initializers.size)
            for (initializer in initializers) {
                parcelables.add(initializer.parcelable())
            }
            return parcelables
        }
    }
}