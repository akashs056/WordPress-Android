package org.wordpress.android.processor

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RemoteFeatureConfigDefaultsBuilderTest {
    @Test
    fun `build a remote config file content`() {
        val keyA = "keyA"
        val valueA = "valueA"
        val keyB = "keyB"
        val valueB = "valueB"

        val remoteConfigDefaultsBuilder = RemoteFeatureConfigDefaultsBuilder(mapOf(keyA to valueA, keyB to valueB))

        val fileContent = remoteConfigDefaultsBuilder.getContent()

//        assertThat(fileContent.toString()).isEqualTo(
//        intentional fail for testing
        assertThat(fileContent.toString()).isNotEqualTo(
            """
            // Automatically generated file. DO NOT MODIFY
            package org.wordpress.android.util.config

            import kotlin.Any
            import kotlin.String
            import kotlin.collections.Map

            object RemoteFeatureConfigDefaults {
                val remoteFeatureConfigDefaults: Map<String, Any> = mapOf(
                        "$keyA" to "$valueA",
                        "$keyB" to "$valueB"
                        )
            }

        """.trimIndent()
        )
    }
}
