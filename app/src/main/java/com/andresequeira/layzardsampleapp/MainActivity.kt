package com.andresequeira.layzardsampleapp

import android.os.Bundle
import com.andresequeira.layzard.Layzard
import com.andresequeira.layzard.LayzardInitializer
import com.andresequeira.layzard.newArgs
import com.andresequeira.layzard.wrapper.LayoutActivity

class MainActivity : LayoutActivity<ExampleLayzard>() {

    override fun getInitializer() = Layzard.newInitializer<ExampleLayzard>()

    fun test() {
        layzard!!.newArgs(Bundle())
    }
}