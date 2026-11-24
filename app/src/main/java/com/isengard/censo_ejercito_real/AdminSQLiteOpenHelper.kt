package com.isengard.censo_ejercito_real

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Se encarga de crear y actualizar la base de datos ejercito.db
class AdminSQLiteOpenHelper(
    context: Context,
    name: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    // Solo se ejecuta la primera vez, cuando el archivo .db todavía no existe
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE batallones (" +
                    "nombre TEXT PRIMARY KEY, " +
                    "soldados INTEGER NOT NULL)"
        )
    }

    // Se ejecuta si subimos el número de versión: borra la tabla y la crea de nuevo
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS batallones")
        onCreate(db)
    }
}