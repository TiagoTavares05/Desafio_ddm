package br.unisanta.appfirebase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.unisanta.appfirebase.R
import br.unisanta.appfirebase.model.Pedido
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PedidoClienteAdapter(
    private val pedidos: List<Pedido>
) : RecyclerView.Adapter<PedidoClienteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtQuemPediu: TextView = view.findViewById(R.id.txtQuemPediu)
        val txtItens: TextView = view.findViewById(R.id.txtItensPedido)
        val txtValor: TextView = view.findViewById(R.id.txtValorPedido)
        val txtData: TextView = view.findViewById(R.id.txtDataPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = pedidos[position]
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        val autor = pedido.quemPediu.ifBlank { "${pedido.clienteNome} (${pedido.clienteEmail})" }
        holder.txtQuemPediu.text = "Quem pediu: $autor"
        holder.txtItens.text = "Itens: ${pedido.itensResumo}"
        holder.txtValor.text = "Total: ${moeda.format(pedido.valorTotal)}"
        holder.txtData.text = "Data: ${sdf.format(pedido.dataPedido.toDate())}"
    }

    override fun getItemCount() = pedidos.size
}
