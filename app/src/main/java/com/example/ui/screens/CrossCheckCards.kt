package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlternativeMatch
import com.example.data.model.BotanicalEvidence
import com.example.data.model.CrossCheckAnalysis
import com.example.data.model.SourceComparisonItem
import com.example.data.model.TaxonomyVerification
import com.example.ui.theme.BotanicalBorder
import com.example.ui.theme.BotanicalButtonPrimaryBg
import com.example.ui.theme.BotanicalButtonPrimaryText
import com.example.ui.theme.BotanicalCard
import com.example.ui.theme.BotanicalPrimaryActive
import com.example.ui.theme.BotanicalSoftSage
import com.example.ui.theme.BotanicalStatusHealthy
import com.example.ui.theme.BotanicalStatusNeutral
import com.example.ui.theme.BotanicalStatusOverdue
import com.example.ui.theme.BotanicalStatusWarning
import com.example.ui.theme.BotanicalStatusWater
import com.example.ui.theme.BotanicalSurfaceElevated
import com.example.ui.theme.BotanicalTextMuted
import com.example.ui.theme.BotanicalTextPrimary
import com.example.ui.theme.BotanicalTextSecondary

/**
 * Alert card displayed when reputable sources disagree or visual characters are ambiguous.
 * Informs the user and prompts for an additional photo angle to cross-check.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UncertainIdentificationAlertCard(
    crossCheck: CrossCheckAnalysis,
    onTakePhoto: () -> Unit,
    onUploadPhoto: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BotanicalCard),
        border = BorderStroke(1.dp, BotanicalStatusWarning.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("uncertain_id_alert_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Uncertain",
                        tint = BotanicalStatusWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Identification Uncertain",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalTextPrimary
                    )
                    Text(
                        text = "Botanical sources require additional verification",
                        style = MaterialTheme.typography.bodySmall,
                        color = BotanicalStatusWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val reasonText = if (crossCheck.uncertaintyReason.isNotBlank()) {
                crossCheck.uncertaintyReason
            } else {
                "Sources report conflicting morphological traits or overlapping species characteristics. Multiple candidates fit this specimen."
            }

            Text(
                text = reasonText,
                style = MaterialTheme.typography.bodyMedium,
                color = BotanicalTextSecondary,
                lineHeight = 20.sp
            )

            if (crossCheck.requestedFollowUpPhotos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A photo of any of these parts will help confirm the species:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BotanicalSoftSage
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    crossCheck.requestedFollowUpPhotos.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BotanicalSurfaceElevated,
                            border = BorderStroke(1.dp, BotanicalBorder)
                        ) {
                            Text(
                                text = "📷 $suggestion",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = BotanicalTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons to provide additional photo for cross-checking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTakePhoto,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BotanicalButtonPrimaryBg,
                        contentColor = BotanicalButtonPrimaryText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("uncertain_take_photo_btn")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onUploadPhoto,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BotanicalSoftSage),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BotanicalTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("uncertain_upload_photo_btn")
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Cross-Database Comparison Card displaying agreements and disagreements between sources.
 */
