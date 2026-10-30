package id.cardframe.app.data.contacts

import android.content.Intent
import android.provider.ContactsContract
import id.cardframe.app.domain.model.ContactDraft

object ContactIntents {
    fun insert(draft: ContactDraft): Intent {
        val phones = draft.phones.map { it.trim() }.filter { it.isNotEmpty() }
        val notes = buildList {
            draft.website.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { add(it) }
            phones.drop(3).forEach { add(it) }
        }.joinToString("\n")
        return Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, draft.fullName.trim())
            putExtra(ContactsContract.Intents.Insert.COMPANY, draft.company.trim())
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, draft.jobTitle.trim())
            putExtra(ContactsContract.Intents.Insert.EMAIL, draft.email.lineSequence().firstOrNull()?.trim().orEmpty())
            putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
            if (phones.isNotEmpty()) {
                putExtra(ContactsContract.Intents.Insert.PHONE, phones[0])
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
            }
            if (phones.size > 1) {
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, phones[1])
                putExtra(
                    ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                )
            }
            if (phones.size > 2) {
                putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, phones[2])
                putExtra(
                    ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER,
                )
            }
            putExtra(ContactsContract.Intents.Insert.POSTAL, draft.address.trim())
            putExtra(
                ContactsContract.Intents.Insert.POSTAL_TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK,
            )
            if (notes.isNotBlank()) putExtra(ContactsContract.Intents.Insert.NOTES, notes)
        }
    }
}
