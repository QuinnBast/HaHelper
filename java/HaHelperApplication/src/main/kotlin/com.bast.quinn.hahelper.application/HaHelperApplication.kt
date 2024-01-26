package com.bast.quinn.hahelper.application

import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.HaHelperServerConfig


fun main(args: Array<String>) {
    HaHelper(HaHelperServerConfig.load()).start()
    while(true) {
        Thread.sleep(500)
    }
}