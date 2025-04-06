import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Alert,
    Autocomplete,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    FormLabel,
    InputLabel,
    ListItem,
    ListItemIcon,
    ListItemText,
    MenuItem,
    Paper,
    PaperProps,
    Select,
    Slider,
    Stack,
    TextField,
    Tooltip,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../theme"
import { useTranslation } from "react-i18next"
import { AutoFixHigh, ExpandMore, Input, Output, Transform, Warning } from "@mui/icons-material"
import { useCallback, useEffect, useState } from "react"
import { ProjectsApi } from "../../../features/projects/projects.api"
import { useParams } from "react-router-dom"
import { CreateOrUpdateMappingsRequest, Mapping, MappingResponse, ScopeHeaderResponse } from "../../../features/projects/projects.types"
import { FetchBaseQueryError } from "@reduxjs/toolkit/query"
import { useSnackbar } from "notistack"
import VirtualizedList from "../../../components/virtualizedList/VirtualizedList"
import useTargetSystemSection from "./components/targetSystemSection/hooks/useTargetSystemSection"
import useMappingNameSection from "./components/mappingNameSection/hooks/useMappingNameSection"
import MappingNameSection from "./components/mappingNameSection/MappingNameSection"
import TargetSystemSection from "./components/targetSystemSection/TargetSystemSection"
import { HostsApi } from "../../../features/hosts/hosts.api"
import { DataIntegrationHeaderDataAPIResponse } from "../../../features/hosts/hosts.types"
import IconTooltip from "../../tooltip/IconTooltip"
import useShake from "../../shake/hooks/useShake"
import Algorithms from "../../../algorithms/fuzzymatching/Algorithms"
import useConfirmationDialog from "../../confirmationDialog/hooks/useConfirmationDialog"
import ConfirmationDialog from "../../confirmationDialog/ConfirmationDialog"
import GetScopeHeaders from "../../../utils/GetScopeHeaders"

interface HeaderGenerate {
    scopeHeaderResponse: ScopeHeaderResponse
    generate: boolean
}

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

const similarityThresholdDefault = 1
const limitMatchesDefault = 1

