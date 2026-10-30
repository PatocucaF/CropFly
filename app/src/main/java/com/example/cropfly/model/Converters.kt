package com.example.cropfly.model

import androidx.room.TypeConverter

/** Room solo guarda tipos simples, así que las listas de texto se guardan separadas por "|". */
class Converters {
    @TypeConverter
    fun listaATexto(lista: List<String>): String = lista.joinToString("|")

    @TypeConverter
    fun textoALista(texto: String): List<String> =
        if (texto.isEmpty()) emptyList() else texto.split("|")
}
