import {
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControlLabel,
    IconButton,
    ListItem,
    Paper,
    PaperProps,
    Stack,
    TextField,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import { Add, ArrowDownward, ArrowUpward, Edit } from "@mui/icons-material"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useParams } from "react-router-dom"
import { ScopeHeaderResponse } from "../../../../features/projects/projects.types"
import React, { useEffect, useRef, useState } from "react"
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
    const headerNameRef = useRef<HTMLInputElement | undefined>()
    const headerRefs = useRef<Record<string, HTMLInputElement | null>>({})
    const [headers, setHeaders] = useState<ScopeHeaderResponse[]>([])

    const [createOrUpdateScopeHeaders] = ProjectsApi.useCreateOrUpdateScopeHeadersMutation()

    const { projectId } = useParams()
    const { enqueueSnackbar } = useSnackbar()

    const { handleShakeClick, shakeSx } = useShake()

    const handleAddHeaderKeyPress = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault()
            handleClickAddHeader()
        }
    }

    const handleClickAddHeader = () => {
        const trimmedHeaderName = headerNameRef.current!.value.trim()
        if (!trimmedHeaderName) {
            enqueueSnackbar("Header name needs text.", { variant: "error" })
            return
        }
        if (headers.some(header => header.id === trimmedHeaderName)) {
            enqueueSnackbar("Header name exists already.", { variant: "error" })
            return
        }
        headerNameRef.current!.value = ""
        setHeaders([
            ...headers,
            {
                id: trimmedHeaderName,
                display: trimmedHeaderName,
                hidden: false
            }
        ])
        enqueueSnackbar("Header added at end of list.", { variant: "success" })
    }

    const handleClickSubmit = async () => {
        const updatedHeaders = headers.map(header => {
            const refValue = headerRefs.current[header.id]!.value.trim()
            return {
                ...header,
                display: refValue
            }
        })

        const createOrUpdateScopeHeadersResponse = await createOrUpdateScopeHeaders({ projectId: projectId!, scopeId, headers: updatedHeaders })
        if (createOrUpdateScopeHeadersResponse.error) {
            enqueueSnackbar("Error occurred during header creation.", { variant: "error" })
        } else if (createOrUpdateScopeHeadersResponse.data) {
            handleClickClose(true)
            enqueueSnackbar("Header edited.", { variant: "success" })
        }
    }

    const closeDialog = (shouldReload = false) => handleClickClose(shouldReload)

    useEffect(() => {
        setHeaders(scopeHeaders)
    }, [scopeHeaders])

    const toggleAllHeaders = (checked: boolean) => {
        setHeaders(prevHeaders =>
            prevHeaders.map(header => ({
                ...header,
                hidden: !checked
            }))
        )
    }

    const moveHeader = (index: number, direction: "up" | "down") => {
        const newHeaders = [...headers]

        if (direction === "up" && index > 0) {
            const temp = newHeaders[index - 1]
            newHeaders[index - 1] = newHeaders[index]
            newHeaders[index] = temp
        } else if (direction === "down" && index < newHeaders.length - 1) {
            const temp = newHeaders[index + 1]
            newHeaders[index + 1] = newHeaders[index]
            newHeaders[index] = temp
        }

        setHeaders(newHeaders)
    }

    const allChecked = headers.every(header => !header.hidden)
    const someChecked = headers.some(header => !header.hidden) && !allChecked

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
                                inputRef={headerNameRef}
                                label="Header Name"
                                placeholder={"Enter a header name..."}
                                onKeyDown={handleAddHeaderKeyPress}
                                InputLabelProps={{
                                    shrink: true
                                }}
                            />
                            <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                                <Button variant="contained" onClick={handleClickAddHeader} endIcon={<Add />}>
                                    {"Add Header"}
                                </Button>
                            </Box>
                        </Stack>
                    </Paper>
                </Stack>
            </DialogTitle>
            <DialogContent>
                <FormControlLabel
                    control={<Checkbox checked={allChecked} indeterminate={someChecked} onChange={(_, checked) => toggleAllHeaders(checked)} />}
                    label="Select All"
                    sx={{ marginBottom: 2 }}
                />
                {headers.map((header, index) => (
                    <ListItem key={header.id}>
                        <Checkbox
                            checked={!header.hidden}
                            onChange={(_, checked) => {
                                setHeaders(prevHeaders => prevHeaders.map(h => (h.id === header.id ? { ...h, hidden: !checked } : h)))
                            }}
                        />
                        <TextField
                            key={header.id}
                            defaultValue={header.display}
                            inputRef={el => {
                                if (el) headerRefs.current[header.id] = el
                            }}
                            variant="standard"
                            fullWidth
                            InputProps={{
                                sx: {
                                    fontWeight: "bold",
                                    textOverflow: "ellipsis",
                                    overflow: "hidden",
                                    whiteSpace: "nowrap"
                                }
                            }}
                        />
                        <Box sx={{ display: "flex", flexDirection: "column" }}>
                            <IconButton size="small" onClick={() => moveHeader(index, "up")} disabled={index === 0}>
                                <ArrowUpward fontSize="small" />{" "}
                            </IconButton>
                            <IconButton size="small" onClick={() => moveHeader(index, "down")} disabled={index === headers.length - 1}>
                                <ArrowDownward fontSize="small" />{" "}
                            </IconButton>
                        </Box>
                    </ListItem>
                ))}
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
