package br.unisanta.appfirebase.model

import com.google.firebase.Timestamp

data class ItemMenu(
    var id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val preco: Double = 0.0,
    val restauranteId: String = "",
    val restauranteNome: String = "",
    val dataCriacao: Timestamp = Timestamp.now()
)
