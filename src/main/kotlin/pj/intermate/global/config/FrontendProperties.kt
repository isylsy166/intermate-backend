package pj.intermate.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
* @property baseUrl
* */
@ConfigurationProperties(prefix = "app.frontend")
data class FrontendProperties(
    val baseUrl: String,
    val loginSuccessPath: String,
    val loginFailurePath: String,
) {
    val loginSuccessUri: String
        get() = baseUrl + loginSuccessPath

    val loginFailureUri: String
        get() = baseUrl + loginFailurePath
}
