package id.cardframe.app.ui.review

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.cardframe.app.R
import id.cardframe.app.domain.model.ContactDraft
import id.cardframe.app.domain.model.FieldKey
import id.cardframe.app.domain.model.needsCheck
import id.cardframe.app.ui.theme.JetBrainsMonoFamily
import id.cardframe.app.ui.theme.auroraPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    onQr: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val card = draft
    var editing by remember { mutableStateOf(false) }
    var local by remember { mutableStateOf<ContactDraft?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showAdvanced by remember { mutableStateOf(false) }
    var askDiscardCard by remember { mutableStateOf(false) }
    var askUnsaved by remember { mutableStateOf(false) }
    var pendingLeave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val working = if (editing) local ?: card else card
    val dirty = editing && local != null && card != null && local != card

    DisposableEffect(editing, dirty) {
        viewModel.gate.onAttemptLeave = { then ->
            if (editing && dirty) {
                pendingLeave = then
                askUnsaved = true
                true
            } else {
                false
            }
        }
        onDispose { viewModel.gate.onAttemptLeave = null }
    }

    BackHandler {
        when {
            editing && dirty -> askUnsaved = true
            editing -> {
                editing = false
                local = null
            }
            card != null -> askDiscardCard = true
            else -> onBack()
        }
    }

    val saveVcf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-vcard")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = viewModel.vcard() ?: return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
        }
    }

    fun abandonEdits(andThen: (() -> Unit)?) {
        editing = false
        local = null
        askUnsaved = false
        val then = andThen
        pendingLeave = null
        then?.invoke()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (editing) "Edit the card" else "The card") },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            editing && dirty -> askUnsaved = true
                            editing -> {
                                editing = false
                                local = null
                            }
                            card != null -> askDiscardCard = true
                            else -> onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!editing && card != null) {
                        IconButton(onClick = {
                            local = card
                            editing = true
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit fields")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (working != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (editing) {
                        Button(
                            onClick = {
                                local?.let(viewModel::commit)
                                editing = false
                                local = null
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        ) { Text("Done") }
                    } else {
                        Button(
                            onClick = {
                                val intent = viewModel.insertIntent() ?: return@Button
                                try {
                                    context.startActivity(intent)
                                    saveError = null
                                } catch (_: ActivityNotFoundException) {
                                    saveError = "No contacts app found. Download the vCard instead."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        ) { Text("Save to phone book") }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { saveVcf.launch(viewModel.fileName()) },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            ) { Text("Download vCard") }
                            OutlinedButton(
                                onClick = onQr,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            ) { Text("QR code") }
                        }
                        saveError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (working == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Scan a card first.", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to camera") }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!editing) {
                preview?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Photo of the business card",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)),
                    )
                }
                ContactView(working)
                TextButton(onClick = { showAdvanced = !showAdvanced }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (showAdvanced) "Hide advanced" else "Advanced")
                }
                if (showAdvanced) {
                    Text("Text read from the photo", style = MaterialTheme.typography.labelLarge)
                    Text(
                        working.rawText.ifBlank { "No text." },
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = { askDiscardCard = true },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("Discard card") }
                Text(
                    stringResource(R.string.privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            } else {
                EditorFields(
                    card = working,
                    onChange = { local = it },
                )
            }
        }
    }

    if (askUnsaved) {
        AlertDialog(
            onDismissRequest = {
                askUnsaved = false
                pendingLeave = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Keep these edits?") },
            text = { Text("You changed this card and have not pressed Done.") },
            confirmButton = {
                TextButton(onClick = {
                    askUnsaved = false
                    pendingLeave = null
                }) { Text("Keep editing") }
            },
            dismissButton = {
                TextButton(onClick = { abandonEdits(pendingLeave) }) { Text("Discard edits") }
            },
        )
    }
    if (askDiscardCard) {
        AlertDialog(
            onDismissRequest = { askDiscardCard = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Discard this card?") },
            text = { Text("The photo leaves memory. You can undo that from the scan screen.") },
            confirmButton = {
                TextButton(onClick = {
                    askDiscardCard = false
                    viewModel.discardCard()
                    onBack()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { askDiscardCard = false }) { Text("Keep") }
            },
        )
    }
}

@Composable
private fun ContactView(card: ContactDraft) {
    val caution = Color(auroraPalette().caution)
    val low = FieldKey.entries.any { card.needsCheck(it, blankOf(card, it)) }
    Text(card.fullName.ifBlank { "No name yet" }, style = MaterialTheme.typography.headlineMedium)
    if (low) {
        Text("Some fields need a look. Edit them before you save.", color = caution, style = MaterialTheme.typography.bodyMedium)
    }
    ValueRow("Job title", card.jobTitle, card.needsCheck(FieldKey.Title, card.jobTitle.isBlank()))
    ValueRow("Company", card.company, card.needsCheck(FieldKey.Company, card.company.isBlank()))
    card.phones.filter { it.isNotBlank() }.forEach { ValueRow("Phone", it, false) }
    if (card.phones.none { it.isNotBlank() }) ValueRow("Phone", "", true)
    ValueRow("Email", card.email, card.needsCheck(FieldKey.Email, card.email.isBlank()))
    ValueRow("Website", card.website, card.needsCheck(FieldKey.Website, card.website.isBlank()))
    ValueRow("Address", card.address, card.needsCheck(FieldKey.Address, card.address.isBlank()))
}

@Composable
private fun ValueRow(label: String, value: String, low: Boolean) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value.ifBlank { "Not found" },
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily),
            color = if (low) Color(auroraPalette().caution) else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EditorFields(card: ContactDraft, onChange: (ContactDraft) -> Unit) {
    val focus = LocalFocusManager.current
    SectionLabel("Person")
    EditorField("Full name", card.fullName, KeyboardType.Text, ImeAction.Next, card.needsCheck(FieldKey.Name, card.fullName.isBlank()), { focus.moveFocus(FocusDirection.Down) }) {
        onChange(card.copy(fullName = it).withConfidence(FieldKey.Name, 1f))
    }
    EditorField("Job title", card.jobTitle, KeyboardType.Text, ImeAction.Next, card.needsCheck(FieldKey.Title, card.jobTitle.isBlank()), { focus.moveFocus(FocusDirection.Down) }) {
        onChange(card.copy(jobTitle = it).withConfidence(FieldKey.Title, 1f))
    }
    EditorField("Company", card.company, KeyboardType.Text, ImeAction.Next, card.needsCheck(FieldKey.Company, card.company.isBlank()), { focus.moveFocus(FocusDirection.Down) }) {
        onChange(card.copy(company = it).withConfidence(FieldKey.Company, 1f))
    }
    SectionLabel("Reach")
    val phones = card.phones.ifEmpty { listOf("") }
    phones.forEachIndexed { index, phone ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            EditorField(
                label = if (phones.size == 1) "Phone" else "Phone ${index + 1}",
                value = phone,
                keyboard = KeyboardType.Phone,
                ime = ImeAction.Next,
                low = card.needsCheck(FieldKey.Phone, phone.isBlank()),
                onNext = { focus.moveFocus(FocusDirection.Down) },
                onValue = { value ->
                    val next = phones.toMutableList().also { it[index] = value }
                    onChange(card.copy(phones = next).withConfidence(FieldKey.Phone, 1f))
                },
                modifier = Modifier.weight(1f),
            )
            if (phones.size > 1) {
                IconButton(onClick = {
                    val next = phones.toMutableList().also { it.removeAt(index) }
                    onChange(card.copy(phones = next).withConfidence(FieldKey.Phone, 1f))
                }) { Icon(Icons.Filled.Close, contentDescription = "Remove phone ${index + 1}") }
            }
        }
    }
    TextButton(
        onClick = { onChange(card.copy(phones = phones + "")) },
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Text("Add phone")
    }
    EditorField("Email", card.email, KeyboardType.Email, ImeAction.Next, card.needsCheck(FieldKey.Email, card.email.isBlank()), { focus.moveFocus(FocusDirection.Down) }) {
        onChange(card.copy(email = it).withConfidence(FieldKey.Email, 1f))
    }
    EditorField("Website", card.website, KeyboardType.Uri, ImeAction.Next, card.needsCheck(FieldKey.Website, card.website.isBlank()), { focus.moveFocus(FocusDirection.Down) }) {
        onChange(card.copy(website = it).withConfidence(FieldKey.Website, 1f))
    }
    SectionLabel("Place")
    EditorField(
        "Address",
        card.address,
        KeyboardType.Text,
        ImeAction.Done,
        card.needsCheck(FieldKey.Address, card.address.isBlank()),
        { focus.clearFocus() },
        singleLine = false,
    ) { onChange(card.copy(address = it).withConfidence(FieldKey.Address, 1f)) }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    keyboard: KeyboardType,
    ime: ImeAction,
    low: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    onValue: (String) -> Unit,
) {
    val caution = Color(auroraPalette().caution)
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        supportingText = if (low) {
            { Text(if (value.isBlank()) "Not found" else "Check this") }
        } else {
            null
        },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = ime),
        keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
        textStyle = MaterialTheme.typography.bodyLarge.merge(
            TextStyle(fontFamily = JetBrainsMonoFamily),
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (low) caution else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (low) caution else MaterialTheme.colorScheme.outline,
            focusedContainerColor = if (low) caution.copy(alpha = 0.10f) else Color.Transparent,
            unfocusedContainerColor = if (low) caution.copy(alpha = 0.08f) else Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

private fun blankOf(card: ContactDraft, key: FieldKey): Boolean = when (key) {
    FieldKey.Phone -> card.phones.none { it.isNotBlank() }
    FieldKey.Name -> card.fullName.isBlank()
    FieldKey.Title -> card.jobTitle.isBlank()
    FieldKey.Company -> card.company.isBlank()
    FieldKey.Email -> card.email.isBlank()
    FieldKey.Website -> card.website.isBlank()
    FieldKey.Address -> card.address.isBlank()
}
