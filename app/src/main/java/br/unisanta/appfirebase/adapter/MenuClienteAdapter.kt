package br.unisanta.appfirebase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.unisanta.appfirebase.R
import br.unisanta.appfirebase.model.ItemMenu
import java.text.NumberFormat
import java.util.Locale

class MenuClienteAdapter(
    private val itens: List<ItemMenu>,
    private val onSelecaoAlterada: (List<ItemMenu>) -> Unit
) : RecyclerView.Adapter<MenuClienteAdapter.ViewHolder>() {

    private val idsSelecionados = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbSelecionar: CheckBox = view.findViewById(R.id.cbSelecionarItem)
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricaoItemCliente)
        val txtRestaurante: TextView = view.findViewById(R.id.txtRestauranteOrigem)
        val txtPreco: TextView = view.findViewById(R.id.txtPrecoItemCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_cliente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        holder.cbSelecionar.setOnCheckedChangeListener(null)
        holder.cbSelecionar.text = item.nome
        holder.cbSelecionar.isChecked = idsSelecionados.contains(item.id)

        holder.txtDescricao.text = if (item.descricao.isNotBlank()) item.descricao else "Item do cardápio"
        holder.txtRestaurante.text = "Restaurante: ${item.restauranteNome}"
        holder.txtPreco.text = moeda.format(item.preco)

        holder.cbSelecionar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                idsSelecionados.add(item.id)
            } else {
                idsSelecionados.remove(item.id)
            }
            onSelecaoAlterada(obterItensSelecionados())
        }
    }

    override fun getItemCount() = itens.size

    fun obterItensSelecionados(): List<ItemMenu> {
        return itens.filter { idsSelecionados.contains(it.id) }
    }

    fun limparSelecao() {
        idsSelecionados.clear()
        notifyDataSetChanged()
        onSelecaoAlterada(emptyList())
    }
}
