export default function GetFrontendEnvironment(key: string) {
    let frontendEnvironment = undefined
    if (window.environments) {
        frontendEnvironment = window.environments.get(key)
    }
    if (!frontendEnvironment) {
        frontendEnvironment = import.meta.env[key]
    }
    return frontendEnvironment
}
