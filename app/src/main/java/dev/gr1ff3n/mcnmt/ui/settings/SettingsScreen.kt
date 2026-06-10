package dev.gr1ff3n.mcnmt.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gr1ff3n.mcnmt.R
import dev.gr1ff3n.mcnmt.data.settings.MileageProfile
import dev.gr1ff3n.mcnmt.ui.components.GlassCard
import dev.gr1ff3n.mcnmt.ui.components.MileageScaffold
import dev.gr1ff3n.mcnmt.ui.components.mileageFieldColors
import dev.gr1ff3n.mcnmt.ui.theme.AccentOrange
import dev.gr1ff3n.mcnmt.ui.theme.SleekText
import dev.gr1ff3n.mcnmt.ui.theme.SleekTextDim

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    MileageScaffold(title = "Settings", onBack = onBack) { padding ->
        SettingsBody(
            profile = profile,
            modifier = Modifier.padding(padding),
            onSave = { viewModel.save(it); onBack() },
        )
    }
}

@Composable
private fun SettingsBody(
    profile: MileageProfile,
    modifier: Modifier = Modifier,
    onSave: (MileageProfile) -> Unit,
) {
    var name by remember(profile) { mutableStateOf(profile.employeeName) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var department by remember(profile) { mutableStateOf(profile.department) }
    var account by remember(profile) { mutableStateOf(profile.accountNumber) }
    var title by remember(profile) { mutableStateOf(profile.travelerTitle) }
    var rateText by remember(profile) { mutableStateOf(profile.ratePerMile.toString()) }
    var rateLabel by remember(profile) { mutableStateOf(profile.rateLabel) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LogoPreviewCard()

        SectionTitle("Voucher header")
        Field("Employee name", name, { name = it }, "e.g. Matt Griffen")
        Field("Address", address, { address = it }, "e.g. 416 N Franklin St, Fort Bragg")
        Field("Department", department, { department = it }, "e.g. MCN Broadband")
        Field("Charge to account number", account, { account = it }, "e.g. 100-4500-xxxx")
        Field("Traveler title", title, { title = it }, "e.g. Network Technician")

        HorizontalDivider(color = Color(0x14FFFFFF))

        SectionTitle("Mileage rate")
        Field("Rate per mile (USD)", rateText, { rateText = it }, "0.725", KeyboardType.Decimal)
        Field("Rate label (printed on form)", rateLabel, { rateLabel = it }, "2026 IRS standard")
        Text(
            "The 2026 IRS business standard is \$0.725/mile. Update both fields when the rate changes.",
            style = MaterialTheme.typography.bodySmall,
            color = SleekTextDim,
        )

        Button(
            onClick = {
                val rate = rateText.toDoubleOrNull()?.takeIf { it > 0 } ?: MileageProfile.DEFAULT_RATE_PER_MILE
                onSave(
                    MileageProfile(
                        employeeName = name.trim(),
                        address = address.trim(),
                        department = department.trim(),
                        accountNumber = account.trim(),
                        travelerTitle = title.trim(),
                        ratePerMile = rate,
                        rateLabel = rateLabel.trim().ifEmpty { MileageProfile.DEFAULT_RATE_LABEL },
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }
    }
}

@Composable
private fun LogoPreviewCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "These appear on your reimbursement voucher",
                style = MaterialTheme.typography.titleSmall,
                color = SleekText,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_fort_bragg),
                    contentDescription = "City of Fort Bragg seal",
                    modifier = Modifier.height(56.dp),
                )
                Image(
                    painter = painterResource(R.drawable.logo_mcn_broadband),
                    contentDescription = "MCN Broadband logo",
                    modifier = Modifier.height(56.dp),
                )
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = mileageFieldColors(),
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = SleekTextDim,
    )
}
