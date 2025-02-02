import {
    Alert,
    Box,
    Button,
    Checkbox,
    CircularProgress,
    Divider,
    FormControl,
    FormControlLabel,
    IconButton,
    InputAdornment,
    InputLabel,
    LinearProgress,
    ListItemIcon,
    Menu,
    MenuItem,
    Select,
    SelectChangeEvent,
    Stack,
    TextField,
    Tooltip,
    Typography
} from "@mui/material"
import {useParams} from "react-router-dom"
import React, {ChangeEvent, useCallback, useEffect, useState} from "react"
import {ProjectsApi} from "../../features/projects/projects.api"
import {useSnackbar} from "notistack"
import FileBrowserDialog from "../projectPage/components/dialogs/FileBrowserDialog"
import {Add, Bolt, Clear, Cloud, CloudDownload, Delete, Edit, FileDownload, Link, Search} from "@mui/icons-material"
import {
    GetCurrentCheckpointStatusResponse,
    ItemResponse,
    MappingResponse,
    ScopeHeaderResponse,
    ScopeResponse
} from "../../features/projects/projects.types"
import usePagination from "../../components/pagination/hooks/usePagination"
import ItemsTable from "./components/itemsTable/ItemsTable"
import theme from "../../theme"
import {ColDef} from "ag-grid-community"
import useConfirmationDialog from "../../components/confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../components/confirmationDialog/ConfirmationDialog"
import {useAppDispatch, useAppSelector} from "../../store/store"
import GenerateScopeKey from "../../utils/GenerateScopeKey"
import GetFrontendEnvironment from "../../utils/GetFrontendEnvironment"
import {FetchBaseQueryError} from "@reduxjs/toolkit/query"
import ImportDataDialog from "./components/importDataDialog/ImportDataDialog"
import CreateMappingDialog from "./components/createMappingDialog/CreateMappingDialog"
import ImportItemsSlice from "../../features/importItems/importItems.slice"
import BulkEditDialog from "./components/bulkEditDialog/BulkEditDialog"
import EditHeaderDialog from "./components/editHeaderDialog/EditHeaderDialog";
import GetScopeHeaders from "../../utils/GetScopeHeaders";
import parse from "filesize-parser";

