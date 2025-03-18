import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, PaperProps, Stack, Switch, TextField, Tooltip, Typography } from "@mui/material"
import Draggable from "react-draggable"
import { Add, Edit } from "@mui/icons-material"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useParams } from "react-router-dom"
import { ScopeHeaderResponse } from "../../../../features/projects/projects.types"
import { ChangeEvent, useEffect, useMemo, useState } from "react"
import useShake from "../../../../components/shake/hooks/useShake"
import { ProjectsApi } from "../../../../features/projects/projects.api"

interface EditHeaderDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    scopeId: string
    scopeHeaders: ScopeHeaderResponse[]
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#edit-header-dialog" cancel={'[class*="MuiDialogContent-root"], .non-draggable'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function EditHeaderDialog({ open, handleClickClose, scopeId, scopeHeaders }: Readonly<EditHeaderDialogProps>) {
    const [headerName, setHeaderName] = useState("")
    const [headers, setHeaders] = useState<ScopeHeaderResponse[]>([])
    const [createOrUpdateScopeHeaders] = ProjectsApi.useCreateOrUpdateScopeHeadersMutation()

    const { projectId } = useParams()
    const { enqueueSnackbar } = useSnackbar()

    const { handleShakeClick, shakeSx } = useShake()

    const handleHeaderNameChange = (event: ChangeEvent<HTMLInputElement>) => {
        const newHeaderName = event.target.value
        setHeaderName(newHeaderName)
    }

    const handleClickAddHeader = () => {
        if (headers.some(header => header.name === headerName)) {
            enqueueSnackbar("Header name exists already.", { variant: "error" })
            return
        }

        setHeaderName("")
        setHeaders([
            ...headers,
            {
                name: headerName,
                hidden: false
            }
        ])
        enqueueSnackbar("Header added at end of list.", { variant: "success" })
    }

    const handleClickSubmit = async () => {
        const createOrUpdateScopeHeadersResponse = await createOrUpdateScopeHeaders({ projectId: projectId!, scopeId, headers })
        if (createOrUpdateScopeHeadersResponse.data) {
            handleClickClose(true)
            enqueueSnackbar("Header edited.", { variant: "success" })
        }
    }

    const closeDialog = (shouldReload = false) => {
        handleClickClose(shouldReload)
        setHeaderName("")
    }

    useEffect(() => {
        setHeaders(scopeHeaders)
    }, [scopeHeaders])

    const headerComponents = useMemo(
        () =>
            headers.map(header => (
                <Paper key={header.name} elevation={5} sx={{ minWidth: 500, padding: 2, borderRadius: 5 }}>
                    <Stack direction="row" display="flex" justifyContent="space-between" alignItems="center" spacing={1}>
                        <Tooltip title={header.name} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <Typography
                                noWrap
                                sx={{
                                    maxWidth: 500,
                                    textOverflow: "ellipsis",
                                    overflow: "hidden",
                                    whiteSpace: "nowrap",
                                    fontWeight: "bold"
                                }}
                            >
                                {header.name}
                            </Typography>
                        </Tooltip>
                        <Switch
                            checked={!header.hidden}
                            onChange={(_, checked) => {
                                setHeaders(prevHeaders => prevHeaders.map(h => (h.name === header.name ? { ...h, hidden: !checked } : h)))
                            }}
                        />
                    </Stack>
                </Paper>
            )),
        [headers]
    )

    return (
        <Dialog open={open} onClose={handleShakeClick} aria-labelledby="edit-header-dialog" PaperComponent={PaperComponent} sx={{ zIndex: theme.zIndex.modal }}>
            <DialogTitle sx={{ cursor: "move" }}>
                <Stack spacing={1}>
                    <Stack direction="row" display="flex" justifyContent="space-between">
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Edit />
                            <Typography variant="h6">{"Edit Header"}</Typography>
                        </Stack>
                    </Stack>
                    <Paper elevation={5} className="non-draggable" sx={{ minWidth: 500, padding: 2, borderRadius: 5, cursor: "default" }}>
                        <Stack direction="row" display="flex" justifyContent="space-between" alignItems="center" spacing={1}>
                            <TextField
                                label="Header Name"
                                placeholder={"Enter a header name..."}
                                value={headerName}
                                InputLabelProps={{
                                    shrink: true
                                }}
                                onChange={handleHeaderNameChange}
                            />
                            <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                                <Button variant="contained" disabled={!headerName.trim()} onClick={handleClickAddHeader} endIcon={<Add />}>
                                    {"Add Header"}
                                </Button>
                            </Box>
                        </Stack>
                    </Paper>
                </Stack>
            </DialogTitle>
            <DialogContent>
                <Stack spacing={1} padding={2}>
                    {headerComponents}
                </Stack>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" onClick={handleClickSubmit}>
                    {"Submit"}
                </Button>
                <Button variant="contained" color="error" onClick={() => closeDialog()} sx={shakeSx}>
                    {"Cancel"}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
