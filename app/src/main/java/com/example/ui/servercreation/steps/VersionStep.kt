package com.example.ui.servercreation.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.servercreation.CreateServerDraft
import com.example.ui.servercreation.WizardTheme
import com.example.ui.servercreation.components.VersionArtwork
import com.example.ui.servercreation.components.WizardInfoBanner

@Composable
fun VersionStep(
    draft: CreateServerDraft,
    onDraftUpdate: (CreateServerDraft) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                "Server Version",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = WizardTheme.PrimaryText
            )
            Text(
                "Choose the version for your Minecraft server.",
                style = MaterialTheme.typography.bodySmall,
                color = WizardTheme.SecondaryText
            )
        }

        RecommendedVersionCard()

        // Other Versions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VersionRow("1.20.80", "Stable", "Jun 13, 2024", true)
            VersionRow("1.20.71", "Stable", "May 23, 2024", false)
            VersionRow("1.20.60", "Stable", "Apr 25, 2024", false)
            VersionRow("1.19 LTS", "Long Term Support", "Jun 7, 2023", false)
        }

        WizardInfoBanner(
            text = "Version sync coming in a future runtime update. You will be able to auto-sync to the latest version."
        )
    }
}

@Composable
private fun RecommendedVersionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WizardTheme.OptionCardRadius),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(WizardTheme.SelectedCardGradientLeft, WizardTheme.SelectedCardGradientRight)
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(WizardTheme.GradientLeft, WizardTheme.GradientRight)
                    ),
                    shape = RoundedCornerShape(WizardTheme.OptionCardRadius)
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon (Minecraft block)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WizardTheme.SoftBlue),
                    contentAlignment = Alignment.Center
                ) {
                    VersionArtwork(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Latest Stable",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = WizardTheme.PrimaryBlue
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFF1EEFF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Recommended",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = WizardTheme.AccentPurple,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Text(
                        "1.20.80  •  Stable Release",
                        style = MaterialTheme.typography.bodySmall,
                        color = WizardTheme.SecondaryText,
                        fontSize = 12.sp
                    )
                    Text(
                        "Best stability and performance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WizardTheme.SecondaryText,
                        fontSize = 12.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(WizardTheme.PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionRow(
    version: String,
    status: String,
    date: String,
    selected: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WizardTheme.OptionCardRadius),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, WizardTheme.Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        version,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WizardTheme.PrimaryText
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(6.dp).background(WizardTheme.Success, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        status, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = WizardTheme.SecondaryText,
                        fontSize = 11.sp
                    )
                }
                Text(
                    "Released $date", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = WizardTheme.SecondaryText,
                    fontSize = 11.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(1.dp, WizardTheme.Border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(WizardTheme.PrimaryBlue, CircleShape)
                    )
                }
            }
        }
    }
}
