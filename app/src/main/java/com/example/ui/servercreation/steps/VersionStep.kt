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
    bedrockVersions: List<Pair<String, EngineVersion>>,
    onBedrockVersionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                "Minecraft Version",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = WizardTheme.PrimaryText
            )
            Text(
                "Choose a Minecraft Bedrock version supported by this server engine.",
                style = MaterialTheme.typography.bodySmall,
                color = WizardTheme.SecondaryText
            )
        }

        if (bedrockVersions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Compatibility information is not available for this engine build.",
                    color = WizardTheme.SecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            bedrockVersions.forEach { (bv, engineVersion) ->
                BedrockVersionCard(
                    bedrockVersion = bv,
                    engineVersion = engineVersion,
                    selected = draft.bedrockVersion == bv,
                    onClick = { onBedrockVersionSelected(bv) }
                )
            }
        }
    }
}

@Composable
private fun BedrockVersionCard(
    bedrockVersion: String,
    engineVersion: EngineVersion,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) WizardTheme.PrimaryBlue else WizardTheme.Border,
                shape = RoundedCornerShape(WizardTheme.OptionCardRadius)
            ),
        shape = RoundedCornerShape(WizardTheme.OptionCardRadius),
        color = if (selected) WizardTheme.PrimaryBlue.copy(alpha = 0.05f) else Color.White,
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Version Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) WizardTheme.PrimaryBlue.copy(alpha = 0.1f) 
                        else WizardTheme.DisabledBackground,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (selected) WizardTheme.PrimaryBlue else WizardTheme.DisabledText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minecraft Bedrock",
                            style = MaterialTheme.typography.labelSmall,
                            color = WizardTheme.SecondaryText
                        )
                        Text(
                            text = bedrockVersion,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = WizardTheme.PrimaryText
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    if (engineVersion.recommended) {
                        Spacer(Modifier.width(8.dp))
                        VersionTag(text = "Recommended", color = WizardTheme.Success)
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = "Compatible build: ${engineVersion.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WizardTheme.SecondaryText
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(WizardTheme.PrimaryBlue, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionTag(
    text: String,
    color: Color
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
            )
        )
    }
}