export default function CreateOrEditMappingDialog({ open, handleClickClose, scopeId, mappingToEdit }: Readonly<CreateMappingDialogProps>) {
    const { projectId } = useParams()
    const { mappingNameRef, mappingName, setMappingName, mappingNameError, setMappingNameError, handleMappingNameChange } = useMappingNameSection()
    const {
        host,
        setHost,
        hostError,
        setHostError,
        handleHostChange,
        database,
        setDatabase,
        databaseError,
        setDatabaseError,
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
    const [sourceHeaders, setSourceHeaders] = useState<ScopeHeaderResponse[]>([])
    const [targetHeaders, setTargetHeaders] = useState<DataIntegrationHeaderDataAPIResponse[]>([])
    const [mappings, setMappings] = useState<Map<string, ScopeHeaderResponse[]>>(new Map<string, ScopeHeaderResponse[]>())
    const [mappingsError, setMappingsError] = useState<Map<string, boolean>>(new Map<string, boolean>())
    const [generateHeaders, setGenerateHeaders] = useState<Map<string, HeaderGenerate>>(new Map<string, HeaderGenerate>())

    const [algorithm, setAlgorithm] = useState("levenshteinDistance")
    const [enableHeuristic, setEnableHeuristic] = useState(true)
    const [similarityThreshold, setSimilarityThreshold] = useState(similarityThresholdDefault)
    const [limitMatches, setLimitMatches] = useState(limitMatchesDefault)

    const { openConfirmationDialog, handleClickOpenConfirmationDialog, handleClickCloseConfirmationDialog } = useConfirmationDialog()

    const [getScopeHeaders] = ProjectsApi.useLazyGetScopeHeadersQuery()
    const [getHostHeaders] = HostsApi.useLazyGetHostHeadersQuery()
    const [createOrUpdateMapping] = ProjectsApi.useCreateOrUpdateMappingMutation()
    const [createOrUpdateScopeHeaders] = ProjectsApi.useCreateOrUpdateScopeHeadersMutation()

    const { handleShakeClick, shakeSx } = useShake()
    const { enqueueSnackbar } = useSnackbar()
    const translation = useTranslation()

    const handleClickSubmit = async () => {
        let inputError = false
        const inputsMissing: string[] = []
        if (mappingNameRef.current?.value.trim() === "") {
            setMappingNameError(true)
            inputError = true
            inputsMissing.push("Name")
        }
        if (host === "select") {
            setHostError(true)
            inputError = true
            inputsMissing.push("Host")
        }
        if (database === "select") {
            setDatabaseError(true)
            inputError = true
            inputsMissing.push("Database")
        }

        const mappingsError = new Map<string, boolean>()
        mappings.forEach((sources, target) => {
            if (sources.length > 1) {
                mappingsError.set(target, true)
            }
        })
        setMappingsError(mappingsError)
        if (mappingsError.size > 0) {
            inputError = true
        }

        if (inputError) {
            enqueueSnackbar("Inputs are missing:\n" + inputsMissing, { variant: "error" })
            return
        }

        const mapping: Mapping = {}
        mappings.forEach((sources, target) => {
            if (sources.length != 0) {
                if (!mapping[sources[0].id]) {
                    mapping[sources[0].id] = []
                }
                mapping[sources[0].id].push(target)
            }
        })

        const createOrUpdateMappingRequest: CreateOrUpdateMappingsRequest = {
            projectId: projectId!,
            scopeId,
            mappingId: mappingToEdit?.id ?? "",
            databaseId: database,
            mappingName: mappingNameRef.current!.value,
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

    const handleClickAutomapper = async () => {
        const newMappings = new Map<string, ScopeHeaderResponse[]>()
        targetHeaders.forEach(target => {
            const matches = new Map<ScopeHeaderResponse, number>()
            const targetNames = [target.id]
            if (enableHeuristic) {
                targetNames.push(...target.alternatives)
            }
            sourceHeaders.forEach(source => {
                targetNames.forEach(targetName => {
                    const fuzzyAlgorithmResult = Algorithms[algorithm].algorithm(source.display.toLowerCase(), targetName.toLowerCase())
                    const similarity = parseFloat(fuzzyAlgorithmResult.similarity.toFixed(2))
                    if (similarity >= similarityThreshold) {
                        const currentBest = matches.get(source) ?? -1
                        if (similarity > currentBest) {
                            matches.set(source, similarity)
                        }
                    }
                })
            })

            const topMatches = Array.from(matches.entries())
                .sort((a, b) => b[1] - a[1])
                .slice(0, limitMatches)
                .map(([source]) => source)
            newMappings.set(target.id, topMatches)
        })
        setNewMappings(newMappings)

        const scopeGenerateHeaders = Array.from(newMappings)
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            .filter(([_, sources]) => sources.length === 0)
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            .map(([target, _]) => {
                return {
                    id: target,
                    display: target,
                    hidden: false
                } as ScopeHeaderResponse
            })

        if (scopeGenerateHeaders.length > 0) {
            const generateHeaders: Map<string, HeaderGenerate> = new Map<string, HeaderGenerate>()
            scopeGenerateHeaders.forEach(scopeGenerateHeader =>
                generateHeaders.set(scopeGenerateHeader.id, { scopeHeaderResponse: scopeGenerateHeader, generate: true })
            )
            setGenerateHeaders(generateHeaders)
            handleClickOpenConfirmationDialog()
        }
    }

    const setNewMappings = (newMappings: Map<string, ScopeHeaderResponse[]>) => {
        setMappings(newMappings)
        setMappingsError(new Map<string, boolean>())
    }

    const handleClickGenerateHeaders = async (generate: boolean) => {
        if (generate) {
            const headersToGenerate = Array.from(generateHeaders.values())
                .filter(generateHeader => generateHeader.generate)
                .map(generateHeader => generateHeader.scopeHeaderResponse)

            if (headersToGenerate.length > 0) {
                const createOrUpdateScopeHeadersResponse = await createOrUpdateScopeHeaders({ projectId: projectId!, scopeId, headers: headersToGenerate })
                if (createOrUpdateScopeHeadersResponse.error) {
                    enqueueSnackbar("Error occurred during header creation.", { variant: "error" })
                    return
                } else if (createOrUpdateScopeHeadersResponse.data) {
                    const scopeHeaders = createOrUpdateScopeHeadersResponse.data
                    setSourceHeaders(scopeHeaders)

                    const newMappings = new Map(mappings)
                    headersToGenerate.forEach(headerToGenerate => {
                        const scopeHeader = scopeHeaders.find(scopeHeader => scopeHeader.display === headerToGenerate.display)!
                        const target = headerToGenerate.display
                        newMappings.set(target, [scopeHeader])
                    })
                    setNewMappings(newMappings)
                }
            }
        } else {
            setNewMappings(mappings)
        }
        enqueueSnackbar("Automapped sources to targets", { variant: "success" })
        handleClickCloseConfirmationDialog()
    }

    const fetchMappingData = useCallback(async () => {
        if (!selectedHost) {
            return
        }

        const getHostHeadersDataResponse = await getHostHeaders({ hostId: selectedHost.id, language: "en" })
        if (getHostHeadersDataResponse.error) {
            setTargetHeaders([])
        } else if (getHostHeadersDataResponse.data) {
            const hostHeaders = getHostHeadersDataResponse.data.headers
            setTargetHeaders(hostHeaders)
        }

        const getScopeHeadersResponse = await getScopeHeaders({ projectId: projectId!, scopeId })
        if (getScopeHeadersResponse.error) {
            setSourceHeaders([])
        } else if (getScopeHeadersResponse.data) {
            const scopeHeaders = GetScopeHeaders(getScopeHeadersResponse.data)

            if (mappingToEdit) {
                const editedMapping = new Map<string, ScopeHeaderResponse[]>()
                Object.entries(mappingToEdit.mapping).forEach(([target, sources]) => {
                    const foundScopeHeaders = sources.map(source => scopeHeaders.find(scopeHeader => scopeHeader.id === source)!)
                    editedMapping.set(target, foundScopeHeaders)
                })
                setMappings(editedMapping)
            }

            setSourceHeaders(scopeHeaders)
        }
    }, [getHostHeaders, getScopeHeaders, mappingToEdit, projectId, scopeId, selectedHost])

    useEffect(() => {
        fetchMappingData()
    }, [fetchMappingData])

    useEffect(() => {
        if (mappingToEdit) {
            setMappingName(mappingToEdit.name)
            setHost(mappingToEdit.database.host.id)
            setDatabase(mappingToEdit.database.id)
        }
    }, [mappingToEdit, setMappingName, setHost, setDatabase])

    return (
        <>
            {openConfirmationDialog && (
                <ConfirmationDialog
                    open={openConfirmationDialog}
                    handleClickClose={() => handleClickGenerateHeaders(false)}
                    handleClickYes={() => handleClickGenerateHeaders(true)}
                >
                    <Stack spacing={5}>
                        <Stack direction="row">
                            <Warning color="warning" />
                            <Stack spacing={2}>
                                <Typography>{"The following target headers have no suitable source header mapping."}</Typography>
                                <Stack>
                                    <Typography fontWeight="bold">{"Should they be generated?"}</Typography>
                                    <Typography variant="body2" color={theme.palette.text.secondary}>
                                        {"If yes, you can additionally deselect headers you dont want to be generated."}
                                    </Typography>
                                </Stack>
                            </Stack>
                        </Stack>
                        <Stack spacing={1}>
                            {Array.from(generateHeaders.values()).map(generateHeader => (
                                <FormControlLabel
                                    key={generateHeader.scopeHeaderResponse.id}
                                    control={
                                        <Checkbox
                                            color="warning"
                                            checked={generateHeader.generate}
                                            onChange={e =>
                                                setGenerateHeaders(prevGenerateHeaders => {
                                                    const generateHeaders = new Map(prevGenerateHeaders)
                                                    generateHeaders.set(generateHeader.scopeHeaderResponse.id, {
                                                        ...generateHeader,
                                                        generate: e.target.checked
                                                    })
                                                    prevGenerateHeaders.get(generateHeader.scopeHeaderResponse.id)!.generate = e.target.checked
                                                    return generateHeaders
                                                })
                                            }
                                        />
                                    }
                                    label={generateHeader.scopeHeaderResponse.display}
                                />
                            ))}
                        </Stack>
                    </Stack>
                </ConfirmationDialog>
            )}
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
                        <MappingNameSection
                            mappingNameRef={mappingNameRef}
                            defaultValue={mappingName}
                            mappingNameError={mappingNameError}
                            handleMappingNameChange={handleMappingNameChange}
                        />
                        <TargetSystemSection
                            openCreateOrEditHostDialog={openCreateOrEditHostDialog}
                            handleClickCloseCreateOrEditHostDialog={handleClickCloseCreateOrEditHostDialog}
                            host={host}
                            database={database}
                            hostError={hostError}
                            databaseError={databaseError}
                            hostsResponse={hostsResponse}
                            handleHostChange={e => {
                                handleHostChange(e)
                                setMappings(new Map<string, ScopeHeaderResponse[]>())
                                setMappingsError(new Map<string, boolean>())
                                setSourceHeaders([])
                                setTargetHeaders([])
                                setGenerateHeaders(new Map<string, HeaderGenerate>())
                            }}
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
                            <Stack spacing={5}>
                                {selectedHost && targetHeaders.length > 1 && (
                                    <Accordion>
                                        <AccordionSummary expandIcon={<ExpandMore />}>
                                            <Typography>{"Automapper"}</Typography>
                                        </AccordionSummary>
                                        <AccordionDetails>
                                            <Stack direction="row" justifyContent="center" spacing={5}>
                                                <Stack spacing={2}>
                                                    <Tooltip
                                                        title={Algorithms[algorithm].display}
                                                        arrow
                                                        PopperProps={{ style: { zIndex: theme.zIndex.modal } }}
                                                    >
                                                        <FormControl color="warning" sx={{ minWidth: 200, maxWidth: 200, textAlign: "left" }}>
                                                            <InputLabel>Algorithm</InputLabel>
                                                            <Select
                                                                value={algorithm}
                                                                onChange={e => setAlgorithm(e.target.value)}
                                                                color="warning"
                                                                label="Algorithm"
                                                            >
                                                                <MenuItem value="select" disabled>
                                                                    {"Select an algorithm"}
                                                                </MenuItem>
                                                                {Object.entries(Algorithms).map(([key, value]) => (
                                                                    <MenuItem key={key} value={key}>
                                                                        {value.display}
                                                                    </MenuItem>
                                                                ))}
                                                            </Select>
                                                        </FormControl>
                                                    </Tooltip>
                                                    <FormControlLabel
                                                        control={
                                                            <Checkbox
                                                                color="warning"
                                                                checked={enableHeuristic}
                                                                onChange={e => setEnableHeuristic(e.target.checked)}
                                                            />
                                                        }
                                                        label="Enable Heuristic"
                                                    />
                                                </Stack>
                                                <Stack spacing={2}>
                                                    <FormControl sx={{ width: 300 }}>
                                                        <FormLabel>Similarity Threshold</FormLabel>
                                                        <Slider
                                                            color="warning"
                                                            defaultValue={similarityThresholdDefault}
                                                            onChangeCommitted={(_, value) => setSimilarityThreshold(value as number)}
                                                            valueLabelDisplay="auto"
                                                            min={0}
                                                            max={1}
                                                            step={0.01}
                                                        />
                                                    </FormControl>
                                                    <FormControl sx={{ width: 300 }}>
                                                        <FormLabel>Limit matches (top matches)</FormLabel>
                                                        <Slider
                                                            color="warning"
                                                            defaultValue={limitMatchesDefault}
                                                            onChangeCommitted={(_, value) => setLimitMatches(value as number)}
                                                            valueLabelDisplay="auto"
                                                            min={1}
                                                            max={5}
                                                        />
                                                    </FormControl>
                                                </Stack>
                                                <Button
                                                    color="warning"
                                                    variant="contained"
                                                    endIcon={<AutoFixHigh />}
                                                    onClick={handleClickAutomapper}
                                                    sx={{ color: theme.palette.common.white }}
                                                >
                                                    {"Run Automapper"}
                                                </Button>
                                            </Stack>
                                        </AccordionDetails>
                                    </Accordion>
                                )}
                                <>
                                    {!selectedHost && <Alert severity="warning">{"Select a host (target system)"}</Alert>}
                                    {selectedHost && targetHeaders.length === 0 && (
                                        <Alert severity="error">
                                            {"No target headers retrieved, check the target system configuration (url and header path)"}
                                        </Alert>
                                    )}
                                    {mappingsError.size > 0 && (
                                        <Alert severity="error">
                                            {
                                                "One or more source mappings have multiple values selected. Please make sure that each source mapping only has one value selected."
                                            }
                                        </Alert>
                                    )}
                                </>
                                {selectedHost && targetHeaders.length > 0 && (
                                    <Stack alignItems="center">
                                        <Stack direction="row" justifyContent="space-between" sx={{ width: 825 }}>
                                            <Stack alignItems="center" direction="row" spacing={1} paddingLeft={20}>
                                                <Input />
                                                <Typography variant="h6">{"Source"}</Typography>
                                            </Stack>
                                            <Stack alignItems="center" direction="row" spacing={1} paddingRight={20}>
                                                <Typography variant="h6">{"Target"}</Typography>
                                                <Output />
                                            </Stack>
                                        </Stack>
                                        <Stack justifyContent="center" direction="row">
                                            <VirtualizedList
                                                fixedSizeListProps={{
                                                    width: 825,
                                                    height: 500,
                                                    itemSize: 100,
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
                                                    <Stack
                                                        direction="row"
                                                        sx={{
                                                            border:
                                                                "1px solid " +
                                                                (!mappingsError.get(targetHeader.id) ? theme.palette.divider : theme.palette.error.main)
                                                        }}
                                                    >
                                                        <Autocomplete
                                                            id={targetHeader.id}
                                                            value={mappings.get(targetHeader.id)}
                                                            multiple
                                                            disableClearable
                                                            options={sourceHeaders}
                                                            getOptionLabel={option => option?.display}
                                                            isOptionEqualToValue={(option, value) => option.id === value.id}
                                                            onChange={(_, value) => {
                                                                if (value) {
                                                                    setMappings(prevMappings => {
                                                                        const newMappings = new Map(prevMappings)
                                                                        newMappings.set(targetHeader.id, value)
                                                                        return newMappings
                                                                    })
                                                                }
                                                            }}
                                                            sx={{ minWidth: 400, maxWidth: 400, minHeight: 95, maxHeight: 95, overflow: "auto" }}
                                                            renderInput={params => (
                                                                <TextField
                                                                    {...params}
                                                                    InputProps={{
                                                                        ...params.InputProps,
                                                                        startAdornment: (
                                                                            <>
                                                                                <Input />
                                                                                {params.InputProps.startAdornment}
                                                                            </>
                                                                        ),
                                                                        style: {
                                                                            minHeight: 95,
                                                                            maxHeight: 95
                                                                        }
                                                                    }}
                                                                    InputLabelProps={{
                                                                        shrink: true
                                                                    }}
                                                                    sx={{
                                                                        "& .MuiOutlinedInput-root": {
                                                                            "& fieldset": { border: "none" },
                                                                            "&:hover fieldset": { border: "none" },
                                                                            "&.Mui-focused fieldset": { border: "none" }
                                                                        },
                                                                        "& .MuiOutlinedInput-root.Mui-focused": {
                                                                            boxShadow: "none"
                                                                        }
                                                                    }}
                                                                />
                                                            )}
                                                        />
                                                        <ListItem
                                                            sx={{
                                                                minWidth: 400,
                                                                maxWidth: 400,
                                                                minHeight: 95,
                                                                maxHeight: 95,
                                                                borderLeft: "1px solid " + theme.palette.divider
                                                            }}
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
                                                            <IconTooltip color="info" placement="right" messages={[targetHeader.tooltip]} maxHeight={150} />
                                                        </ListItem>
                                                    </Stack>
                                                )}
                                            </VirtualizedList>
                                        </Stack>
                                    </Stack>
                                )}
                            </Stack>
                        </Paper>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button variant="contained" onClick={handleClickSubmit}>
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
