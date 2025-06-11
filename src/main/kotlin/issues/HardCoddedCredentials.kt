package com.example

import java.sql.Connection
import java.sql.DriverManager

class DatabaseConnector {

    fun connectToDatabase(): Connection? {
        val dbUrl = "jdbc:mysql://localhost:3306/mydatabase"
        val user = "admin"
        val password = "Password123!"
        return DriverManager.getConnection(dbUrl, user, password)
    }
}

fun main() {
    val connector = DatabaseConnector()
    val connection = connector.connectToDatabase()
    if (connection != null) {
        println("¡Conexión exitosa!")
        connection.close()
    }
}
