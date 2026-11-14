package br.unisanta.appfirebase.model

import com.google.firebase.Timestamp

data class Pedido(
    var id: String = "",
    val clienteId: String = "",
    val clienteNome: String = "",
    val clienteEmail: String = "",
    val quemPediu: String = "",
    val itensResumo: String = "",
    val quantidadeItens: Int = 0,
    val valorTotal: Double = 0.0,
    val dataPedido: Timestamp = Timestamp.now()
)
