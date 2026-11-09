package br.edu.up.rastro.data.local

import androidx.room.Embedded

/** Resultado da consulta com JOIN: o local e quantas saídas ele tem. */
data class LocalComTotal(
    @Embedded val local: Local,
    val totalSaidas: Int
)