export default function ProjectImportPage() {
    const { projectId } = useParams()
    const scopesFromStore = useAppSelector<Record<string, string>>(state => state.importItems.scopes)
    const mappingsFromStore = useAppSelector<Record<string, string>>(state => state.importItems.mappings)
    const filterMappedItemsFromStore = useAppSelector<Record<string, boolean>>(state => state.importItems.filterMappedItems)
    const [search, setSearch] = useState<string>("")
    const dispatch = useAppDispatch()

    const [openImportDataDialog, setOpenImportDataDialog] = useState(false)

    const [openCreateMappingDialog, setOpenCreateMappingDialog] = useState(false)
    const [isMappingEditMode, setIsMappingEditMode] = useState(false)

    const [openFileBrowserDialog, setOpenFileBrowserDialog] = useState(false)

    const [openEditHeaderDialog, setOpenEditHeaderDialog] = useState(false)

    const [openBulkEditDialog, setOpenBulkEditDialog] = useState(false)

    const [scope, setScope] = useState(scopesFromStore[projectId!] || "select")
    const [scopesResponse, setScopesResponse] = useState<ScopeResponse[]>([])

    const [mapping, setMapping] = useState(mappingsFromStore[projectId!] || "select")
    const [mappingsResponse, setMappingsResponse] = useState<MappingResponse[]>([])

    const [searchSelectedHeader, setSearchSelectedHeader] = useState("Free Text")

    const [selectedItems, setSelectedItems] = useState<string[]>([])

    const [checkedFilterMappedItems, setCheckedFilterMappedItems] = useState(filterMappedItemsFromStore[projectId!] || false)

    const [importAnchorEl, setImportAnchorEl] = useState<HTMLElement | null>(null)

    const {
        openConfirmationDialog: openDeleteConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseDeleteConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenDeleteConfirmationDialog
    } = useConfirmationDialog()
    const {
        openConfirmationDialog: openInterruptConfirmationDialog,
        handleClickCloseConfirmationDialog: handleClickCloseInterruptConfirmationDialog,
        handleClickOpenConfirmationDialog: handleClickOpenInterruptConfirmationDialog
    } = useConfirmationDialog()
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

    const [createOrGetScope] = ProjectsApi.useCreateOrGetScopeMutation()
    const [importDataFile] = ProjectsApi.useImportDataFileMutation()
    const [importDataS3] = ProjectsApi.useImportDataS3Mutation()
    const [getScopes] = ProjectsApi.useLazyGetScopesQuery()
    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getItems] = ProjectsApi.useLazyGetItemsQuery()
    const [getMappings] = ProjectsApi.useLazyGetMappingsQuery()
    const [interruptScope] = ProjectsApi.useInterruptScopeMutation()
    const [markScopeForDeletion] = ProjectsApi.useMarkScopeForDeletionMutation()
    const [applyMapping] = ProjectsApi.useApplyMappingMutation()
    const [markMappingForDeletion] = ProjectsApi.useMarkMappingForDeletionMutation()
    const [getCurrentCheckpointStatus] = ProjectsApi.useLazyGetCurrentCheckpointStatusQuery()

    const { enqueueSnackbar } = useSnackbar()

    const handleImportMenuOpen = (e: React.MouseEvent<HTMLElement>) => setImportAnchorEl(e.currentTarget)
    const handleImportMenuClose = () => setImportAnchorEl(null)

    const handleClickSearchClear = async () => {
        setSearch("")
        await fetchItemsData(scope, searchSelectedHeader, "", page, pageSize, sort)
    }
    const handleChangeSearch = (e: ChangeEvent<HTMLInputElement>) => setSearch(e.target.value)
    const handleSearchKeyPress = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault()
            await handleClickSearch()
        }
    }
    const handleClickSearch = async () => await fetchItemsData(scope, searchSelectedHeader, search, page, pageSize, sort)

    const handleClickOpenImportDataDialog = () => setOpenImportDataDialog(true)
    const handleClickCloseImportDataDialog = () => setOpenImportDataDialog(false)

    const handleClickOpenCreateMappingDialog = () => {
        setIsMappingEditMode(false)
        setOpenCreateMappingDialog(true)
    }
    const handleClickOpenEditMappingDialog = () => {
        setIsMappingEditMode(true)
        setOpenCreateMappingDialog(true)
    }
    const handleClickCloseCreateMappingDialog = async (shouldReload = false) => {
        setOpenCreateMappingDialog(false)
        if (shouldReload) {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId: scope }).unwrap()
            setMappingsResponse(getMappingsResponse)
        }
    }

    const handleClickOpenFileBrowserDialog = () => setOpenFileBrowserDialog(true)
    const handleClickCloseFileBrowserDialog = () => setOpenFileBrowserDialog(false)

    const handleClickOpenEditHeaderDialog = () => setOpenEditHeaderDialog(true)
    const handleClickCloseEditHeaderDialog = async (shouldReload = false) => {
        setOpenEditHeaderDialog(false)
        if (shouldReload) {
            await fetchItemsData(scope, searchSelectedHeader, search, page, pageSize, sort)
        }
    }

    const handleClickOpenBulkEditDialog = () => setOpenBulkEditDialog(true)
    const handleClickCloseBulkEditDialog = async (shouldReload = false) => {
        setOpenBulkEditDialog(false)
        if (shouldReload) {
            await fetchItemsData(scope, searchSelectedHeader, search, page, pageSize, sort)
        }
    }

    const handleFilterMappedItemsChange = (e: ChangeEvent<HTMLInputElement>) => {
        const checkedFilterMappedItems = e.target.checked
        setCheckedFilterMappedItems(checkedFilterMappedItems)
        dispatch(ImportItemsSlice.actions.putFilterMappedItem({ projectId: projectId!, filterMappedItem: checkedFilterMappedItems }))
    }

    const handleFileChange = async (e: ChangeEvent<HTMLInputElement>, delimiter: string) => {
        if (delimiter === "select") {
            enqueueSnackbar("Please select a delimiter", { variant: "error" })
        } else {
            const files = e.target.files
            if (files) {
                const file = files[0]
                e.target.value = ""
                if (!file.name.toLowerCase().endsWith(".csv")) {
                    enqueueSnackbar("Please select a CSV file", { variant: "error" })
                } else if(file.size > parse(GetFrontendEnvironment("VITE_SMALL_FILE_IMPORT_LIMIT"))) {
                    enqueueSnackbar("The file is too big for small file import", { variant: "error" })
                } else {
                    const scopeKey = GenerateScopeKey(file)
                    const scopeResponse = await createOrGetScope({ projectId: projectId!, scopeKey, external: false }).unwrap()
                    await fetchScopesData()
                    setScope(scopeResponse.id)
                    dispatch(ImportItemsSlice.actions.putScope({ projectId: projectId!, scope: scopeResponse.id }))
                    setShouldStartTimer(true)
                    await importDataFile({ projectId: projectId!, scopeId: scopeResponse.id, delimiter, file });
                    setMapping("select")
                    enqueueSnackbar("Started data import process", { variant: "success" })
                }
            }
        }
        e.target.value = ""
    }

    const handleClickStartImportS3 = async (key: string) => {
        const scopeKey = key.split("/")[1]
        const scopeResponse = await createOrGetScope({
            projectId: projectId!,
            scopeKey,
            external: true
        }).unwrap()
        await fetchScopesData()
        handleClickCloseFileBrowserDialog()
        setScope(scopeResponse.id)
        dispatch(ImportItemsSlice.actions.putScope({ projectId: projectId!, scope: scopeResponse.id }))
        setShouldStartTimer(true)
        const bucket = GetFrontendEnvironment("VITE_S3_BUCKET")
        await importDataS3({ scopeId: scopeResponse.id, bucket, key })
        setMapping("select")
        enqueueSnackbar("Started data import process", { variant: "success" })
    }

    const [currentCheckpointStatus, setCurrentCheckpointStatus] = useState<GetCurrentCheckpointStatusResponse>()
    const [shouldStartTimer, setShouldStartTimer] = useState(false)

    const handleScopeChange = async (event: SelectChangeEvent) => {
        const newScope = event.target.value
        setScope(newScope)
        dispatch(ImportItemsSlice.actions.putScope({ projectId: projectId!, scope: newScope }))
        setCheckedFilterMappedItems(false)
        dispatch(ImportItemsSlice.actions.putFilterMappedItem({ projectId: projectId!, filterMappedItem: false }))
        if (selectedMapping) {
            setMapping("select")
        }
    }

    const selectedMapping = mappingsResponse.find(m => m.id === mapping)
    const handleMappingChange = async (event: SelectChangeEvent) => {
        const newMapping = event.target.value
        setMapping(newMapping)
        dispatch(ImportItemsSlice.actions.putMapping({ projectId: projectId!, mapping: newMapping }))
        setCheckedFilterMappedItems(false)
        dispatch(ImportItemsSlice.actions.putFilterMappedItem({ projectId: projectId!, filterMappedItem: false }))
    }

    const handleSearchSelectedHeaderChange = async (event: SelectChangeEvent) => {
        const searchSelectedHeader = event.target.value
        setSearchSelectedHeader(searchSelectedHeader)
    }

    const [rowData, setRowData] = useState<ItemResponse[]>([])
    const [scopeHeaders, setScopeHeaders] = useState<ScopeHeaderResponse[]>([])
    const [columnDefs, setColumnDefs] = useState<ColDef[]>([])

    const handleClickInterruptScope = async () => await interruptScope({ projectId: projectId!, scopeId: scope })

    const handleClickDeleteScope = async () => {
        await markScopeForDeletion({ projectId: projectId!, scopeId: scope })
        await fetchScopesData()
        setScope("select")
        setColumnDefs([])
        setRowData([])
        setTotalElements(0)
        setCurrentCheckpointStatus(undefined)
        enqueueSnackbar("Deleted scope", { variant: "success" })
    }

    const handleClickDeleteMapping = async () => {
        await markMappingForDeletion({ projectId: projectId!, mappingId: mapping })
        await fetchMappingsData(scope)
        setMapping("select")
        enqueueSnackbar("Deleted mapping", { variant: "success" })
    }

    const handleClickApplyMapping = async () => {
        const applyMappingResponse = await applyMapping({ projectId: projectId!, mappingId: mapping, itemIds: selectedItems })
        if (applyMappingResponse.error) {
            enqueueSnackbar("Error occurred during mapping", { variant: "error" })
        } else {
            await fetchItemsData(scope, searchSelectedHeader, search, page, pageSize, sort)
            enqueueSnackbar("Applied mapping", { variant: "success" })
        }
    }

    const fetchMappingsData = useCallback(
        async (scopeId: string) => {
            const getMappingsResponse = await getMappings({ projectId: projectId!, scopeId }).unwrap()
            setMappingsResponse(getMappingsResponse)
        },
        [projectId, getMappings]
    )

    const fetchItemsData = useCallback(
        async (scopeId: string, searchSelectedHeader: string, search: string, page: number, pageSize: number, sort?: string) => {
            const getItemsResponse = await getItems({
                projectId: projectId!,
                scopeId,
                mappingId: mapping === "select" ? undefined : mapping,
                filterMappedItems: checkedFilterMappedItems,
                header: searchSelectedHeader === "Free Text" ? "" : searchSelectedHeader,
                search,
                page,
                size: pageSize,
                sort
            }).unwrap()
            setRowData(getItemsResponse.content)
            setTotalElements(getItemsResponse.totalElements)
            const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId }).unwrap()
            setScopeHeaders(getScopeHeadersResponse)
            await fetchMappingsData(scopeId)
        },
        [getItems, setTotalElements, projectId, getScopeHeaders, mapping, checkedFilterMappedItems, fetchMappingsData]
    )

    const fetchScopesData = useCallback(async () => {
        const scopesResponse = await getScopes({ projectId: projectId! }).unwrap()
        setScopesResponse(scopesResponse)
    }, [projectId, getScopes])

    useEffect(() => {
        fetchScopesData()
    }, [fetchScopesData])

    const fetchCurrentCheckpointStatus = useCallback(async () => {
        const statusResponse = await getCurrentCheckpointStatus({ projectId: projectId!, scopeId: scope })
        if (statusResponse.error) {
            const statusResponseError = statusResponse.error as FetchBaseQueryError
            setCurrentCheckpointStatus(undefined)
            if (statusResponseError.status === 404) {
                await fetchScopesData()
                setScope("select")
                setColumnDefs([])
                setRowData([])
                setTotalElements(0)
            }
        } else if (statusResponse.data) {
            const statusResponseData = statusResponse.data
            setCurrentCheckpointStatus(statusResponseData)

            if (statusResponseData.finished) {
                await fetchItemsData(scope, "", "", page, pageSize, sort)
            } else {
                setColumnDefs([])
                setRowData([])
                setTotalElements(0)
            }
        }
    }, [getCurrentCheckpointStatus, projectId, scope, fetchItemsData, page, pageSize, sort, setTotalElements, fetchScopesData])

    useEffect(() => {
        if (scope !== "select") {
            fetchCurrentCheckpointStatus()
        }
    }, [fetchCurrentCheckpointStatus, scope])

    useEffect(() => {
        let intervalId: number | null = null

        if (
            !currentCheckpointStatus?.finished &&
            scope !== "select" &&
            (currentCheckpointStatus?.processing || currentCheckpointStatus?.batchesProcessed === -1 || shouldStartTimer)
        ) {
            intervalId = setInterval(async () => {
                const statusResponse = await getCurrentCheckpointStatus({ projectId: projectId!, scopeId: scope })
                if (statusResponse.error) {
                    const statusResponseError = statusResponse.error as FetchBaseQueryError
                    if (statusResponseError.status === 404) {
                        if (intervalId) {
                            clearInterval(intervalId)
                            setShouldStartTimer(false)
                            await fetchScopesData()
                            setScope("select")
                            setColumnDefs([])
                            setRowData([])
                            setTotalElements(0)
                            setCurrentCheckpointStatus(undefined)
                            enqueueSnackbar("Error occurred during data import process. Scope got deleted.", {
                                variant: "warning",
                                autoHideDuration: 10000
                            })
                        }
                    }
                } else if (statusResponse.data) {
                    const statusResponseData = statusResponse.data
                    setCurrentCheckpointStatus(statusResponseData)
                    if (!statusResponseData.processing && (statusResponseData.batchesProcessed !== -1 || statusResponseData.finished)) {
                        if (intervalId) {
                            clearInterval(intervalId)
                            setShouldStartTimer(false)
                            if (statusResponseData.finished) {
                                enqueueSnackbar("Data successfully imported", { variant: "success" })
                                await fetchItemsData(scope, "", "", page, pageSize, sort)
                            }
                        }
                    }
                }
            }, GetFrontendEnvironment("VITE_CURRENT_CHECKPOINT_INTERVAL_IN_MS"))
        }

        return () => {
            if (intervalId) {
                clearInterval(intervalId)
            }
        }
    }, [
        scope,
        currentCheckpointStatus,
        projectId,
        getCurrentCheckpointStatus,
        shouldStartTimer,
        page,
        pageSize,
        sort,
        fetchItemsData,
        fetchScopesData,
        setTotalElements,
        enqueueSnackbar
    ])

    return (
        <>
            {openImportDataDialog && (
                <ImportDataDialog open={openImportDataDialog} handleClickClose={handleClickCloseImportDataDialog} handleFileChange={handleFileChange} />
            )}
            {openCreateMappingDialog && (
                <CreateMappingDialog
                    open={openCreateMappingDialog}
                    handleClickClose={handleClickCloseCreateMappingDialog}
                    scopeId={scope}
                    mappingToEdit={isMappingEditMode ? selectedMapping : undefined}
                />
            )}
            {openFileBrowserDialog && (
                <FileBrowserDialog
                    open={openFileBrowserDialog}
                    handleClickClose={handleClickCloseFileBrowserDialog}
                    projectId={projectId!}
                    handleClickStartImportS3={handleClickStartImportS3}
                />
            )}
            {openInterruptConfirmationDialog && (
                <ConfirmationDialog
                    open={openInterruptConfirmationDialog}
                    handleClickClose={handleClickCloseInterruptConfirmationDialog}
                    handleClickYes={handleClickInterruptScope}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to interrupt the import of the scope?</Typography>
                    </Stack>
                </ConfirmationDialog>
            )}
            {openDeleteConfirmationDialog && (
                <ConfirmationDialog
                    open={openDeleteConfirmationDialog}
                    handleClickClose={handleClickCloseDeleteConfirmationDialog}
                    handleClickYes={handleClickDeleteScope}
                >
                    <Stack spacing={2}>
                        <Typography variant="body1">Are you sure you want to delete the scope?</Typography>
                    </Stack>
                </ConfirmationDialog>
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
            {openEditHeaderDialog && (
                <EditHeaderDialog
                    open={openEditHeaderDialog}
                    handleClickClose={handleClickCloseEditHeaderDialog}
                    scopeId={scope}
                    scopeHeaders={scopeHeaders}
                />
            )}
            {openBulkEditDialog && (
                <BulkEditDialog
                    open={openBulkEditDialog}
                    handleClickClose={handleClickCloseBulkEditDialog}
                    itemIds={rowData.map(data => data.id)}
                    headers={GetScopeHeaders(scopeHeaders)}
                />
            )}
            <Menu anchorEl={importAnchorEl} open={Boolean(importAnchorEl)} onClose={handleImportMenuClose}>
                <MenuItem
                    onClick={() => {
                        handleImportMenuClose()
                        handleClickOpenImportDataDialog()
                    }}
                >
                    <ListItemIcon>
                        <FileDownload fontSize="small" />
                    </ListItemIcon>
                    {"Import Small File =" + GetFrontendEnvironment("VITE_SMALL_FILE_IMPORT_LIMIT")}
                </MenuItem>
                <MenuItem
                    onClick={() => {
                        handleImportMenuClose()
                        handleClickOpenFileBrowserDialog()
                    }}
                >
                    <ListItemIcon>
                        <Cloud fontSize="small" />
                    </ListItemIcon>
                    {"Import Large File >" + GetFrontendEnvironment("VITE_SMALL_FILE_IMPORT_LIMIT")}
                </MenuItem>
            </Menu>
            <Stack spacing={3} width="100vw">
                <Stack spacing={2} justifyContent="space-between" direction="row">
                    <Stack direction="row" spacing={1}>
                        <Tooltip title={scopesResponse.find(s => s.id === scope)?.key} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                            <FormControl sx={{ minWidth: 425, maxWidth: 425, textAlign: "left" }}>
                                <InputLabel>Scope</InputLabel>
                                <Select value={scope} label="Scope" onChange={handleScopeChange} sx={{ backgroundColor: theme.palette.common.white }}>
                                    <MenuItem value="select" disabled>
                                        {"Select a scope / import data"}
                                    </MenuItem>
                                    {scopesResponse.map(scope => (
                                        <MenuItem key={scope.id} value={scope.id}>
                                            {scope.key}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Tooltip>
                        <Button
                            color="error"
                            variant="contained"
                            endIcon={<CloudDownload />}
                            onClick={handleImportMenuOpen}
                            sx={{ backgroundColor: "#C72E49" }}
                        >
                            Import
                        </Button>
                    </Stack>
                    {currentCheckpointStatus && (
                        <Stack
                            direction="row"
                            alignItems="center"
                            spacing={2}
                            sx={{
                                border: "1px solid " + theme.palette.divider,
                                borderRadius: "8px",
                                padding: "5px",
                                background: theme.palette.common.white,
                                width: "30%"
                            }}
                        >
                            <Stack alignItems="center" sx={{ width: "100%" }}>
                                {currentCheckpointStatus?.finished && <Alert>Data is imported and available</Alert>}
                                {!currentCheckpointStatus?.finished && currentCheckpointStatus.totalBatches !== 0 && (
                                    <>
                                        {currentCheckpointStatus.batchesProcessed !== -1 && (
                                            <Typography>
                                                {currentCheckpointStatus?.batchesProcessed}/{currentCheckpointStatus?.totalBatches} Batches
                                            </Typography>
                                        )}
                                        {currentCheckpointStatus.batchesProcessed === -1 && <Typography>{"Starting import..."}</Typography>}
                                        <Box sx={{ width: "100%" }}>
                                            <LinearProgress
                                                variant="determinate"
                                                value={
                                                    currentCheckpointStatus && currentCheckpointStatus.batchesProcessed !== -1
                                                        ? (currentCheckpointStatus?.batchesProcessed / currentCheckpointStatus?.totalBatches) * 100
                                                        : 0
                                                }
                                                sx={{ height: "10px", borderRadius: "8px" }}
                                            />
                                        </Box>
                                    </>
                                )}
                            </Stack>
                            {(currentCheckpointStatus?.processing ||
                                (!currentCheckpointStatus.finished && currentCheckpointStatus.batchesProcessed === -1)) && <CircularProgress size={30} />}
                            {!currentCheckpointStatus?.processing &&
                                !currentCheckpointStatus?.finished &&
                                currentCheckpointStatus?.external &&
                                currentCheckpointStatus.batchesProcessed !== -1 && (
                                    <Alert severity="warning" sx={{ width: "100%" }}>
                                        Manually restart import
                                    </Alert>
                                )}
                            {!currentCheckpointStatus?.processing &&
                                !currentCheckpointStatus?.finished &&
                                !currentCheckpointStatus?.external &&
                                currentCheckpointStatus.batchesProcessed !== -1 && (
                                    <Alert severity="error" sx={{ width: "100%" }}>
                                        Manually delete scope
                                    </Alert>
                                )}
                        </Stack>
                    )}
                </Stack>
                <Stack spacing={2} justifyContent="space-between" direction="row">
                    <Box sx={{ flexGrow: 1 }}>
                        <Stack direction="row" spacing={1} sx={{ display: currentCheckpointStatus?.finished ? "display" : "none" }}>
                            <Tooltip title={mappingsResponse.find(m => m.id === mapping)?.name} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                                <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 200, maxWidth: 200, textAlign: "left" }}>
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
                            <Button variant="contained" color="success" onClick={handleClickOpenCreateMappingDialog} sx={{ color: theme.palette.common.white }}>
                                <Add />
                            </Button>
                            <Button
                                disabled={mapping === "select"}
                                variant="contained"
                                color="warning"
                                onClick={handleClickOpenEditMappingDialog}
                                sx={{ color: theme.palette.common.white }}
                            >
                                <Edit />
                            </Button>
                            <Button disabled={mapping === "select"} variant="contained" color="error" onClick={handleClickOpenMappingDeleteConfirmationDialog}>
                                <Delete />
                            </Button>
                            <Tooltip title={"Apply selected mapping to selected items"} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                                <Button
                                    disabled={selectedItems.length <= 0 || mapping === "select"}
                                    color="info"
                                    variant="contained"
                                    onClick={handleClickApplyMapping}
                                >
                                    <Stack direction="row" spacing={2}>
                                        <Typography>Map selected</Typography>
                                        <Link />
                                    </Stack>
                                </Button>
                            </Tooltip>
                        </Stack>
                    </Box>
                    {scope !== "select" && (
                        <Stack direction="row" spacing={2}>
                            <Button
                                disabled={
                                    scope === "select" ||
                                    currentCheckpointStatus?.finished ||
                                    (!currentCheckpointStatus?.processing && currentCheckpointStatus?.batchesProcessed !== -1)
                                }
                                variant="contained"
                                color="warning"
                                onClick={handleClickOpenInterruptConfirmationDialog}
                                endIcon={<Bolt />}
                                sx={{ color: theme.palette.common.white, minHeight: 56, height: 56 }}
                            >
                                Interrupt
                            </Button>
                            <Button
                                disabled={
                                    scope === "select" ||
                                    currentCheckpointStatus?.processing ||
                                    (!currentCheckpointStatus?.finished && currentCheckpointStatus?.batchesProcessed === -1)
                                }
                                variant="contained"
                                color="error"
                                onClick={handleClickOpenDeleteConfirmationDialog}
                                endIcon={<Delete />}
                            >
                                Delete
                            </Button>
                        </Stack>
                    )}
                </Stack>
                <Divider />
                <Stack spacing={2} justifyContent="space-between" direction="row" alignItems="center">
                    <Stack direction="row" spacing={0.5} alignItems="center">
                        {currentCheckpointStatus?.finished && (
                            <>
                                <Tooltip title={searchSelectedHeader} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                                    <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: 150, maxWidth: 150, textAlign: "left" }}>
                                        <InputLabel>Header</InputLabel>
                                        <Select value={searchSelectedHeader} label="Mapping" onChange={handleSearchSelectedHeaderChange}>
                                            <MenuItem value="Free Text">{"Free Text"}</MenuItem>
                                            {GetScopeHeaders(scopeHeaders).map(scopeHeader => (
                                                <MenuItem key={scopeHeader} value={scopeHeader}>
                                                    {scopeHeader}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Tooltip>
                                <Box component="form" noValidate autoComplete="off">
                                    <TextField
                                        value={search}
                                        label={"Search"}
                                        placeholder={"Search..."}
                                        onChange={handleChangeSearch}
                                        onKeyDown={handleSearchKeyPress}
                                        InputProps={{
                                            endAdornment: (
                                                <InputAdornment position="end">
                                                    {search && (
                                                        <IconButton edge="end" size="small" onClick={handleClickSearchClear}>
                                                            <Clear />
                                                        </IconButton>
                                                    )}
                                                    <Box
                                                        sx={{
                                                            height: "24px",
                                                            borderLeft: "1px solid",
                                                            borderColor: theme.palette.grey[400],
                                                            marginLeft: 1,
                                                            marginRight: 1
                                                        }}
                                                    />
                                                    <IconButton edge="end" size="small" onClick={handleClickSearch}>
                                                        <Search />
                                                    </IconButton>
                                                </InputAdornment>
                                            )
                                        }}
                                        InputLabelProps={{
                                            shrink: true
                                        }}
                                        sx={{ backgroundColor: theme.palette.common.white, minWidth: 250, maxWidth: 250 }}
                                    />
                                </Box>
                                <FormControlLabel
                                    disabled={mapping === "select"}
                                    control={<Checkbox checked={checkedFilterMappedItems} onChange={handleFilterMappedItemsChange} color="primary" />}
                                    label="Hide mapped items"
                                />
                            </>
                        )}
                    </Stack>
                    <Stack direction="row" spacing={3} alignItems="center">
                        {currentCheckpointStatus?.finished && (
                            <>
                                <Button
                                    color="warning"
                                    variant="contained"
                                    endIcon={<Edit />}
                                    onClick={handleClickOpenEditHeaderDialog}
                                    sx={{ color: theme.palette.common.white }}
                                >
                                    {"Edit Header"}
                                </Button>
                                <Tooltip title={"Bulk edit currently shown items of a column"} arrow PopperProps={{ style: { zIndex: theme.zIndex.modal } }}>
                                    <Button
                                        color="warning"
                                        variant="contained"
                                        endIcon={<Edit />}
                                        onClick={handleClickOpenBulkEditDialog}
                                        sx={{ color: theme.palette.common.white }}
                                    >
                                        {"Bulk edit"}
                                    </Button>
                                </Tooltip>
                            </>
                        )}
                    </Stack>
                </Stack>
                <ItemsTable
                    rowData={rowData}
                    scopeHeaders={scopeHeaders}
                    columnDefs={columnDefs}
                    setColumnDefs={setColumnDefs}
                    setSelectedItems={setSelectedItems}
                    mapping={mapping}
                    {...pagination}
                />
            </Stack>
        </>
    )
}