@Composable
fun CrossDatabaseComparisonCard(
    crossCheck: CrossCheckAnalysis
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BotanicalCard),
        border = BorderStroke(1.dp, BotanicalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cross_database_comparison_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = BotanicalPrimaryActive,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Database Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalTextPrimary
                    )
                }

                // Agreement Level Pill
                val isStrong = crossCheck.agreementLevel.contains("Strong", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BotanicalSurfaceElevated,
                    border = BorderStroke(1.dp, if (isStrong) BotanicalStatusHealthy else BotanicalStatusWarning)
                ) {
                    Text(
                        text = "Agreement: ${crossCheck.agreementLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isStrong) BotanicalStatusHealthy else BotanicalStatusWarning,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Consensus / Disagreement Summary
            if (crossCheck.agreementSummary.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BotanicalSurfaceElevated,
                    border = BorderStroke(1.dp, BotanicalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = crossCheck.agreementSummary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = BotanicalTextSecondary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Sources List showing how each individual source voted
            crossCheck.sourcesComparison.forEachIndexed { index, source ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (source.agreement) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = if (source.agreement) "Agree" else "Differ",
                        tint = if (source.agreement) BotanicalStatusHealthy else BotanicalStatusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${source.sourceName}:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = source.speciesMatched,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = if (source.agreement) BotanicalPrimaryActive else BotanicalStatusWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < crossCheck.sourcesComparison.lastIndex) {
                    HorizontalDivider(
                        color = BotanicalBorder,
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Alternative Realistic Matches Card showing candidates when there is disagreement or ambiguity.
 */
@Composable
fun AlternativeMatchesCard(
    primarySpecies: String,
    alternatives: List<AlternativeMatch>
) {
    if (alternatives.isEmpty()) return

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BotanicalCard),
        border = BorderStroke(1.dp, BotanicalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alternative_matches_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌿", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Alternative Matches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalTextPrimary
                    )
                    Text(
                        text = "Compared against realistic botanical candidates",
                        style = MaterialTheme.typography.bodySmall,
                        color = BotanicalTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            alternatives.forEachIndexed { index, match ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BotanicalSurfaceElevated,
                    border = BorderStroke(1.dp, BotanicalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BotanicalPrimaryActive
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = match.commonName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BotanicalTextPrimary
                                    )
                                    Text(
                                        text = match.scientificName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = BotanicalSoftSage
                                    )
                                }
                            }

                            // Support Strength Badge
                            val textColor = when {
                                match.supportStrength.contains("Strong", ignoreCase = true) -> BotanicalStatusHealthy
                                match.supportStrength.contains("Possible", ignoreCase = true) -> BotanicalStatusWater
                                else -> BotanicalStatusNeutral
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BotanicalCard,
                                border = BorderStroke(1.dp, BotanicalBorder)
                            ) {
                                Text(
                                    text = match.supportStrength,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (match.differentiatingTraits.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = match.differentiatingTraits,
                                style = MaterialTheme.typography.bodySmall,
                                color = BotanicalTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botanical Morphological Evidence Card explaining WHY the plant was identified.
 * Details leaf shape, arrangement, venation, stem, flowers, bark, and diagnostic reasoning.
 */
@Composable
fun WhyIdentifiedBotanicalFeaturesCard(
    evidence: BotanicalEvidence
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BotanicalCard),
        border = BorderStroke(1.dp, BotanicalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("why_identified_evidence_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = BotanicalPrimaryActive,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Why this plant was identified",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalTextPrimary
                    )
                    Text(
                        text = "Diagnostic morphological characters observed in specimen",
                        style = MaterialTheme.typography.bodySmall,
                        color = BotanicalTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (evidence.diagnosticReasoning.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BotanicalSurfaceElevated,
                    border = BorderStroke(1.dp, BotanicalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Diagnostic Reasoning",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalSoftSage
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = evidence.diagnosticReasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = BotanicalTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            EvidenceItemRow(icon = "🍃", label = "Leaf Shape", value = evidence.leafShape)
            EvidenceItemRow(icon = "🌿", label = "Leaf Arrangement", value = evidence.leafArrangement)
            EvidenceItemRow(icon = "🕸️", label = "Venation Pattern", value = evidence.venation)
            EvidenceItemRow(icon = "🎋", label = "Stem & Petioles", value = evidence.stemAndPetioles)
            EvidenceItemRow(icon = "🌸", label = "Flowers & Fruit", value = evidence.flowersAndFruit)
            EvidenceItemRow(icon = "🪵", label = "Bark / Cuticle Surface", value = evidence.barkOrSurface)
            EvidenceItemRow(icon = "🎨", label = "Variegation & Color", value = evidence.variegationOrColor)
            EvidenceItemRow(icon = "🌱", label = "Growth Habit", value = evidence.growthHabit)
        }
    }
}

@Composable
private fun EvidenceItemRow(icon: String, label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = BotanicalSoftSage
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = BotanicalTextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Botanical Taxonomy and Nomenclature Verification Card.
 * Cross-checks accepted name, synonyms, genus, family, and explains nomenclatural revisions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaxonomyVerificationCard(
    taxonomy: TaxonomyVerification
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BotanicalCard),
        border = BorderStroke(1.dp, BotanicalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("taxonomy_verification_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = BotanicalPrimaryActive,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Taxonomy Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BotanicalSurfaceElevated,
                    border = BorderStroke(1.dp, BotanicalBorder)
                ) {
                    Text(
                        text = taxonomy.taxonomicStatus,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalStatusHealthy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = taxonomy.acceptedScientificName,
                style = MaterialTheme.typography.titleSmall,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = BotanicalTextPrimary
            )

            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Genus: ${taxonomy.genus}  •  Family: ${taxonomy.family}",
                    style = MaterialTheme.typography.labelMedium,
                    color = BotanicalTextSecondary
                )
            }

            if (taxonomy.taxonomicNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BotanicalSurfaceElevated,
                    border = BorderStroke(1.dp, BotanicalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = taxonomy.taxonomicNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = BotanicalTextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (taxonomy.synonyms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Documented Botanical Synonyms:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BotanicalSoftSage
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    taxonomy.synonyms.take(6).forEach { syn ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BotanicalSurfaceElevated,
                            border = BorderStroke(1.dp, BotanicalBorder)
                        ) {
                            Text(
                                text = syn,
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = BotanicalTextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable Source Transparency Card showing all databases queried for the identification.
 */
@Composable
fun SourcesCheckedExpandableCard(
    sources: List<SourceComparisonItem>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BotanicalCard),
        border = BorderStroke(1.dp, BotanicalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sources_checked_expandable_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📚", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Sources Checked (${sources.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalTextPrimary
                        )
                        Text(
                            text = if (isExpanded) "Tap to collapse" else "Tap to see database records and citations",
                            style = MaterialTheme.typography.bodySmall,
                            color = BotanicalTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = BotanicalPrimaryActive
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    sources.forEachIndexed { index, source ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BotanicalSurfaceElevated,
                            border = BorderStroke(1.dp, BotanicalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = source.sourceName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BotanicalTextPrimary
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BotanicalCard,
                                        border = BorderStroke(1.dp, BotanicalBorder)
                                    ) {
                                        Text(
                                            text = source.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (source.agreement) BotanicalStatusHealthy else BotanicalStatusWarning,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (source.sourceType.isNotBlank()) {
                                    Text(
                                        text = source.sourceType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BotanicalSoftSage
                                    )
                                }

                                if (source.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = source.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BotanicalTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }

                                if (source.url.isNotBlank() && (source.url.startsWith("http://") || source.url.startsWith("https://"))) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = BotanicalPrimaryActive,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "View scientific record",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BotanicalPrimaryActive,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
