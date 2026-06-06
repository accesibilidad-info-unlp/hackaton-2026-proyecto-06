package com.sindicato.aplicacionaccesible.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    suspend fun seedDatabase(dao: SignLanguageDao) {
        withContext(Dispatchers.IO) {
            if (dao.getItemCount() == 0) {
                val initialData = listOf(
                    SignLanguageEntity(word = "Hola", imageName = "hola"),
                    SignLanguageEntity(word = "Yo", imageName = "yo"),
                    SignLanguageEntity(word = "Gracias", imageName = "gracias"),
                    SignLanguageEntity(word = "De nada", imageName = "denada"),
                    SignLanguageEntity(word = "Chau", imageName = "chau"),
                    SignLanguageEntity(word = "Saludar", imageName = "saludar"),
                    SignLanguageEntity(word = "Nombre", imageName = "nombre"),
                    SignLanguageEntity(word = "Apellido", imageName = "apellido"),
                    SignLanguageEntity(word = "Interprete", imageName = "interprete"),
                    SignLanguageEntity(word = "Oyente", imageName = "oyente"),
                    SignLanguageEntity(word = "Buenos Días", imageName = "buenosdias"),
                    SignLanguageEntity(word = "Buenas Tardes", imageName = "buenastardes"),
                    SignLanguageEntity(word = "Buenas Noches", imageName = "buenasnoches"),
                    SignLanguageEntity(word = "Sordo/a", imageName = "sordo")
                )
                dao.insertAll(initialData)
            }
        }
    }
}
