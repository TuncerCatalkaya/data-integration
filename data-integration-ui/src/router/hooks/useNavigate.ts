import { generatePath, useNavigate as useRouterNavigate } from "react-router-dom"
import { useMemo } from "react"
import RouterPaths from "../constants/RouterPaths"

export default function useNavigate() {
    const navigate = useRouterNavigate()
    return useMemo(
        () => ({
            toProjects: () => navigate(generatePath(RouterPaths.PROJECTS)),
            toProject: (projectId: string) => navigate(generatePath(RouterPaths.PROJECT, { projectId })),
            toProjectDetails: (projectId: string) => navigate(generatePath(RouterPaths.PROJECT_DETAILS, { projectId })),
            toProjectImport: (projectId: string) => navigate(generatePath(RouterPaths.PROJECT_IMPORT, { projectId })),
            toProjectMappedItems: (projectId: string) => navigate(generatePath(RouterPaths.PROJECT_MAPPING, { projectId }))
        }),
        [navigate]
    )
}
