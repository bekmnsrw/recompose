package com.bekmnsrw.recomposer.stability

sealed interface Stability {

    data object Stable : Stability

    data class Unstable(val reason: String) : Stability

    data object Unknown : Stability
}
