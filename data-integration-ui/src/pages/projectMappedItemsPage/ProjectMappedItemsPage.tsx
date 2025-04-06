import {
    Alert,
    Box,
    Button,
    Checkbox,
    FormControl,
    FormControlLabel,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
    Stack,
    Tooltip,
    Typography
} from "@mui/material"
import { useParams } from "react-router-dom"
import { ChangeEvent, useCallback, useEffect, useRef, useState } from "react"
import { ProjectsApi } from "../../features/projects/projects.api"
import { useSnackbar } from "notistack"
import { Add, CompareArrows, Delete, Edit, LinkOff } from "@mui/icons-material"
import { MappedItemResponse, MappingResponse, ScopeHeaderResponse, ScopeResponse } from "../../features/projects/projects.types"
import usePagination from "../../components/pagination/hooks/usePagination"
import theme from "../../theme"
import { ColDef, GridApi } from "ag-grid-community"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import MappedItemsTable from "./components/mappedItemsTable/MappedItemsTable"
import { useAppDispatch, useAppSelector } from "../../store/store"
import MappedItemsSlice from "../../features/mappedItems/mappedItems.slice"
import CreateOrEditMappingDialog from "../../components/dialogs/createMappingDialog/CreateOrEditMappingDialog"
import ImportItemsSlice from "../../features/importItems/importItems.slice"
import { HostsApi } from "../../features/hosts/hosts.api"
import { DataIntegrationHeaderAPIResponse } from "../../features/hosts/hosts.types"
import { FetchBaseQueryError } from "@reduxjs/toolkit/query"

