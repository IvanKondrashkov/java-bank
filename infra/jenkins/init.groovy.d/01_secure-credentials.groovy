import jenkins.model.*
import hudson.util.Secret
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

def env = System.getenv()

def dbPassword = env['DB_PASSWORD']
def keycloakAdminPass = env['KEYCLOAK_ADMIN_PASSWORD']
def ghcrToken = env['GHCR_TOKEN']
def githubToken = env['GITHUB_TOKEN']
def githubUsername = env['GITHUB_USERNAME']
def dockerRegistry = env['DOCKER_REGISTRY']

def store = Jenkins.instance.getExtensionList(
        'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
)[0].getStore()

if (githubUsername && githubToken) {
    println "--> Creating credential: github-creds (username + token)"
    def githubCreds = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            "github-creds",
            "GitHub credentials from ENV",
            githubUsername,
            githubToken
    )
    store.addCredentials(Domain.global(), githubCreds)
}

if (githubUsername) {
    println "--> Creating credential: GITHUB_USERNAME (plain string)"
    def usernameCred = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "GITHUB_USERNAME",
            "GitHub username only (for registry login)",
            Secret.fromString(githubUsername)
    )
    store.addCredentials(Domain.global(), usernameCred)
}

if (ghcrToken) {
    println "--> Creating credential: GHCR_TOKEN"
    def ghcrCred = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "GHCR_TOKEN",
            "GHCR token from ENV",
            Secret.fromString(ghcrToken)
    )
    store.addCredentials(Domain.global(), ghcrCred)
}

if (dockerRegistry) {
    println "--> Creating credential: DOCKER_REGISTRY"
    def registryCred = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "DOCKER_REGISTRY",
            "Docker registry address from ENV",
            Secret.fromString(dockerRegistry)
    )
    store.addCredentials(Domain.global(), registryCred)
}

if (dbPassword) {
    println "--> Creating credential: DB_PASSWORD"
    def dbCred = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "DB_PASSWORD",
            "Database password from ENV",
            Secret.fromString(dbPassword)
    )
    store.addCredentials(Domain.global(), dbCred)
}

if (keycloakAdminPass) {
    println "--> Creating credential: KEYCLOAK_ADMIN_PASSWORD"
    def keycloakCred = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "KEYCLOAK_ADMIN_PASSWORD",
            "Keycloak admin password from ENV",
            Secret.fromString(keycloakAdminPass)
    )
    store.addCredentials(Domain.global(), keycloakCred)
}

println "--> Credential setup complete."