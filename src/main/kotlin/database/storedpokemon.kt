package database

import kotlinx.serialization.Serializable
//classe del porco de dio serializzabile
@Serializable
data class StoredPokemon(
    val species: String,
    val nickname: String,
    val gender: String,
    val shiny: Boolean,
    val level: Int,
    val exp: Int,
    val moves: List<String>,
    val iv: List<Int>,
    val ev: List<Int>,
    val ability: String?,
    val nature: String,
    val aspects: List<String>,
    val forms: String,
    val friendship: Int,
    val heldItem: String
)