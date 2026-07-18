package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LocalServerDataService
import com.example.data.ServerProfile
import com.example.data.ServerSettingsState
import com.example.server.version.CompatibilityMode
import com.example.server.version.EngineCompatibilityValidator
import com.example.server.version.EngineVersionCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Properties

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EngineCompatibilityTest {

    @Test
    fun `PowerNukkitX 2-0-0 maps only to 1-26-30`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        val version = catalog.findVersion("powernukkitx:2.0.0")
        
        assertNotNull(version)
        assertEquals(listOf("1.26.30"), version!!.supportedBedrockVersions)
        assertEquals("1.26.30", version.recommendedBedrockVersion)
    }

    @Test
    fun `Nukkit PM1E Build 4437 maps only to 1-26-30`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        val version = catalog.findVersion("nukkit-pm1e:4437")
        
        assertNotNull(version)
        assertEquals(listOf("1.26.30"), version!!.supportedBedrockVersions)
        assertEquals("1.26.30", version.recommendedBedrockVersion)
    }

    @Test
    fun `PowerNukkit 1-5-2-1-PN maps only to 1-17-40`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        val version = catalog.findVersion("powernukkit:1.5.2.1-PN")
        
        assertNotNull(version)
        assertEquals(listOf("1.17.40"), version!!.supportedBedrockVersions)
        assertEquals("1.17.40", version.recommendedBedrockVersion)
    }

    @Test
    fun `Cloudburst produces no selectable option while compatibility is UNKNOWN`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        val version = catalog.findVersion("cloudburst:1.0-20260616.184029-1239")
        
        assertNotNull(version)
        assertEquals(CompatibilityMode.UNKNOWN, version!!.compatibilityMode)
        assertTrue(version.supportedBedrockVersions.isEmpty())
        assertNull(version.recommendedBedrockVersion)
    }

    @Test
    fun `Nukkit-MOT produces one recommended option with multi-version information`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        val version = catalog.findVersion("nukkit-mot:rolling-java25")
        
        assertNotNull(version)
        assertEquals(CompatibilityMode.MULTI_VERSION, version!!.compatibilityMode)
        assertEquals("1.26.30", version.recommendedBedrockVersion)
        assertNotNull(version.compatibilitySummary)
    }

    @Test
    fun `recommendedBedrockVersion must exist in supportedBedrockVersions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        catalog.getAllVersions().forEach { version ->
            if (version.recommendedBedrockVersion != null) {
                assertTrue(
                    "Recommended version ${version.recommendedBedrockVersion} not in supported list for ${version.id}",
                    version.supportedBedrockVersions.contains(version.recommendedBedrockVersion)
                )
            }
        }
    }

    @Test
    fun `Invalid profile compatibility blocks startup`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = EngineVersionCatalog(context)
        
        val invalidProfile = ServerProfile(
            id = "test",
            name = "Test",
            engineId = "bedrock_power_nukkit_x",
            engineVersionId = "powernukkitx:2.0.0",
            bedrockVersion = "1.21.0", // Invalid version for this build
            serverDirectory = "",
            levelName = "world",
            iconPath = null,
            port = 19132,
            memoryMb = 600,
            maxPlayers = 10,
            createdAt = 0,
            updatedAt = 0
        )
        
        val result = EngineCompatibilityValidator.validate(invalidProfile, catalog)
        assertFalse(result.success)
        assertEquals("Selected Minecraft version is not supported by the selected engine build.", result.message)
    }

    @Test
    fun `bedrockVersion is saved and read from profile-properties`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempDir = File(context.cacheDir, "test_server")
        tempDir.mkdirs()
        
        val dataService = LocalServerDataService { tempDir }
        val settings = ServerSettingsState(serverName = "Test Server", levelName = "world")
        
        dataService.saveLocalServerProfile(
            settings = settings,
            templateId = "bedrock_power_nukkit_x",
            engineVersionId = "powernukkitx:2.0.0",
            bedrockVersion = "1.26.30",
            iconPath = null
        )
        
        val profile = dataService.readLocalServerProfile(context)
        assertNotNull(profile)
        assertEquals("1.26.30", profile!!.bedrockVersion)
        
        // Verify file content directly
        val propsFile = File(tempDir, ".minehost/profile.properties")
        val props = Properties()
        propsFile.inputStream().use(props::load)
        assertEquals("1.26.30", props.getProperty("bedrockVersion"))
    }
}
