package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Contact

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val street: String,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String,
    val createdAt: Long
) {
    fun toDomain(): Contact {
        return Contact(
            id = id,
            name = name,
            phone = phone,
            email = email,
            street = street,
            city = city,
            state = state,
            country = country,
            zipCode = zipCode,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(contact: Contact): ContactEntity {
            return ContactEntity(
                id = contact.id,
                name = contact.name,
                phone = contact.phone,
                email = contact.email,
                street = contact.street,
                city = contact.city,
                state = contact.state,
                country = contact.country,
                zipCode = contact.zipCode,
                createdAt = contact.createdAt
            )
        }
    }
}
