package com.example.pc_anonymous.aplicacionfinalkotlink

import java.io.OutputStream

interface OutputMessage

data class BotonUno(val unit: Unit) : OutputMessage
data class BotonDos(val unit: Unit) : OutputMessage
data class BotonTres(val unit: Unit) : OutputMessage
data class BotonCuatro(val unit: Unit) : OutputMessage

val botonUno = BotonUno(Unit)
val botonDos = BotonDos(Unit)
val botonTres = BotonTres(Unit)
val botonCuatro = BotonCuatro(Unit)


fun OutputStream.writeMessage(message: OutputMessage) = write(

        when (message) {
            is BotonUno -> 49
            is BotonDos -> 50
            is BotonTres -> 51
            is BotonCuatro -> 52
            else -> 90
        }
)


private fun speedToByte(speed: Float) = (Math.abs(speed) * 255f).toByte()