export default function ProjectMappedItemsPage() {
    const { projectId } = useParams()
    const gridApiRef = useRef<GridApi | null>(null)
    const scopesFromStore = useAppSelector<Record<string, string>>(state => state.mappedItems.scopes)
    const mappingsFromStore = useAppSelector<Record<string, string>>(state => state.mappedItems.mappings)
    const importMappingsFromStore = useAppSelector<Record<string, string>>(state => state.importItems.mappings)
    const filterIntegratedItemsFromStore = useAppSelector<Record<string, boolean>>(state => state.mappedItems.filterIntegratedItems)
    const dispatch = useAppDispatch()

    const [openCreateMappingDialog, setOpenCreateMappingDialog] = useState(false)
    const [isMappingEditMode, setIsMappingEditMode] = useState(false)

    const [scope, setScope] = useState(scopesFromStore[projectId!] || "select")
    const [scopesResponse, setScopesResponse] = useState<ScopeResponse[]>([])

    const [mapping, setMapping] = useState(mappingsFromStore[projectId!] || "select")
    const [mappingsResponse, setMappingsResponse] = useState<MappingResponse[]>([])

    const [getHostHeadersResponse, setGetHostHeadersResponse] = useState<DataIntegrationHeaderAPIResponse>({
        headers: []
    })

    const [selectedItems, setSelectedItems] = useState<string[]>([])

    const [checkedFilterIntegratedItems, setCheckedFilterIntegratedItems] = useState(filterIntegratedItemsFromStore[projectId!] || false)

    const {
        openConfirmationDialog: openMappingDeleteConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseMappingDeleteConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenMappingDeleteConfirmationDialog
    } = useConfirmationDialog()

    const {
        openConfirmationDialog: openUnmapSelectedConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseUnmapSelectedConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenUnmapSelectedConfirmationDialog
    } = useConfirmationDialog()

    const {
        openConfirmationDialog: openIntegrateSelectedConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseIntegrateSelectedConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenIntegrateSelectedConfirmationDialog
    } = useConfirmationDialog()

    const pagination = usePagination()
    const page = pagination.page
    const pageSize = pagination.pageSize
    const sort = pagination.sort
    const setTotalElements = pagination.setTotalElements

    const [getScopes] = ProjectsApi.useLazyGetScopesQuery()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getHostHeaders] = HostsApi.useLazyGetHostHeadersQuery()
    const [getMappings] = ProjectsApi.useLazyGetMappingsQuery()
    const [getMappedItems] = ProjectsApi.useLazyGetMappedItemsQuery()
    const [markMappingForDeletion] = ProjectsApi.useMarkMappingForDeletionMutation()
    const [applyUnmapping] = ProjectsApi.useApplyUnmappingMutation()
    const [integrate] = ProjectsApi.useIntegrateMutation()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickCloseCreateMappingDialog = async (shouldReload = false) => {
        setOpenCreateMappingDialog(false)
        if (shouldReload) {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId: scope }).unwrap()
            setMappingsResponse(getMappingsResponse)
        }
    }
    const handleClickOpenCreateMappingDialog = () => {
        setIsMappingEditMode(false)
        setOpenCreateMappingDialog(true)
    }
    const handleClickOpenEditMappingDialog = () => {
        setIsMappingEditMode(true)
        setOpenCreateMappingDialog(true)
    }

    const handleFilterMappedItemsChange = (e: ChangeEvent<HTMLInputElement>) => {
        const checkedFilterIntegratedItems = e.target.checked
        setCheckedFilterIntegratedItems(checkedFilterIntegratedItems)
        dispatch(MappedItemsSlice.actions.putFilterIntegratedItem({ projectId: projectId!, filterIntegratedItem: checkedFilterIntegratedItems }))
    }

    const selectedScope = scopesResponse.find(s => s.id === scope)
    const handleScopeChange = async (event: SelectChangeEvent) => {
        const newScope = event.target.value
        setScope(newScope)
        dispatch(MappedItemsSlice.actions.putScope({ projectId: projectId!, scope: newScope }))
        setCheckedFilterIntegratedItems(false)
        dispatch(MappedItemsSlice.actions.putFilterIntegratedItem({ projectId: projectId!, filterIntegratedItem: false }))
        if (selectedMapping) {
            setMapping("select")
            dispatch(MappedItemsSlice.actions.putMapping({ projectId: projectId!, mapping: "select" }))
            setColumnDefs([])
            setRowData([])
            setTotalElements(0)
        }
    }

    const selectedMapping = mappingsResponse.find(m => m.id === mapping)
    const handleMappingChange = async (event: SelectChangeEvent) => {
        const newMapping = event.target.value
        setMapping(newMapping)
        dispatch(MappedItemsSlice.actions.putMapping({ projectId: projectId!, mapping: newMapping }))
        setCheckedFilterIntegratedItems(false)
        dispatch(MappedItemsSlice.actions.putFilterIntegratedItem({ projectId: projectId!, filterIntegratedItem: false }))
    }

    const [rowData, setRowData] = useState<MappedItemResponse[]>([])
    const [scopeHeaders, setScopeHeaders] = useState<ScopeHeaderResponse[]>([])
    const [columnDefs, setColumnDefs] = useState<ColDef[]>([])

    const handleClickDeleteMapping = async () => {
        await markMappingForDeletion({ projectId: projectId!, mappingId: mapping })
        await fetchMappingsData(selectedScope!.id)
        setMapping("select")
        dispatch(MappedItemsSlice.actions.putMapping({ projectId: projectId!, mapping: "select" }))
        if (importMappingsFromStore[projectId!] === mapping) {
            dispatch(ImportItemsSlice.actions.putMapping({ projectId: projectId!, mapping: "select" }))
        }
        setColumnDefs([])
        setRowData([])
        setTotalElements(0)
        enqueueSnackbar("Deleted mapping", { variant: "success" })
    }

    const handleClickUnmapSelected = async () => {
        const applyMappingResponse = await applyUnmapping({ projectId: projectId!, mappedItemIds: selectedItems })
        if (applyMappingResponse.error) {
            enqueueSnackbar("Error occurred during unmapping", { variant: "error" })
        } else {
            await fetchMappedItemsData(scope, mapping, page, pageSize, sort)
            enqueueSnackbar("Applied unmapping", { variant: "success" })
        }
    }

    const handleClickIntegrateSelected = async () => {
        const integrateResponse = await integrate({ projectId: projectId!, mappingId: selectedMapping!.id, language: "en", mappedItemIds: selectedItems })
        if (integrateResponse.error) {
            const integrateResponseError = integrateResponse.error as FetchBaseQueryError
            if (integrateResponseError.status === 401 || integrateResponseError.status === 403) {
                enqueueSnackbar("You dont have permissions. Check database of mapping or contact an administrator.", { variant: "error" })
            } else {
                enqueueSnackbar("Error occurred during integration", { variant: "error" })
            }
        } else {
            await fetchMappedItemsData(scope, mapping, page, pageSize, sort)
            if (gridApiRef.current) {
                gridApiRef.current?.deselectAll()
            }
            enqueueSnackbar("Integration completed, please look at each result", { variant: "warning" })
        }
    }

    const fetchMappedItemsData = useCallback(
        async (scopeId: string, mappingId: string, page: number, pageSize: number, sort?: string) => {
            const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
            setScopeHeaders(getScopeHeadersResponse)
            const getMappedItemsResponse = await getMappedItems({
                projectId: projectId!,
                mappingId: mappingId,
                filterIntegratedItems: checkedFilterIntegratedItems,
                page,
                size: pageSize,
                sort
            }).unwrap()
            setRowData(getMappedItemsResponse.content)
            setTotalElements(getMappedItemsResponse.totalElements)
        },
        [getScopeHeaders, projectId, getMappedItems, checkedFilterIntegratedItems, setTotalElements]
    )

    useEffect(() => {
        if (selectedMapping && selectedScope) {
            fetchMappedItemsData(selectedScope.id, selectedMapping.id, page, pageSize, sort)
        }
    }, [fetchMappedItemsData, selectedMapping, selectedScope, page, pageSize, sort])

    const fetchScopesData = useCallback(async () => {
        const getScopesResponse = await getScopes({ projectId: projectId! }).unwrap()
        setScopesResponse(getScopesResponse)
    }, [projectId, getScopes])

    useEffect(() => {
        fetchScopesData()
    }, [fetchScopesData])

    const fetchMappingsData = useCallback(
        async (scopeId: string) => {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId }).unwrap()
            setMappingsResponse(getMappingsResponse)
        },
        [projectId, getMappings]
    )

    useEffect(() => {
        if (selectedScope) {
            fetchMappingsData(selectedScope.id)
        }
    }, [selectedScope, fetchMappingsData])

    const fetchHostHeadersData = useCallback(async () => {
        const getHostHeadersResponse = await getHostHeaders({ hostId: selectedMapping!.database.host.id, language: "en" }).unwrap()
        setGetHostHeadersResponse(getHostHeadersResponse)
    }, [getHostHeaders, selectedMapping])

    useEffect(() => {
        if (selectedMapping) {
            fetchHostHeadersData()
        }
    }, [fetchHostHeadersData, selectedMapping])

    const missingHeaders = getHostHeadersResponse.headers.map(header => header.id).filter(headerId => !selectedMapping?.mapping[headerId])

    return (
        <>
            {openCreateMappingDialog && (
                <CreateOrEditMappingDialog
                    open={openCreateMappingDialog}
                    handleClickClose={handleClickCloseCreateMappingDialog}
                    scopeId={scope}
                    mappingToEdit={isMappingEditMode ? selectedMapping : undefined}
                />
            )}
            {openMappingDeleteConfirmationDialog && (
                <ConfirmationDialog
                    open={openMappingDeleteConfirmationDialog}
                    handleClickClose={handleClickCloseMappingDeleteConfirmationDialog}
                    handleClickYes={handleClickDeleteMapping}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to delete the mapping?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            {openUnmapSelectedConfirmationDialog && (
                <ConfirmationDialog
                    open={openUnmapSelectedConfirmationDialog}
                    handleClickClose={handleClickCloseUnmapSelectedConfirmationDialog}
                    handleClickYes={handleClickUnmapSelected}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to unmap the selected mapped items?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            {openIntegrateSelectedConfirmationDialog && (
                <ConfirmationDialog
                    open={openIntegrateSelectedConfirmationDialog}
                    handleClickClose={handleClickCloseIntegrateSelectedConfirmationDialog}
                    handleClickYes={handleClickIntegrateSelected}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to integrate the selected mapped items?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            <Stack spacing={2} width="100vw">
                <Stack spacing={2} justifyContent="space-between" direction="row">
                    <Stack spacing={2} direction="row">
                        <Tooltip title={selectedScope?.key} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                                <InputLabel>Scope</InputLabel>
                                <Select value={scope} label="Scope" onChange={handleScopeChange}>
                                    <MenuItem value="select" disabled>
                                        {"Select a scope"}
                                    </MenuItem>
                                    {scopesResponse.map(scope => (
                                        <MenuItem key={scope.id} value={scope.id}>
                                            {scope.key}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Tooltip>
                        <Tooltip title={selectedMapping?.name} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <FormControl
                                disabled={!selectedScope}
                                sx={{ backgroundColor: theme.palette.common.white, minWidth: 425, maxWidth: 425, textAlign: "left" }}
                            >
                                <InputLabel>Mapping</InputLabel>
                                <Select value={mapping} label="Mapping" onChange={handleMappingChange}>
                                    <MenuItem value="select" disabled>
                                        {"Select a mapping"}
                                    </MenuItem>
                                    {mappingsResponse.map(mapping => (
                                        <MenuItem key={mapping.id} value={mapping.id}>
                                            {mapping.name}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Tooltip>
                    </Stack>
                    <Stack direction="row" spacing={2}>
                        <Box>
                            <Button
                                disabled={!selectedScope}
                                variant="contained"
                                color="success"
                                onClick={handleClickOpenCreateMappingDialog}
                                endIcon={<Add />}
                                sx={{ color: theme.palette.common.white }}
                            >
                                Add
                            </Button>
                        </Box>
                        <Box>
                            <Button
                                disabled={!selectedMapping}
                                variant="contained"
                                color="warning"
                                onClick={handleClickOpenEditMappingDialog}
                                endIcon={<Edit />}
                                sx={{ color: theme.palette.common.white }}
                            >
                                Edit
                            </Button>
                        </Box>
                        <Box>
                            <Button
                                disabled={!selectedMapping}
                                variant="contained"
                                color="error"
                                onClick={handleClickOpenMappingDeleteConfirmationDialog}
                                endIcon={<Delete />}
                            >
                                Delete
                            </Button>
                        </Box>
                    </Stack>
                </Stack>
                <Stack spacing={2} justifyContent="space-between" direction="row">
                    <Stack direction="row">
                        <FormControlLabel
                            disabled={!selectedMapping}
                            control={<Checkbox color="info" checked={checkedFilterIntegratedItems} onChange={handleFilterMappedItemsChange} />}
                            label="Hide integrated items"
                        />
                        <Tooltip title={"Apply unmapping of selected items"} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <Button
                                disabled={selectedItems.length <= 0 || mapping === "select"}
                                color="info"
                                variant="contained"
                                onClick={handleClickOpenUnmapSelectedConfirmationDialog}
                            >
                                <Stack direction="row" spacing={2}>
                                    <Typography>Unmap selected</Typography>
                                    <LinkOff />
                                </Stack>
                            </Button>
                        </Tooltip>
                    </Stack>
                    <Stack direction="row" spacing={3}>
                        {rowData.length > 0 && missingHeaders.length > 0 && (
                            <Tooltip
                                title={
                                    <div>
                                        <div>{"Edit mapping and map these missing headers:"}</div>
                                        {missingHeaders.map(header => (
                                            <div key={header}>{header}</div>
                                        ))}
                                    </div>
                                }
                                arrow
                                PopperProps={{ style: { zIndex: theme.zIndex.modal } }}
                            >
                                <Alert severity="warning" sx={{ cursor: "help" }}>
                                    {"Headers missing for integration. Hover here to see."}
                                </Alert>
                            </Tooltip>
                        )}
                        <Tooltip title={"Integrate selected items"} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <Button
                                disabled={selectedItems.length <= 0 || mapping === "select" || missingHeaders.length > 0}
                                color="error"
                                variant="contained"
                                onClick={handleClickOpenIntegrateSelectedConfirmationDialog}
                                sx={{ color: theme.palette.common.white, backgroundColor: "#C72E49" }}
                            >
                                <Stack direction="row" spacing={2}>
                                    <Typography>Integrate selected</Typography>
                                    <CompareArrows />
                                </Stack>
                            </Button>
                        </Tooltip>
                    </Stack>
                </Stack>
                <MappedItemsTable
                    gridApiRef={gridApiRef}
                    rowData={rowData}
                    scopeHeaders={scopeHeaders}
                    selectedScope={selectedScope}
                    selectedMapping={selectedMapping}
                    getHostHeadersResponse={getHostHeadersResponse}
                    columnDefs={columnDefs}
                    setColumnDefs={setColumnDefs}
                    setSelectedItems={setSelectedItems}
                    mapping={mapping}
                    fetchMappedItemsData={fetchMappedItemsData}
                    {...pagination}
                />
            </Stack>
        </>
    )
}
