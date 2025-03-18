import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Paper,
    PaperProps,
    Stack,
    TextField,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../../theme"
import {useTranslation} from "react-i18next"
import {Add, Dns, Info} from "@mui/icons-material"
import {ChangeEvent, useEffect, useState} from "react"
import AddableCard from "../../../../components/addableCard/AddableCard"
import {InputField} from "../../../../components/addableCard/AddableCard.types"
import {v4 as uuidv4} from "uuid"
import {HostsApi} from "../../../../features/hosts/hosts.api"
import {CreateOrUpdateHostsRequest, Host} from "../../../../features/hosts/hosts.types"
import {FetchBaseQueryError} from "@reduxjs/toolkit/query"
import {useSnackbar} from "notistack";
import InfoTooltip from "../../../../components/tooltip/InfoTooltip";
import GetFrontendEnvironment from "../../../../utils/GetFrontendEnvironment";

interface CreateOrEditHostDialogProps {
    open: boolean
    handleClickClose: (shouldReload?: boolean) => void
    hostToEdit?: Host
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#create-or-edit-mapping-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function CreateOrEditHostDialog({open, handleClickClose, hostToEdit}: Readonly<CreateOrEditHostDialogProps>) {
    const [hostName, setHostName] = useState("")
    const [hostBaseUrl, setHostBaseUrl] = useState("")
    const [hostIntegrationPath, setHostIntegrationPath] = useState("")
    const [hostGetHeadersPath, setHostGetHeadersPath] = useState("")
    const [databases, setDatabases] = useState<InputField[]>([
        {
            id: uuidv4(),
            dbId: "",
            value: "",
            removeDisabled: false
        }
    ])

    const [hostBaseUrlError, setHostBaseUrlError] = useState(" ")

    const [createOrUpdateHost] = HostsApi.useCreateOrUpdateHostMutation()

    const translation = useTranslation()
    const { enqueueSnackbar } = useSnackbar()

