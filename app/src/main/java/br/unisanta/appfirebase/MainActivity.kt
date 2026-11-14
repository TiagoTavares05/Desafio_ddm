package br.unisanta.appfirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.unisanta.appfirebase.adapter.MenuClienteAdapter
import br.unisanta.appfirebase.adapter.PedidoClienteAdapter
import br.unisanta.appfirebase.model.ItemMenu
import br.unisanta.appfirebase.model.Pedido
import com.firebase.ui.auth.AuthUI
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val listaMenu = mutableListOf<ItemMenu>()
    private val listaPedidos = mutableListOf<Pedido>()

    private lateinit var menuAdapter: MenuClienteAdapter
    private lateinit var pedidoAdapter: PedidoClienteAdapter
    private lateinit var txtTotalPedido: TextView
    private lateinit var txtMenuVazio: TextView

    private var nomeUsuarioLogado: String = "Cliente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = auth.currentUser
        if (user == null) {
            voltarParaLogin()
            return
        }

        setContentView(R.layout.activity_main)

        nomeUsuarioLogado = if (user.displayName.isNullOrBlank()) "Cliente" else user.displayName!!
        findViewById<TextView>(R.id.txtBoasVindas).text = "Olá, $nomeUsuarioLogado!"
        findViewById<TextView>(R.id.txtEmail).text = "Perfil: Cliente • ${user.email}"

        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nomeFirestore = doc.getString("nome")
                if (!nomeFirestore.isNullOrBlank()) {
                    nomeUsuarioLogado = nomeFirestore
                    findViewById<TextView>(R.id.txtBoasVindas).text = "Olá, $nomeUsuarioLogado!"
                }
            }

        findViewById<Button>(R.id.btnSair).setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener { voltarParaLogin() }
        }

        txtTotalPedido = findViewById(R.id.txtTotalPedido)
        txtMenuVazio = findViewById(R.id.txtMenuVazio)
        val btnFazerPedido = findViewById<Button>(R.id.btnFazerPedido)
        val rvMenu = findViewById<RecyclerView>(R.id.rvMenuCliente)
        val rvPedidos = findViewById<RecyclerView>(R.id.rvPedidosCliente)

        val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        menuAdapter = MenuClienteAdapter(listaMenu) { selecionados ->
            val total = selecionados.sumOf { it.preco }
            txtTotalPedido.text = "Total selecionado (${selecionados.size} itens): ${moeda.format(total)}"
        }
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = menuAdapter

        pedidoAdapter = PedidoClienteAdapter(listaPedidos)
        rvPedidos.layoutManager = LinearLayoutManager(this)
        rvPedidos.adapter = pedidoAdapter

        btnFazerPedido.setOnClickListener {
            val selecionados = menuAdapter.obterItensSelecionados()
            if (selecionados.isEmpty()) {
                Toast.makeText(this, "Selecione ao menos 1 item do menu do restaurante", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val total = selecionados.sumOf { it.preco }
            val itensResumo = selecionados.joinToString(", ") {
                "${it.nome} (${moeda.format(it.preco)})"
            }
            val itensDetalhados = selecionados.map { item ->
                hashMapOf(
                    "itemId" to item.id,
                    "nome" to item.nome,
                    "preco" to item.preco,
                    "restauranteId" to item.restauranteId,
                    "restauranteNome" to item.restauranteNome
                )
            }

            val emailCliente = user.email ?: ""
            val quemPediu = "$nomeUsuarioLogado ($emailCliente)"

            // RF03: Armazena detalhes de cada pedido no Firestore, incluindo quem pediu
            val novoPedido = hashMapOf(
                "clienteId" to user.uid,
                "clienteNome" to nomeUsuarioLogado,
                "clienteEmail" to emailCliente,
                "quemPediu" to quemPediu,
                "itensResumo" to itensResumo,
                "itensDetalhados" to itensDetalhados,
                "quantidadeItens" to selecionados.size,
                "valorTotal" to total,
                "dataPedido" to Timestamp.now()
            )

            btnFazerPedido.isEnabled = false
            db.collection("pedidos").add(novoPedido)
                .addOnSuccessListener {
                    btnFazerPedido.isEnabled = true
                    Toast.makeText(this, "Pedido registrado com sucesso!", Toast.LENGTH_SHORT).show()
                    menuAdapter.limparSelecao()
                }
                .addOnFailureListener { e ->
                    btnFazerPedido.isEnabled = true
                    Toast.makeText(this, "Erro ao fazer pedido: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        ouvirMenuDoRestaurante()
        ouvirMeusPedidos(user.uid)
    }

    private fun ouvirMenuDoRestaurante() {
        db.collection("itens_menu")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                val itens = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(ItemMenu::class.java)?.apply { id = doc.id }
                }.sortedByDescending { it.dataCriacao }

                listaMenu.clear()
                listaMenu.addAll(itens)
                menuAdapter.notifyDataSetChanged()

                txtMenuVazio.visibility = if (listaMenu.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun ouvirMeusPedidos(uid: String) {
        db.collection("pedidos")
            .whereEqualTo("clienteId", uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                val pedidos = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.apply { id = doc.id }
                }.sortedByDescending { it.dataPedido }

                listaPedidos.clear()
                listaPedidos.addAll(pedidos)
                pedidoAdapter.notifyDataSetChanged()
            }
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
