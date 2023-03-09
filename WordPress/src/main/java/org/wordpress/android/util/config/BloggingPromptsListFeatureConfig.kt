package org.wordpress.android.util.config

import org.wordpress.android.BuildConfig
import org.wordpress.android.annotation.RemoteFeatureFlagDefault
import javax.inject.Inject

@RemoteFeatureFlagDefault(BloggingPromptsListFeatureConfig.BLOGGING_PROMPTS_LIST_REMOTE_FIELD, true)
class BloggingPromptsListFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(
    appConfig,
    BuildConfig.BLOGGING_PROMPTS_LIST,
    BLOGGING_PROMPTS_LIST_REMOTE_FIELD,
) {
    override fun isEnabled(): Boolean {
        return super.isEnabled() && BuildConfig.IS_JETPACK_APP
    }

    companion object {
        const val BLOGGING_PROMPTS_LIST_REMOTE_FIELD = "blogging_prompts_list_enabled"
    }
}
