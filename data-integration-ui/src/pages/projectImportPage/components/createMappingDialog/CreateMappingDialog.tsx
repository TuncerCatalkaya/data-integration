import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    IconButton,
    InputLabel,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
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
import theme from "../../../../theme"
import {useTranslation} from "react-i18next"
import {FixedSizeList} from "react-window"
import {Add, Delete, Edit, Input, Output, Transform} from "@mui/icons-material"
import {ChangeEvent, CSSProperties, FC, useCallback, useEffect, useRef, useState} from "react"
import CreateOrEditHostDialog from ".././createOrEditHostDialog/CreateOrEditHostDialog"
import {HostsApi} from "../../../../features/hosts/hosts.api"
import {Host} from "../../../../features/hosts/hosts.types"
import useConfirmationDialog from "../../../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../../../components/confirmationDialog/ConfirmationDialog"
import {ProjectsApi} from "../../../../features/projects/projects.api"
import {useParams} from "react-router-dom"
import {v4 as uuidv4} from "uuid"
import {CreateOrUpdateMappingsRequest, MappingResponse} from "../../../../features/projects/projects.types"
import {FetchBaseQueryError} from "@reduxjs/toolkit/query"
import {useSnackbar} from "notistack"
import useShake from "../../../../components/shake/hooks/useShake";

interface CreateMappingDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    scopeId: string
    mappingToEdit?: MappingResponse
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-mapping-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

interface MappingsInput {
    id: string
    header: string
    values: MappingsValuesInput[]
}

interface MappingsValuesInput {
    id: string
    value: string
}

