package com.example.ui.servercreation.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.server.version.EngineVersion
import com.example.server.version.ReleaseChannel
import com.example.ui.servercreation.CreateServerDraft
import com.example.ui.servercreation.WizardTheme

@Composable
fun VersionStep(
    draft: CreateServerDraft,
    versions: List<EngineVersion>,
    onVersionSelected: (EngineVersion) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                "Engine Build",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = WizardTheme.PrimaryText
            )
            Text(
                "Choose the server-engine build MineHost will install.",
                style = MaterialTheme.typography.bodySmall,
                color = WizardTheme.SecondaryText
            )
        }

        if (versions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No builds available for this engine.", color = WizardTheme.SecondaryText)
            }
        } else {
            versions.forEach { version ->
                VersionCard(
                    version = version,
                    selected = draft.engineVersionId == version.id,
                    onClick = { onVersionSelected(version) }
                )
            }
        }
    }
}

@Composable
private fun VersionCard(
    version: EngineVersion,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) WizardTheme.PrimaryBlue else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) WizardTheme.PrimaryBlue.copy(alpha = 0.05f) else Color.White,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = version.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = WizardTheme.PrimaryText
                        )
                    )
                    
                    if (version.recommended) {
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ) {
                            Text("Recommended", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                        }
                    }

                    if (version.channel == ReleaseChannel.SNAPSHOT) {
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White
                        ) {
                            Text("Snapshot", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                        }
                    } else if (version.channel == ReleaseChannel.PREVIEW) {
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = Color(0xFF9C27B0),
                            contentColor = Color.White
                        ) {
                            Text("Preview", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = version.compatibilityLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = WizardTheme.SecondaryText
                )
                
                Text(
                    text = "Build: ${version.versionName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = WizardTheme.SecondaryText.copy(alpha = 0.7f)
                )
            }

            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = WizardTheme.PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
