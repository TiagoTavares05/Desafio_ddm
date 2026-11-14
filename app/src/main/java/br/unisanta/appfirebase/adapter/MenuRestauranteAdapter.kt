package br.unisanta.appfirebase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.unisanta.appfirebase.R
import br.unisanta.appfirebase.model.ItemMenu
import java.text.NumberFormat
import java.util.Locale

class MenuRestauranteAdapter(
    private val itens: List<ItemMenu>,
    private val onExcluirClick: (ItemMenu) -> Unit
) : RecyclerView.Adapter<MenuRestauranteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeItemRestaurante)
        val txtDescricao: TextView = view.findViewById(R.id.txtDescricaoItemRestaurante)
        val txtPreco: TextView = view.findViewById(R.id.txtPrecoItemRestaurante)
        val btnExcluir: Button = view.findViewById(R.id.btnExcluirItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_restaurante, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        holder.txtNome.text = item.nome
        holder.txtDescricao.text = if (item.descricao.isNotBlank()) item.descricao else "Sem descrição"
        holder.txtPreco.text = moeda.format(item.preco)

        holder.btnExcluir.setOnClickListener {
            onExcluirClick(item)
        }
    }

    override fun getItemCount() = itens.size
}