export default function CreateMappingDialog({ open, handleClickClose, scopeId, mappingToEdit }: Readonly<CreateMappingDialogProps>) {
    const { projectId } = useParams()
    const [host, setHost] = useState("select")
    const [database, setDatabase] = useState("select")
    const [mappingNameKey, setMappingNameKey] = useState("")
    const [mappingName, setMappingName] = useState("")
    const [hostsResponse, setHostsResponse] = useState<Host[]>([])
    const [mappings, setMappings] = useState<MappingsInput[]>([])
    const [selectedMapping, setSelectedMapping] = useState<MappingsInput>({
        id: "",
        header: "",
        values: []
    })

    const [openCreateOrEditHostDialog, setOpenCreateOrEditHostDialog] = useState(false)
    const [isEditMode, setIsEditMode] = useState(false)

    const [getHosts] = HostsApi.useLazyGetHostsQuery()
    const [deleteHost] = HostsApi.useDeleteHostMutation()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [createOrUpdateMapping] = ProjectsApi.useCreateOrUpdateMappingMutation()

    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()

    const { handleShakeClick, shakeSx } = useShake();
    
    const { enqueueSnackbar } = useSnackbar()

    const translation = useTranslation()
    
    const handleClickOpenCreateHostDialog = () => {
        setIsEditMode(false)
        setOpenCreateOrEditHostDialog(true)
    }
    const handleClickOpenEditHostDialog = () => {
        setIsEditMode(true)
        setOpenCreateOrEditHostDialog(true)
    }
    const handleClickCloseCreateOrEditHostDialog = async (shouldReload = false) => {
        setOpenCreateOrEditHostDialog(false)
        if (shouldReload) {
            await fetchHostsData()
        }
    }

    const handleClickDeleteHost = async () => {
        await deleteHost({ hostId: selectedHost!.id })
        await fetchHostsData()
        setHost("select")
        setDatabase("select")
    }

    const selectedHost = hostsResponse.find(h => h.id === host)
    const handleHostChange = async (event: SelectChangeEvent) => {
        const newHost = event.target.value
        setHost(newHost)
        setDatabase("select")
    }

    const handleDatabaseChange = async (event: SelectChangeEvent) => {
        const newDatabase = event.target.value
        setDatabase(newDatabase)
    }

    const debounceTimeoutRef = useRef<number | null>(null)
    const handleMappingNameChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newMappingName = event.target.value

        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current)
        }

        debounceTimeoutRef.current = setTimeout(() => {
            setMappingName(newMappingName)
        }, 100)
    }

    const handleClickSubmit = async () => {
        const createOrUpdateMappingRequest: CreateOrUpdateMappingsRequest = {
            projectId: projectId!,
            scopeId,
            mappingId: mappingToEdit?.id ?? "",
            databaseId: database,
            mappingName,
            mapping: Object.assign({}, ...mappings.map(mapping => ({ [mapping.header]: mapping.values.map(value => value.value) })))
        }
        const createOrUpdateMappingResponse = await createOrUpdateMapping(createOrUpdateMappingRequest)

        if (createOrUpdateMappingResponse.data) {
            handleClickClose(true)
            enqueueSnackbar(mappingToEdit ? "Mapping edited" : "Mapping created", { variant: "success" })
        } else if (createOrUpdateMappingResponse.error) {
            const createOrUpdateMappingResponseError = createOrUpdateMappingResponse.error as FetchBaseQueryError
            enqueueSnackbar("Error occurred during mapping creation", { variant: "error" })
            if (createOrUpdateMappingResponseError.status === 409) {
                alert(createOrUpdateMappingResponseError.data)
            }
        }
    }

    const submitButtonDisabled = host === "select" || database === "select" || mappingName.trim() === ""

    const fetchScopeHeadersData = useCallback(async () => {
        const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
        if (mappingToEdit) {
            setMappingNameKey(mappingToEdit.id)
            setMappingName(mappingToEdit.name)
            setHost(mappingToEdit.database.host.id)
            setDatabase(mappingToEdit.database.id)
        }
        const mappings = getScopeHeadersResponse.map(scopeHeader => ({
            id: uuidv4(),
            header: scopeHeader.name,
            values: (mappingToEdit?.mapping[scopeHeader.name] ?? [scopeHeader.name]).map(value => ({
                id: uuidv4(),
                value
            }))
        }))
        setMappings(mappings)
        setSelectedMapping(mappings[0])
    }, [getScopeHeaders, mappingToEdit, projectId, scopeId])

    useEffect(() => {
        fetchScopeHeadersData()
    }, [fetchScopeHeadersData])

    const fetchHostsData = useCallback(async () => {
        const hostsResponse = await getHosts().unwrap()
        setHostsResponse(hostsResponse)
    }, [getHosts])

    useEffect(() => {
        fetchHostsData()
    }, [fetchHostsData])

    const MappingRow: FC<{ index: number; style: CSSProperties }> = ({ index, style }) => {
        const mapping = mappings[index]
        return (
            <div style={style}>
                <Tooltip title={mapping.header} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal }, disablePortal: true }}>
                    <ListItemButton disableRipple selected={selectedMapping.id === mapping.id} onClick={() => setSelectedMapping(mapping)}>
                        <ListItemIcon>
                            <Input />
                        </ListItemIcon>
                        <ListItemText
                            primary={mapping.header}
                            primaryTypographyProps={{
                                style: {
                                    whiteSpace: "nowrap",
                                    textOverflow: "ellipsis",
                                    overflow: "hidden"
                                }
                            }}
                        />
                    </ListItemButton>
                </Tooltip>
            </div>
        )
    }

    type ActionType = "add" | "delete" | "update"

    const handleMappingChange = useCallback((action: ActionType, index?: number, value?: string) => {
        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current)
        }

        debounceTimeoutRef.current = setTimeout(() => {
            setSelectedMapping(prevState => {
                const updatedValues = [...prevState.values]

                switch (action) {
                    case "add": {
                        const id = uuidv4()
                        updatedValues.push({
                            id,
                            value: id
                        })
                        break
                    }
                    case "delete": {
                        updatedValues.splice(index!, 1)
                        break
                    }
                    case "update": {
                        updatedValues[index!] = {
                            id: updatedValues[index!].id,
                            value: value!
                        }
                        break
                    }
                    default: {
                        break
                    }
                }

                const updatedMapping = {
                    ...prevState,
                    values: updatedValues
                }

                setMappings(prevMappings =>
                    prevMappings.map(mapping => {
                        if (mapping.id === updatedMapping.id) {
                            return {
                                ...mapping,
                                ...updatedMapping
                            }
                        }
                        return mapping
                    })
                )

                return updatedMapping
            })
        }, 100)
    }, [])

    return (
        <>
            {openCreateOrEditHostDialog && (
                <CreateOrEditHostDialog
                    open={openCreateOrEditHostDialog}
                    handleClickClose={handleClickCloseCreateOrEditHostDialog}
                    hostToEdit={isEditMode ? selectedHost : undefined}
                />
            )}
            {openConfirmationDialog && (
                <ConfirmationDialog open={openConfirmationDialog} handleClickClose={handleClickCloseConfirmationDialog} handleClickYes={handleClickDeleteHost}>
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to delete the host?</Typography>
                        <Stack>
                            <Typography variant="body1">
                                {"Host name: "} <strong>{selectedHost!.name}</strong>
                            </Typography>
                            <Typography variant="body1">
                                {"Host URL: "} <strong>{selectedHost!.url}</strong>
                            </Typography>
                        </Stack>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Dialog
                open={open}
                onClose={handleShakeClick}
                aria-labelledby="create-mapping-dialog"
                PaperComponent={PaperComponent}
                sx={{ zIndex: theme.zIndex.modal }}
            >
                <DialogTitle sx={{ cursor: "move" }}>
                    <Stack spacing={1}>
                        <Stack direction="row" display="flex" justifyContent="space-between">
                            <Stack direction="row" alignItems="center" spacing={1}>
                                <Transform />
                                <Typography variant="h6">{translation.t("pages.projectImport.components.dialogs.createMappingDialog.title")}</Typography>
                            </Stack>
                        </Stack>
                    </Stack>
                </DialogTitle>
                <DialogContent>
                    <Stack spacing={2}>
                        <Paper sx={{ padding: "25px" }}>
                            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                                Decide a name for the mapping
                            </Typography>
                            <TextField
                                key={mappingNameKey}
                                fullWidth
                                label={"Name"}
                                placeholder={"Enter a name..."}
                                defaultValue={mappingName}
                                InputLabelProps={{
                                    shrink: true
                                }}
                                onChange={handleMappingNameChange}
                            />
                        </Paper>
                        <Paper sx={{ padding: "25px" }}>
                            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                                Select a target system
                            </Typography>
                            <Stack direction="row" alignItems="center" spacing={2} sx={{ padding: "5px" }}>
                                <Stack spacing={2}>
                                    <Stack direction="row" spacing={2}>
                                        <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px", maxWidth: "200px" }}>
                                            <InputLabel>Host</InputLabel>
                                            <Select value={host} label="host" onChange={handleHostChange}>
                                                <MenuItem value="select" disabled>
                                                    {"Select a host"}
                                                </MenuItem>
                                                {hostsResponse.map(host => (
                                                    <MenuItem key={host.id} value={host.id}>
                                                        {host.name}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                        <Stack direction="row" spacing={1}>
                                            <Button
                                                variant="contained"
                                                color="success"
                                                onClick={handleClickOpenCreateHostDialog}
                                                sx={{ color: theme.palette.common.white }}
                                            >
                                                <Add />
                                            </Button>
                                            <Button
                                                disabled={!selectedHost}
                                                variant="contained"
                                                color="warning"
                                                onClick={handleClickOpenEditHostDialog}
                                                sx={{ color: theme.palette.common.white }}
                                            >
                                                <Edit />
                                            </Button>
                                            <Button
                                                disabled={!selectedHost || selectedHost.inUse}
                                                variant="contained"
                                                color="error"
                                                onClick={handleClickOpenConfirmationDialog}
                                            >
                                                <Delete />
                                            </Button>
                                        </Stack>
                                    </Stack>
                                    <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px", maxWidth: "200px" }}>
                                        <InputLabel>Database</InputLabel>
                                        <Select value={database} label="database" onChange={handleDatabaseChange}>
                                            <MenuItem value="select" disabled>
                                                {"Select a database"}
                                            </MenuItem>
                                            {selectedHost?.databases?.map(database => (
                                                <MenuItem key={database.id} value={database.id}>
                                                    {database.name}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Stack>
                            </Stack>
                        </Paper>
                        <Paper sx={{ padding: "25px" }}>
                            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                                Mapping of headers
                            </Typography>
                            <Stack spacing={5}>
                                <Stack alignItems="center">
                                    <Stack alignItems="center" direction="row" spacing={1}>
                                        <Input />
                                        <Typography variant="h6">Source</Typography>
                                    </Stack>

                                    <FixedSizeList
                                        width={400}
                                        height={300}
                                        itemSize={50}
                                        itemCount={mappings.length}
                                        style={{
                                            overflow: "auto",
                                            border: `1px solid ${theme.palette.divider}`,
                                            borderRadius: "8px",
                                            padding: "5px"
                                        }}
                                    >
                                        {MappingRow}
                                    </FixedSizeList>
                                </Stack>
                                <Stack alignItems="center">
                                    <Stack alignItems="center" direction="row" spacing={1}>
                                        <Typography variant="h6">Target</Typography>
                                        <Output />
                                    </Stack>

                                    <div>
                                        <List
                                            sx={{
                                                minWidth: "400px",
                                                maxWidth: "400px",
                                                minHeight: "200px",
                                                maxHeight: "200px",
                                                overflow: "auto",
                                                border: "1px solid " + theme.palette.divider,
                                                borderRadius: "8px",
                                                padding: "5px"
                                            }}
                                        >
                                            {selectedMapping.values.map((value, index) => (
                                                <ListItem key={value.id}>
                                                    <ListItemIcon>
                                                        <Output />
                                                    </ListItemIcon>
                                                    <ListItemText
                                                        primary={
                                                            <TextField
                                                                variant={"standard"}
                                                                fullWidth
                                                                defaultValue={value.value}
                                                                onChange={e => handleMappingChange("update", index, e.target.value)}
                                                            />
                                                        }
                                                        sx={{ paddingRight: "25px" }}
                                                    />
                                                    <IconButton edge="end" onClick={() => handleMappingChange("delete", index)}>
                                                        <Delete />
                                                    </IconButton>
                                                </ListItem>
                                            ))}
                                        </List>
                                        <ListItemButton
                                            onClick={() => handleMappingChange("add")}
                                            sx={{
                                                backgroundColor: theme.palette.action.hover,
                                                border: "2px dashed " + theme.palette.primary.main,
                                                borderRadius: "8px",
                                                marginTop: "10px",
                                                "&:hover": {
                                                    backgroundColor: theme.palette.action.selected,
                                                    borderColor: theme.palette.primary.dark
                                                }
                                            }}
                                        >
                                            <ListItemText
                                                primary={
                                                    <Stack
                                                        direction="row"
                                                        justifyContent="center"
                                                        alignItems="center"
                                                        sx={{ fontWeight: "bold", color: theme.palette.primary.main }}
                                                    >
                                                        <Add sx={{ fontSize: "1.5rem" }} />
                                                        {"Add new target mapping"}
                                                        <Add sx={{ fontSize: "1.5rem" }} />
                                                    </Stack>
                                                }
                                            />
                                        </ListItemButton>
                                    </div>
                                </Stack>
                            </Stack>
                        </Paper>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button variant="contained" disabled={submitButtonDisabled} onClick={handleClickSubmit}>
                        {"Submit"}
                    </Button>
                    <Button variant="contained" color="error" onClick={() => handleClickClose()} sx={shakeSx}>
                        {"Cancel"}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    )
}
