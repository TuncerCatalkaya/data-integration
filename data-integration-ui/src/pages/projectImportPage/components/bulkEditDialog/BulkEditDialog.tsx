import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    InputAdornment,
    InputLabel,
    MenuItem,
    Paper,
    PaperProps,
    Select,
    SelectChangeEvent,
    Stack,
    TextField,
    Tooltip,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import { Edit, ViewColumn } from "@mui/icons-material"
import { ChangeEvent, useState } from "react"
import { ProjectsApi } from "../../../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import theme from "../../../../theme"
import { useParams } from "react-router-dom"
import useShake from "../../../../components/shake/hooks/useShake"
import { ScopeHeaderResponse } from "../../../../features/projects/projects.types"

interface BulkEditDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    itemIds: string[]
    headers: ScopeHeaderResponse[]
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#bulk-edit-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function BulkEditDialog(bulkEditDialogProps: Readonly<BulkEditDialogProps>) {
    const { projectId } = useParams()
    const [header, setHeader] = useState("select")
    const [newValue, setNewValue] = useState<string>("")
    const [updateItemProperties] = ProjectsApi.useUpdateItemPropertiesMutation()
    const { enqueueSnackbar } = useSnackbar()

    const { handleShakeClick, shakeSx } = useShake()

    const closeDialog = (shouldReload = false) => {
        bulkEditDialogProps.handleClickClose(shouldReload)
        setHeader("select")
    }

    const handleClickBulkEdit = async () => {
        const response = await updateItemProperties({ projectId: projectId!, itemIds: bulkEditDialogProps.itemIds, key: header, newValue })
        if (response.error) {
            enqueueSnackbar("Error occurred during bulk edit", { variant: "error" })
        } else {
            enqueueSnackbar("Bulk edited column for selected items", { variant: "success" })
            closeDialog(true)
        }
    }

    const handleChangeNewValue = (e: ChangeEvent<HTMLInputElement>) => setNewValue(e.target.value)

    const handleChangeHeader = async (event: SelectChangeEvent) => setHeader(event.target.value)

    const handleNewValueKeyPress = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault()
            if (header !== "select" && newValue) {
                await handleClickBulkEdit()
            }
        }
    }

    return (
        <Dialog
            open={bulkEditDialogProps.open}
            onClose={handleShakeClick}
            aria-labelledby="bulk-edit-dialog"
            PaperComponent={PaperComponent}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>
                <Stack spacing={1}>
                    <Stack direction="row" display="flex" justifyContent="space-between">
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Edit />
                            <Typography variant="h6">{"Bulk edit currently shown items of a column"}</Typography>
                        </Stack>
                    </Stack>
                </Stack>
            </DialogTitle>
            <DialogContent>
                <Box margin={1}>
                    <Tooltip title={header} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                        <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                            <InputLabel>Header</InputLabel>
                            <Select value={header} label="Header" onChange={handleChangeHeader}>
                                <MenuItem value="select" disabled>
                                    {"Select a header"}
                                </MenuItem>
                                {bulkEditDialogProps.headers.map(header => (
                                    <MenuItem key={header.id} value={header.id}>
                                        {header.display}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Tooltip>
                    <Box component="form" noValidate autoComplete="off">
                        <TextField
                            autoFocus
                            margin="dense"
                            value={newValue}
                            label={"New Value"}
                            onChange={handleChangeNewValue}
                            onKeyDown={handleNewValueKeyPress}
                            fullWidth
                            variant="outlined"
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <ViewColumn />
                                    </InputAdornment>
                                )
                            }}
                        />
                    </Box>
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" disabled={header === "select"} onClick={handleClickBulkEdit}>
                    {"Submit"}
                </Button>
                <Button variant="contained" color="error" onClick={() => closeDialog()} sx={shakeSx}>
                    {"Cancel"}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
