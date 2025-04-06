import { createRoot } from "react-dom/client"
import App from "./App"

interface DataIntegrationApp {
    init: (containerId: string, language: string, token?: string) => void
}

declare global {
    interface Window {
        DataIntegrationApp: DataIntegrationApp
        dataIntegrationBaseUrl: string
        environments: Map<string, string>
    }
}

window.DataIntegrationApp = {
    init(containerId: string, language: string, token?: string) {
        const container = document.getElementById(containerId)
        if (container) {
            window.environments = new Map<string, string>()
            fetch(`${window.dataIntegrationBaseUrl}/data-integration/environments/frontend`).then(response =>
                response.json().then(data => {
                    const frontendEnvironments: Record<string, string> = data
                    for (const key in frontendEnvironments) {
                        window.environments.set(key, frontendEnvironments[key])
                    }
                    const root = createRoot(container)
                    root.render(<App token={token} language={language} />)
                })
            )
        } else {
            console.error(`Target container ${containerId} is not a DOM element.`)
        }
    }
}
