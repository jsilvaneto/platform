package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.ContactType

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String = "FORNECEDOR",
    val phone: String,
    val email: String,
    val street: String,
    val number: String = "",
    val complement: String = "",
    val neighborhood: String = "",
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
            type = ContactType.fromString(type),
            phone = phone,
            email = email,
            street = street,
            number = number,
            complement = complement,
            neighborhood = neighborhood,
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
                type = contact.type.name,
                phone = contact.phone,
                email = contact.email,
                street = contact.street,
                number = contact.number,
                complement = contact.complement,
                neighborhood = contact.neighborhood,
                city = contact.city,
                state = contact.state,
                country = contact.country,
                zipCode = contact.zipCode,
                createdAt = contact.createdAt
            )
        }
    }
}
