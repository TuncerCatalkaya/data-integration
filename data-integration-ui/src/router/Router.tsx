import { BrowserRouter, MemoryRouter, Navigate, Route, Routes, useParams } from "react-router-dom"
import TokenProtectedRoute from "../components/protectedRoute/TokenProtectedRoute"
import RootPage from "../pages/rootPage/RootPage"
import ProjectsPage from "../pages/projectsPage/ProjectsPage"
import ProjectPage from "../pages/projectPage/ProjectPage"
import RouterPaths from "./constants/RouterPaths"
import ProjectDetailsPage from "../pages/projectDetailsPage/ProjectDetailsPage"
import ProjectImportPage from "../pages/projectImportPage/ProjectImportPage"
import ProjectMappedItemsPage from "../pages/projectMappedItemsPage/ProjectMappedItemsPage"

function ProjectRedirect() {
    const { projectId } = useParams()
    return <Navigate to={RouterPaths.PROJECT_DETAILS.replace(":projectId", projectId!)} replace />
}

export default function Router() {
    const routes = (
        <Routes>
            <Route path={RouterPaths.INDEX} element={<Navigate to={RouterPaths.ROOT} replace />} />

            <Route path={RouterPaths.ROOT} element={<TokenProtectedRoute component={<RootPage />} />}>
                <Route index element={<Navigate to={RouterPaths.PROJECTS} replace />} />
                <Route path={RouterPaths.PROJECTS} element={<ProjectsPage />} />

                <Route path={RouterPaths.PROJECT} element={<ProjectPage />}>
                    <Route index element={<ProjectRedirect />} />
                    <Route path={RouterPaths.PROJECT_DETAILS} element={<ProjectDetailsPage />} />
                    <Route path={RouterPaths.PROJECT_IMPORT} element={<ProjectImportPage />} />
                    <Route path={RouterPaths.PROJECT_MAPPING} element={<ProjectMappedItemsPage />} />
                </Route>
            </Route>

            <Route path="*" element={<Navigate to={RouterPaths.INDEX} replace />} />
        </Routes>
    )
    return <>{import.meta.env.DEV ? <BrowserRouter>{routes}</BrowserRouter> : <MemoryRouter>{routes}</MemoryRouter>}</>
}
