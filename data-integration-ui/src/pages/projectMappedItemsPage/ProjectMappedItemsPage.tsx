import {
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
import {useParams} from "react-router-dom"
import {ChangeEvent, useCallback, useEffect, useState} from "react"
import {ProjectsApi} from "../../features/projects/projects.api"
import {useSnackbar} from "notistack"
import {Add, Delete, Edit, LinkOff} from "@mui/icons-material"
import {
    MappedItemResponse,
    MappingResponse,
    ScopeHeaderResponse,
    ScopeResponse
} from "../../features/projects/projects.types"
import usePagination from "../../components/pagination/hooks/usePagination"
import theme from "../../theme"
import {ColDef} from "ag-grid-community"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import MappedItemsTable from "./components/mappedItemsTable/MappedItemsTable"
import {useAppDispatch, useAppSelector} from "../../store/store"
import MappedItemsSlice from "../../features/mappedItems/mappedItems.slice"
import CreateMappingDialog from "../projectImportPage/components/createMappingDialog/CreateMappingDialog"
import ImportItemsSlice from "../../features/importItems/importItems.slice"

export default function ProjectMappedItemsPage() {
    const { projectId } = useParams()
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

    const [selectedItems, setSelectedItems] = useState<string[]>([])

    const [checkedFilterIntegratedItems, setCheckedFilterIntegratedItems] = useState(filterIntegratedItemsFromStore[projectId!] || false)

    const {
        openConfirmationDialog: openMappingDeleteConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseMappingDeleteConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenMappingDeleteConfirmationDialog
    } = useConfirmationDialog()

    const pagination = usePagination()
    const page = pagination.page
    const pageSize = pagination.pageSize
    const sort = pagination.sort
    const setTotalElements = pagination.setTotalElements

    const [getScopes] = ProjectsApi.useLazyGetScopesQuery()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getItems] = ProjectsApi.useLazyGetItemsQuery()
    const [getMappings] = ProjectsApi.useLazyGetMappingsQuery()
    const [getMappedItems] = ProjectsApi.useLazyGetMappedItemsQuery()
    const [markMappingForDeletion] = ProjectsApi.useMarkMappingForDeletionMutation()
    const [applyUnmapping] = ProjectsApi.useApplyUnmappingMutation()

    const { enqueueSnackbar } = useSnackbar()

    const handleClickCloseCreateMappingDialog = async (shouldReload = false) => {
        console.log(getItems)
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

    const handleClickApplyUnmapping = async () => {
        const applyMappingResponse = await applyUnmapping({ projectId: projectId!, mappedItemIds: selectedItems })
        if (applyMappingResponse.error) {
            enqueueSnackbar("Error occurred during unmapping", { variant: "error" })
        } else {
            await fetchMappedItemsData(scope, mapping, page, pageSize, sort)
            enqueueSnackbar("Applied unmapping", { variant: "success" })
        }
    }

    const fetchMappedItemsData = useCallback(
        async (scopeId: string, mappingId: string, page: number, pageSize: number, sort?: string) => {
            const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
            setScopeHeaders(getScopeHeadersResponse)
            const getMappedItemsResponse = await getMappedItems({
                projectId: projectId!,
                mappingId: mappingId,
                page,
                size: pageSize,
                sort
            }).unwrap()
            setRowData(getMappedItemsResponse.content)
            setTotalElements(getMappedItemsResponse.totalElements)
        },
        [setTotalElements, projectId, getScopeHeaders, getMappedItems]
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

    return (
        <>
            {openCreateMappingDialog && (
                <CreateMappingDialog
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
                <Stack spacing={2} justifyContent="space-between" direction="row" alignItems="center">
                    <Stack direction="row">
                        <FormControlLabel
                            disabled={!selectedMapping}
                            control={<Checkbox checked={checkedFilterIntegratedItems} onChange={handleFilterMappedItemsChange} color="primary" />}
                            label="Hide integrated items"
                        />
                        <Tooltip title={"Apply unmapping of selected items"} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <Button
                                disabled={selectedItems.length <= 0 || mapping === "select"}
                                color="info"
                                variant="contained"
                                onClick={handleClickApplyUnmapping}
                            >
                                <Stack direction="row" spacing={2}>
                                    <Typography>Unmap selected</Typography>
                                    <LinkOff />
                                </Stack>
                            </Button>
                        </Tooltip>
                    </Stack>
                </Stack>
                <MappedItemsTable
                    rowData={rowData}
                    scopeHeaders={scopeHeaders}
                    selectedScope={selectedScope}
                    selectedMapping={selectedMapping}
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
