import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, InputAdornment, Paper, PaperProps, TextField } from "@mui/material"
import Draggable from "react-draggable"
import { Add, Close, Folder } from "@mui/icons-material"
import { ChangeEvent, useState } from "react"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useTranslation } from "react-i18next"

interface CreateProjectDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-project-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function CreateProjectDialog(createProjectDialogProps: Readonly<CreateProjectDialogProps>) {
    const [projectName, setProjectName] = useState<string>("")
    const [createProject] = ProjectsApi.useCreateProjectMutation()
    const { enqueueSnackbar } = useSnackbar()

    const translation = useTranslation()

    const handleChangeProjectName = (e: ChangeEvent<HTMLInputElement>) => setProjectName(e.target.value)

    const closeDialog = (shouldReload = false) => {
        createProjectDialogProps.handleClickClose(shouldReload)
        setProjectName("")
    }

    const handleClickCreateProject = async () => {
        const response = await createProject({ projectName })

        if (response.data) {
            enqueueSnackbar(translation.t("pages.projects.components.dialogs.createProjectDialog.snackbar.success"), { variant: "success" })
            closeDialog(true)
        } else if (response.error) {
            enqueueSnackbar(translation.t("pages.projects.components.dialogs.createProjectDialog.snackbar.error"), { variant: "error" })
        }
    }

    const handleProjectNameKeyPress = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault()
            if (projectName) {
                await handleClickCreateProject()
            }
        }
    }

    return (
        <Dialog
            open={createProjectDialogProps.open}
            onClose={() => closeDialog()}
            aria-labelledby="create-project-dialog"
            PaperComponent={PaperComponent}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>{translation.t("pages.projects.components.dialogs.createProjectDialog.title")}</DialogTitle>
            <DialogContent>
                <Box component="form" noValidate autoComplete="off">
                    <TextField
                        autoFocus
                        margin="dense"
                        value={projectName}
                        label={translation.t("pages.projects.components.dialogs.createProjectDialog.input.projectName")}
                        onChange={handleChangeProjectName}
                        onKeyDown={handleProjectNameKeyPress}
                        fullWidth
                        variant="outlined"
                        inputProps={{ maxLength: 255 }}
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <Folder />
                                </InputAdornment>
                            )
                        }}
                    />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" disabled={!projectName} onClick={handleClickCreateProject} endIcon={<Add />}>
                    {translation.t("pages.projects.components.dialogs.createProjectDialog.actions.submit")}
                </Button>
                <Button variant="contained" color="error" onClick={() => closeDialog()} startIcon={<Close />}>
                    {translation.t("pages.projects.components.dialogs.createProjectDialog.actions.cancel")}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