    const handleHostNameChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newHostName = event.target.value
        setHostName(newHostName)
    }
    const handleHostBaseUrlChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newHostBaseUrl = event.target.value
        setHostBaseUrlError(" ")
        setHostBaseUrl(newHostBaseUrl)
    }
    const handleHostIntegrationPathChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newHostIntegrationPath = event.target.value
        setHostIntegrationPath(newHostIntegrationPath)
    }
    const handleHostGetHeaderPathChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const newHostGetHeadersPath = event.target.value
        setHostGetHeadersPath(newHostGetHeadersPath)
    }
    const handleDatabaseChange = (id: string, value: string): void => {
        const updatedDatabases = databases.map(database => {
            if (database.id === id) {
                return {...database, value}
            }
            return database
        })
        setDatabases(updatedDatabases)
    }

    const addDatabase = (): void => setDatabases([...databases, {
        id: uuidv4(),
        dbId: "",
        value: "",
        removeDisabled: false
    }])
    const removeDatabase = (id: string): void => setDatabases(databases.filter(field => field.id !== id))

    const handleClickSubmit = async () => {
        const host: CreateOrUpdateHostsRequest = {
            id: hostToEdit?.id ?? "",
            name: hostName,
            baseUrl: hostBaseUrl,
            integrationPath: hostIntegrationPath,
            getHeadersPath: hostGetHeadersPath,
            databases: databases.map(database => {
                return {
                    id: database.dbId,
                    name: database.value
                }
            })
        }
        const createOrUpdateHostResponse = await createOrUpdateHost(host)
        if (createOrUpdateHostResponse.data) {
            handleClickClose(true)
        } else if (createOrUpdateHostResponse.error) {
            const createOrUpdateHostResponseError = createOrUpdateHostResponse.error as FetchBaseQueryError
            if (createOrUpdateHostResponseError.status === 422) {
                setHostBaseUrlError("URL is not valid")
            } else {
                enqueueSnackbar("Could not create host due to an internal error", { variant: "error" })
            }
        }
    }

    const submitButtonDisabled = hostName.trim() === "" || hostBaseUrl.trim() === "" || hostIntegrationPath.trim() === "" || hostGetHeadersPath.trim() === "" || databases.some(database => database.value === "")

    useEffect(() => {
        if (hostToEdit) {
            setHostName(hostToEdit.name)
            setHostBaseUrl(hostToEdit.baseUrl)
            setHostIntegrationPath(hostToEdit.integrationPath);
            setHostGetHeadersPath(hostToEdit.getHeadersPath);
            setDatabases(
                hostToEdit.databases.map(database => ({
                    id: database.id,
                    dbId: database.id,
                    value: database.name,
                    removeDisabled: database.inUse
                }))
            )
        }
    }, [hostToEdit, open])

    return (
        <Dialog
            open={open}
            onClose={() => handleClickClose()}
            aria-labelledby="create-or-edit-mapping-dialog"
            PaperComponent={PaperComponent}
            sx={{zIndex: theme.zIndex.modal}}
        >
            <DialogTitle sx={{cursor: "move"}}>
                <Stack spacing={1}>
                    <Stack direction="row" display="flex" justifyContent="space-between">
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Dns/>
                            {!hostToEdit && (
                                <Typography variant="h6">
                                    {translation.t("pages.projectImport.components.dialogs.createOrEditHostDialog.create.title")}
                                </Typography>
                            )}
                            {hostToEdit && (
                                <Typography variant="h6">
                                    {translation.t("pages.projectImport.components.dialogs.createOrEditHostDialog.edit.title")}
                                </Typography>
                            )}
                        </Stack>
                    </Stack>
                </Stack>
            </DialogTitle>
            <DialogContent>
                <Stack spacing={2}>
                    <Paper sx={{padding: "25px"}}>
                        <Stack direction="row" alignItems="center" spacing="5px">
                            <Typography variant="h6">Host</Typography>
                            <InfoTooltip messages={["Valid Domains for URL:\n" + GetFrontendEnvironment("VITE_VALID_DOMAINS")]}>
                                <Info color="info" />
                            </InfoTooltip>
                        </Stack>
                        <Stack direction="row" alignItems="center" spacing={2} sx={{padding: "10px"}}>
                            <TextField
                                label="Name"
                                placeholder={"Enter a name..."}
                                value={hostName}
                                helperText={" "}
                                InputLabelProps={{
                                    shrink: true
                                }}
                                onChange={handleHostNameChange}
                            />
                            <TextField
                                label="Base URL"
                                placeholder={"Enter a Base URL..."}
                                value={hostBaseUrl}
                                error={!!hostBaseUrlError.trim()}
                                helperText={hostBaseUrlError}
                                InputLabelProps={{
                                    shrink: true
                                }}
                                onChange={handleHostBaseUrlChange}
                            />
                        </Stack>
                        <Stack direction="row" alignItems="center" spacing={2} sx={{padding: "10px"}}>
                            <TextField
                                label="Integration path"
                                placeholder={"Enter the integration path..."}
                                value={hostIntegrationPath}
                                helperText={" "}
                                InputLabelProps={{
                                    shrink: true
                                }}
                                onChange={handleHostIntegrationPathChange}
                            />
                            <TextField
                                label="Get headers path"
                                placeholder={"Enter the get headers path..."}
                                value={hostGetHeadersPath}
                                helperText={" "}
                                InputLabelProps={{
                                    shrink: true
                                }}
                                onChange={handleHostGetHeaderPathChange}
                            />
                        </Stack>
                    </Paper>
                    {databases.map((database, index) => (
                        <AddableCard
                            key={database.id}
                            label={"Database"}
                            index={index + 1}
                            handleClickRemove={() => removeDatabase(database.id)}
                            removeDisabled={database.removeDisabled ||databases.length == 1}
                        >
                            <Stack direction="row" alignItems="center" spacing={2} sx={{padding: "10px"}}>
                                <TextField
                                    label="Name"
                                    placeholder={"Enter a name..."}
                                    value={database.value}
                                    InputLabelProps={{
                                        shrink: true
                                    }}
                                    onChange={e => handleDatabaseChange(database.id, e.target.value)}
                                />
                            </Stack>
                        </AddableCard>
                    ))}
                    <Button variant="contained" startIcon={<Add/>} onClick={addDatabase}>
                        Add database
                    </Button>
                </Stack>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" disabled={submitButtonDisabled} onClick={handleClickSubmit}>
                    Submit
                </Button>
                <Button variant="contained" color="error" onClick={() => handleClickClose()}>
                    Cancel
                </Button>
            </DialogActions>
        </Dialog>
    )
}
