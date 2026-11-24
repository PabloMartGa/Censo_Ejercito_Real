package com.isengard.censo_ejercito_real

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etSoldados: EditText
    private lateinit var btnInsertar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnConsultar: Button
    private lateinit var tvConsola: TextView

    private lateinit var helper: AdminSQLiteOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNombre = findViewById(R.id.etNombreBatallon)
        etSoldados = findViewById(R.id.etNumeroSoldados)
        btnInsertar = findViewById(R.id.btnInsertarBD)
        btnEliminar = findViewById(R.id.btnEliminarBD)
        btnConsultar = findViewById(R.id.btnConsultarBD)
        tvConsola = findViewById(R.id.tvConsolaBD)

        // Base de datos ejercito.db, versión 1
        helper = AdminSQLiteOpenHelper(this, "ejercito.db", null, 1)

        btnInsertar.setOnClickListener {
            insertarBatallon()
        }

        btnEliminar.setOnClickListener {
            eliminarBatallon()
        }

        btnConsultar.setOnClickListener {
            consultarBatallones()
        }


        // Al abrir la app ya mostramos lo que haya guardado
        consultarBatallones()
    }

    // Alta
    private fun insertarBatallon() {
        val nombre = etNombre.text.toString().trim()
        val soldadosTexto = etSoldados.text.toString().trim()

        // Comprobamos los campos antes de tocar la base de datos
        if (nombre.isEmpty() || soldadosTexto.isEmpty()) {
            Toast.makeText(this, "Rellena el nombre y los efectivos", Toast.LENGTH_SHORT).show()
            return
        }

        // toIntOrNull evita que la app pete si el número es demasiado grande
        val soldados = soldadosTexto.toIntOrNull()
        if (soldados == null) {
            etSoldados.error = "Número de efectivos no válido"
            return
        }

        val db = helper.writableDatabase
        var resultado = -1L
        try {
            val registro = ContentValues().apply {
                put("nombre", nombre)
                put("soldados", soldados)
            }
            // Si el nombre ya existe, REPLACE sustituye la fila
            resultado = db.insertWithOnConflict(
                "batallones",
                null,
                registro,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        } catch (e: SQLiteException) {
            Log.e("Censo", "Error al insertar", e)
        } finally {
            db.close()
        }

        if (resultado != -1L) {
            Toast.makeText(this, "Batallón registrado", Toast.LENGTH_SHORT).show()
            etNombre.text.clear()
            etSoldados.text.clear()
            consultarBatallones()
        } else {
            Toast.makeText(this, "Error al guardar el batallón", Toast.LENGTH_SHORT).show()
        }
    }

    // Baja
    private fun eliminarBatallon() {
        val nombre = etNombre.text.toString().trim()

        if (nombre.isEmpty()) {
            etNombre.error = "Escribe el batallón que quieres dar de baja"
            return
        }

        val db = helper.writableDatabase
        var filasBorradas = 0
        try {
            // El ? se rellena con arrayOf(nombre), así no se concatena texto en el SQL
            filasBorradas = db.delete("batallones", "nombre = ?", arrayOf(nombre))
        } catch (e: SQLiteException) {
            Log.e("Censo", "Error al borrar", e)
        } finally {
            db.close()
        }

        etNombre.text.clear()
        etSoldados.text.clear()

        // delete() devuelve cuántas filas ha borrado
        if (filasBorradas > 0) {
            Toast.makeText(this, "Batallón dado de baja", Toast.LENGTH_SHORT).show()
            consultarBatallones()
        } else {
            Toast.makeText(this, "Ese batallón no existe", Toast.LENGTH_SHORT).show()
        }
    }

    // Consulta
    private fun consultarBatallones() {
        val db = helper.readableDatabase
        var cursor: Cursor? = null
        val texto = StringBuilder()
        texto.append("REPORTE DE FUERZAS\n")
        texto.append("==================\n\n")

        try {
            cursor = db.rawQuery(
                "SELECT nombre, soldados FROM batallones ORDER BY soldados DESC",
                null
            )

            // moveToFirst() devuelve false si la tabla está vacía
            if (cursor.moveToFirst()) {
                do {
                    val nombre = cursor.getString(0)   // columna 0: nombre
                    val soldados = cursor.getInt(1)    // columna 1: soldados
                    texto.append("• $nombre -> Efectivos: $soldados\n")
                } while (cursor.moveToNext())
            } else {
                texto.append("No hay batallones registrados.")
            }
        } catch (e: SQLiteException) {
            Log.e("Censo", "Error al consultar", e)
            texto.append("Error al leer la base de datos.")
        } finally {
            // Se cierra siempre, aunque haya fallado algo antes
            cursor?.close()
            db.close()
        }

        tvConsola.text = texto.toString()
    }
}