import {
    Alert,
    Autocomplete,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Paper,
    PaperProps,
    Stack,
    TextField,
    Tooltip,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../theme"
import { useTranslation } from "react-i18next"
import { Info, Input, Output, Transform } from "@mui/icons-material"
import { useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { CreateOrUpdateMappingsRequest, Mapping, MappingResponse } from "../../../features/projects/projects.types"
import { FetchBaseQueryError } from "@reduxjs/toolkit/query"
import { useSnackbar } from "notistack"
import useShake from "../../../components/shake/hooks/useShake"
import VirtualizedList from "../../../components/virtualizedList/VirtualizedList"
import useTargetSystemSection from "./components/targetSystemSection/hooks/useTargetSystemSection"
import useMappingNameSection from "./components/mappingNameSection/hooks/useMappingNameSection"
import MappingNameSection from "./components/mappingNameSection/MappingNameSection"
import TargetSystemSection from "./components/targetSystemSection/TargetSystemSection"
import { HostsApi } from "../../../features/hosts/hosts.api"
import { DataIntegrationHeaderDataAPIResponse } from "../../../features/hosts/hosts.types"
import InfoTooltip from "../../tooltip/InfoTooltip"

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

export default function CreateOrEditMappingDialog({ open, handleClickClose, scopeId, mappingToEdit }: Readonly<CreateMappingDialogProps>) {
    const { projectId } = useParams()
    const { mappingNameKey, setMappingNameKey, mappingName, setMappingName, handleMappingNameChange } = useMappingNameSection()
    const {
        host,
        setHost,
        handleHostChange,
        database,
        setDatabase,
        handleDatabaseChange,
        hostsResponse,
        isEditMode,
        openCreateOrEditHostDialog,
        handleClickOpenCreateHostDialog,
        handleClickOpenEditHostDialog,
        handleClickDeleteHost,
        handleClickCloseCreateOrEditHostDialog,
        selectedHost
    } = useTargetSystemSection()
    const [inputHeaders, setInputHeaders] = useState<string[]>([])
    const [targetHeaders, setTargetHeaders] = useState<DataIntegrationHeaderDataAPIResponse[]>([])
    const [selectedTargetHeader, setSelectedTargetHeader] = useState<DataIntegrationHeaderDataAPIResponse>({
        id: "",
        display: "",
        tooltip: ""
    })
    const [mappings, setMappings] = useState<Map<string, string>>(new Map<string, string>())

    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getHostHeaders] = HostsApi.useLazyGetHostHeadersQuery()
    const [createOrUpdateMapping] = ProjectsApi.useCreateOrUpdateMappingMutation()

    const { handleShakeClick, shakeSx } = useShake()
    const { enqueueSnackbar } = useSnackbar()
    const translation = useTranslation()

    const handleClickSubmit = async () => {
        const mapping: Mapping = {}
        mappings.forEach((source, target) => {
            if (!mapping[source]) {
                mapping[source] = []
            }
            mapping[source].push(target)
        })

        const createOrUpdateMappingRequest: CreateOrUpdateMappingsRequest = {
            projectId: projectId!,
            scopeId,
            mappingId: mappingNameKey,
            databaseId: database,
            mappingName,
            mapping
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

    const submitButtonDisabled = host === "select" || ((selectedHost?.databases.length ?? 0) > 0 && database === "select") || mappingName.trim() === ""

    const fetchMappingData = useCallback(async () => {
        if (!selectedHost) {
            return
        }

        const getHostHeadersData = await getHostHeaders({ hostId: selectedHost.id, language: "en" }).unwrap()
        const hostHeaders = getHostHeadersData.headers
        setTargetHeaders(hostHeaders)
        setSelectedTargetHeader(hostHeaders[0])

        const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
        const scopeHeaders = getScopeHeadersResponse.filter(scopeHeader => !scopeHeader.hidden).map(scopeHeader => scopeHeader.name)
        setInputHeaders(scopeHeaders)
    }, [getHostHeaders, getScopeHeaders, projectId, scopeId, selectedHost])

    useEffect(() => {
        fetchMappingData()
    }, [fetchMappingData])

    useEffect(() => {
        if (mappingToEdit) {
            setMappingNameKey(mappingToEdit.id)
            setMappingName(mappingToEdit.name)
            setHost(mappingToEdit.database.host.id)
            setDatabase(mappingToEdit.database.id)

            const editedMapping = new Map<string, string>()
            Object.entries(mappingToEdit.mapping).forEach(([target, source]) => editedMapping.set(target, source[0]))
            setMappings(editedMapping)
        }
    }, [mappingToEdit, setMappingNameKey, setMappingName, setHost, setDatabase])

    const sourceHeader = mappings.get(selectedTargetHeader.id) ?? ""

    return (
        <Dialog
            open={open}
            onClose={handleShakeClick}
            aria-labelledby="create-mapping-dialog"
            PaperComponent={PaperComponent}
            PaperProps={{
                sx: {
                    width: "1200px",
                    maxWidth: "none"
                }
            }}
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
                    <MappingNameSection mappingNameKey={mappingNameKey} mappingName={mappingName} handleMappingNameChange={handleMappingNameChange} />
                    <TargetSystemSection
                        openCreateOrEditHostDialog={openCreateOrEditHostDialog}
                        handleClickCloseCreateOrEditHostDialog={handleClickCloseCreateOrEditHostDialog}
                        host={host}
                        database={database}
                        hostsResponse={hostsResponse}
                        handleHostChange={handleHostChange}
                        handleDatabaseChange={handleDatabaseChange}
                        isEditMode={isEditMode}
                        handleClickOpenCreateHostDialog={handleClickOpenCreateHostDialog}
                        handleClickOpenEditHostDialog={handleClickOpenEditHostDialog}
                        handleClickDeleteHost={handleClickDeleteHost}
                        selectedHost={selectedHost}
                    />
                    <Paper sx={{ padding: "25px" }}>
                        <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                            Mapping of headers
                        </Typography>
                        {!selectedHost && <Alert severity="warning">{"Select a host (target system)"}</Alert>}
                        {selectedHost && (
                            <Stack justifyContent="center" spacing={5} direction="row">
                                <Stack alignItems="center">
                                    <Stack alignItems="center" direction="row" spacing={1}>
                                        <Input />
                                        <Typography variant="h6">Source</Typography>
                                    </Stack>
                                    <Autocomplete
                                        disablePortal
                                        value={sourceHeader}
                                        options={inputHeaders}
                                        isOptionEqualToValue={(option, value) => option === value}
                                        onChange={(_, value) => {
                                            if (value) {
                                                setMappings(prevMappings => {
                                                    const newMappings = new Map(prevMappings)
                                                    newMappings.set(selectedTargetHeader.id, value)
                                                    return newMappings
                                                })
                                            } else {
                                                setMappings(prevMappings => {
                                                    const newMappings = new Map(prevMappings)
                                                    newMappings.delete(selectedTargetHeader.id)
                                                    return newMappings
                                                })
                                            }
                                        }}
                                        sx={{ width: 300 }}
                                        renderInput={params => (
                                            <TextField
                                                {...params}
                                                placeholder={"Choose a source..."}
                                                InputLabelProps={{
                                                    shrink: true
                                                }}
                                            />
                                        )}
                                    />
                                </Stack>
                                <Stack alignItems="center">
                                    <Stack alignItems="center" direction="row" spacing={1}>
                                        <Output />
                                        <Typography variant="h6">Target</Typography>
                                    </Stack>

                                    <VirtualizedList
                                        fixedSizeListProps={{
                                            width: 400,
                                            height: 300,
                                            itemSize: 50,
                                            itemCount: targetHeaders.length,
                                            style: {
                                                overflow: "auto",
                                                border: `1px solid ${theme.palette.divider}`,
                                                borderRadius: "8px",
                                                padding: "5px"
                                            }
                                        }}
                                        items={targetHeaders}
                                    >
                                        {(targetHeader: DataIntegrationHeaderDataAPIResponse) => (
                                            <ListItemButton
                                                disableRipple
                                                selected={selectedTargetHeader.id === targetHeader.id}
                                                onClick={() => setSelectedTargetHeader(targetHeader)}
                                            >
                                                <ListItemIcon>
                                                    <Output />
                                                </ListItemIcon>
                                                <Tooltip
                                                    title={targetHeader.id}
                                                    arrow
                                                    PopperProps={{ style: { zIndex: theme.zIndex.modal }, disablePortal: true }}
                                                >
                                                    <ListItemText
                                                        primary={targetHeader.id}
                                                        primaryTypographyProps={{
                                                            style: {
                                                                whiteSpace: "nowrap",
                                                                textOverflow: "ellipsis",
                                                                overflow: "hidden"
                                                            }
                                                        }}
                                                    />
                                                </Tooltip>
                                                <InfoTooltip placement="right" messages={[targetHeader.tooltip]}>
                                                    <Info color="info" />
                                                </InfoTooltip>
                                            </ListItemButton>
                                        )}
                                    </VirtualizedList>
                                </Stack>
                            </Stack>
                        )}
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
    )
}
