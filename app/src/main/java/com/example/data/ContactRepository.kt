package com.example.data

import com.example.domain.Contact
import com.example.util.DuplicateDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts().map { entities ->
        entities.map { ContactMapper.toDomain(it) }
    }

    val favorites: Flow<List<Contact>> = contactDao.getFavorites().map { entities ->
        entities.map { ContactMapper.toDomain(it) }
    }

    fun getContactById(id: Long): Flow<Contact?> = contactDao.getContactById(id).map { entity ->
        entity?.let { ContactMapper.toDomain(it) }
    }

    suspend fun getContactByIdOnce(id: Long): Contact? {
        val entity = contactDao.getContactByIdOnce(id)
        return entity?.let { ContactMapper.toDomain(it) }
    }

    suspend fun saveContact(contact: Contact): Long {
        val entity = ContactMapper.toEntity(contact)
        return if (contact.id == 0L) {
            contactDao.insertContact(entity)
        } else {
            contactDao.updateContact(entity)
            contact.id
        }
    }

    suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContactById(contact.id)
    }

    suspend fun deleteContactById(id: Long) {
        contactDao.deleteContactById(id)
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        contactDao.updateFavoriteStatus(id, !currentFavorite)
    }

    suspend fun deleteAllContacts() {
        contactDao.deleteAllContacts()
    }

    suspend fun importContacts(contacts: List<Contact>): Int {
        val entities = contacts.map { ContactMapper.toEntity(it.copy(id = 0)) }
        val ids = contactDao.insertAll(entities)
        return ids.size
    }

    suspend fun mergeContacts(primary: Contact, secondary: Contact): Contact {
        val merged = DuplicateDetector.merge(primary, secondary)
        saveContact(merged)
        deleteContactById(secondary.id)
        return merged
    }
}
