package com.andresequeira.layzardsampleapp

import com.andresequeira.layzard.Layzard
import com.andresequeira.layzard.wrapper.LayoutActivity

class MainActivity : LayoutActivity<ExampleLayzard>() {

    override fun getInitializer() = Layzard.newInitializer(ExampleLayzard::class.java)

}