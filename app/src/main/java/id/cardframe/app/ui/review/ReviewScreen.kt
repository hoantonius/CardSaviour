package id.cardframe.app.ui.review

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.cardframe.app.R
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
    var saveError by remember { mutableStateOf<String?>(null) }
    var showRaw by remember { mutableStateOf(false) }
    val card = draft
    val saveVcf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-vcard")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = viewModel.vcard() ?: return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Check the card") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clear()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to camera")
                    }
                },
            )
        },
    ) { padding ->
        if (card == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Scan a card first.", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Back to camera")
                }
            }
            return@Scaffold
        }

        val anyLow = FieldKey.entries.any { key ->
            val blank = when (key) {
                FieldKey.Phone -> card.phones.none { it.isNotBlank() }
                FieldKey.Name -> card.fullName.isBlank()
                FieldKey.Title -> card.jobTitle.isBlank()
                FieldKey.Company -> card.company.isBlank()
                FieldKey.Email -> card.email.isBlank()
                FieldKey.Website -> card.website.isBlank()
                FieldKey.Address -> card.address.isBlank()
            }
            card.needsCheck(key, blank)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            preview?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Photo of the business card",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
            if (anyLow) {
                Text(
                    "Highlighted fields need a look.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(auroraPalette().caution),
                )
            }
            DraftField(
                label = "Full name",
                value = card.fullName,
                low = card.needsCheck(FieldKey.Name, card.fullName.isBlank()),
                onValue = { value -> viewModel.edit { it.copy(fullName = value).withConfidence(FieldKey.Name, 1f) } },
            )
            DraftField(
                label = "Job title",
                value = card.jobTitle,
                low = card.needsCheck(FieldKey.Title, card.jobTitle.isBlank()),
                onValue = { value -> viewModel.edit { it.copy(jobTitle = value).withConfidence(FieldKey.Title, 1f) } },
            )
            DraftField(
                label = "Company",
                value = card.company,
                low = card.needsCheck(FieldKey.Company, card.company.isBlank()),
                onValue = { value -> viewModel.edit { it.copy(company = value).withConfidence(FieldKey.Company, 1f) } },
            )
            val phones = card.phones.ifEmpty { listOf("") }
            phones.forEachIndexed { index, phone ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DraftField(
                        label = if (phones.size == 1) "Phone" else "Phone ${index + 1}",
                        value = phone,
                        low = card.needsCheck(FieldKey.Phone, phone.isBlank()),
                        onValue = { value ->
                            val next = phones.toMutableList().also { it[index] = value }
                            viewModel.edit { it.copy(phones = next).withConfidence(FieldKey.Phone, 1f) }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (phones.size > 1) {
                        IconButton(onClick = {
                            val next = phones.toMutableList().also { it.removeAt(index) }
                            viewModel.edit { it.copy(phones = next).withConfidence(FieldKey.Phone, 1f) }
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove phone ${index + 1}")
                        }
                    }
                }
            }
            TextButton(
                onClick = { viewModel.edit { it.copy(phones = phones + "") } },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add phone")
            }
            DraftField(
                label = "Email",
                value = card.email,
                low = card.needsCheck(FieldKey.Email, card.email.isBlank()),
                onValue = { value -> viewModel.edit { it.copy(email = value).withConfidence(FieldKey.Email, 1f) } },
            )
            DraftField(
                label = "Website",
                value = card.website,
                low = card.needsCheck(FieldKey.Website, card.website.isBlank()),
                onValue = { value -> viewModel.edit { it.copy(website = value).withConfidence(FieldKey.Website, 1f) } },
            )
            DraftField(
                label = "Address",
                value = card.address,
                low = card.needsCheck(FieldKey.Address, card.address.isBlank()),
                onValue = { value -> viewModel.edit { it.copy(address = value).withConfidence(FieldKey.Address, 1f) } },
                singleLine = false,
                minLines = 2,
            )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text("Save to phone book")
            }
            saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(
                onClick = { saveVcf.launch(viewModel.fileName()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Download vCard")
            }
            TextButton(
                onClick = onQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Generate QR of this contact")
            }
            TextButton(onClick = { showRaw = !showRaw }) {
                Text(if (showRaw) "Hide text read from the photo" else "Show text read from the photo")
            }
            if (showRaw) {
                Text(
                    card.rawText.ifBlank { "No text." },
                    style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 13.sp, lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                stringResource(R.string.privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun DraftField(
    label: String,
    value: String,
    low: Boolean,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    val palette = auroraPalette()
    val caution = Color(palette.caution)
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
        minLines = minLines,
        textStyle = TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
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
