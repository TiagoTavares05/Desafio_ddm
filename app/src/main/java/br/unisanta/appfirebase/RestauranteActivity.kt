package br.unisanta.appfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.unisanta.appfirebase.adapter.MenuRestauranteAdapter
import br.unisanta.appfirebase.model.ItemMenu
import com.firebase.ui.auth.AuthUI
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RestauranteActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val listaItens = mutableListOf<ItemMenu>()
    private lateinit var adapter: MenuRestauranteAdapter
    private var nomeRestaurante: String = "Restaurante"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = auth.currentUser
        if (user == null) {
            voltarParaLogin()
            return
        }

        setContentView(R.layout.activity_restaurante)

        nomeRestaurante = if (user.displayName.isNullOrBlank()) "Restaurante" else user.displayName!!
        findViewById<TextView>(R.id.txtNomeRestaurante).text = "Painel: $nomeRestaurante"
        findViewById<TextView>(R.id.txtEmailRestaurante).text = "Perfil: Restaurante • ${user.email}"

        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val nomeFirestore = doc.getString("nome")
                if (!nomeFirestore.isNullOrBlank()) {
                    nomeRestaurante = nomeFirestore
                    findViewById<TextView>(R.id.txtNomeRestaurante).text = "Painel: $nomeRestaurante"
                }
            }

        findViewById<Button>(R.id.btnSairRestaurante).setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener { voltarParaLogin() }
        }

        val tilNomeItem = findViewById<TextInputLayout>(R.id.tilNomeItem)
        val tilPrecoItem = findViewById<TextInputLayout>(R.id.tilPrecoItem)
        val edtNomeItem = findViewById<TextInputEditText>(R.id.edtNomeItem)
        val edtDescricaoItem = findViewById<TextInputEditText>(R.id.edtDescricaoItem)
        val edtPrecoItem = findViewById<TextInputEditText>(R.id.edtPrecoItem)
        val btnCriarItem = findViewById<Button>(R.id.btnCriarItem)
        val rvItens = findViewById<RecyclerView>(R.id.rvItensRestaurante)

        adapter = MenuRestauranteAdapter(listaItens) { item ->
            excluirItem(item)
        }
        rvItens.layoutManager = LinearLayoutManager(this)
        rvItens.adapter = adapter

        btnCriarItem.setOnClickListener {
            val nomeItem = edtNomeItem.text.toString().trim()
            val descricaoItem = edtDescricaoItem.text.toString().trim()
            val precoTexto = edtPrecoItem.text.toString().trim().replace(",", ".")

            tilNomeItem.error = null
            tilPrecoItem.error = null

            val preco = precoTexto.toDoubleOrNull()

            when {
                nomeItem.isEmpty() -> tilNomeItem.error = "Informe o nome do item"
                preco == null || preco <= 0.0 -> tilPrecoItem.error = "Informe um preço válido (ex: 25.50)"
                else -> {
                    val novoItem = hashMapOf(
                        "nome" to nomeItem,
                        "descricao" to descricaoItem,
                        "preco" to preco,
                        "restauranteId" to user.uid,
                        "restauranteNome" to nomeRestaurante,
                        "dataCriacao" to Timestamp.now()
                    )

                    btnCriarItem.isEnabled = false
                    db.collection("itens_menu").add(novoItem)
                        .addOnSuccessListener {
                            btnCriarItem.isEnabled = true
                            Toast.makeText(this, "Item adicionado ao menu!", Toast.LENGTH_SHORT).show()
                            edtNomeItem.text?.clear()
                            edtDescricaoItem.text?.clear()
                            edtPrecoItem.text?.clear()
                        }
                        .addOnFailureListener { e ->
                            btnCriarItem.isEnabled = true
                            Toast.makeText(this, "Erro ao criar item: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }

        ouvirItensDoRestaurante(user.uid)
    }

    private fun ouvirItensDoRestaurante(restauranteUid: String) {
        db.collection("itens_menu")
            .whereEqualTo("restauranteId", restauranteUid)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                val itens = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(ItemMenu::class.java)?.apply { id = doc.id }
                }.sortedByDescending { it.dataCriacao }

                listaItens.clear()
                listaItens.addAll(itens)
                adapter.notifyDataSetChanged()
            }
    }

    private fun excluirItem(item: ItemMenu) {
        if (item.id.isBlank()) return
        db.collection("itens_menu").document(item.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Item removido do menu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
