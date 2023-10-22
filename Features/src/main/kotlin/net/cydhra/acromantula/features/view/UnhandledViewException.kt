package net.cydhra.acromantula.features.view

import java.lang.RuntimeException

class UnhandledViewException(generatorType: String, fileName: String) : RuntimeException("view generator " +
        "\"$generatorType\" does not handle \"$fileName\"")