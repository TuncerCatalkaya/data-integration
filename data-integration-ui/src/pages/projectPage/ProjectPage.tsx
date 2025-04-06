import { Outlet, useParams } from "react-router-dom"
import ProjectTabs from "./components/projectTabs/ProjectTabs"
import { Box, Stack, Typography } from "@mui/material"
import { NavigateBefore } from "@mui/icons-material"
import useNavigate from "../../router/hooks/useNavigate"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"
import theme from "../../theme"
import { useCallback, useEffect } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"

export default function ProjectPage() {
    const { projectId } = useParams()
    const { toProjects } = useNavigate()
    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()

    const [isProjectPermitted] = ProjectsApi.useLazyIsProjectPermittedQuery()

    const fetchData = useCallback(async () => {
        const response = await isProjectPermitted({ projectId: projectId! })
        if (response.error) {
            toProjects()
        }
    }, [isProjectPermitted, projectId, toProjects])

    useEffect(() => {
        fetchData()
    }, [fetchData])
    return (
        <>
            {openConfirmationDialog && (
                <ConfirmationDialog open={openConfirmationDialog} handleClickClose={handleClickCloseConfirmationDialog} handleClickYes={() => toProjects()}>
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to go back to the project overview?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Stack spacing={2}>
                <NavigateBefore fontSize="large" onClick={handleClickOpenConfirmationDialog} sx={{ ":hover": { cursor: "pointer" } }} />
                <Box>
                    <ProjectTabs />
                    <Box
                        alignItems="left"
                        sx={{
                            backgroundColor: theme.palette.grey["500"],
                            padding: "20px",
                            borderRadius: "0 8px 8px 8px",
                            width: "90vw",
                            margin: "0 auto",
                            display: "flex",
                            justifyContent: "flex-start"
                        }}
                    >
                        <Outlet />
                    </Box>
                </Box>
            </Stack>
        </>
    )
}
