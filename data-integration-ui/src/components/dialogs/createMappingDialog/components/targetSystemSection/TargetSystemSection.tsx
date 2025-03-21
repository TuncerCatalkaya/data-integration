import { Button, FormControl, InputLabel, MenuItem, Paper, Select, SelectChangeEvent, Stack, Typography } from "@mui/material"
import theme from "../../../../../theme"
import { Add, Delete, Edit } from "@mui/icons-material"
import { HostResponse } from "../../../../../features/hosts/hosts.types"
import CreateOrEditHostDialog from "../../../createOrEditHostDialog/CreateOrEditHostDialog"
import ConfirmationDialog from "../../../../confirmationDialog/ConfirmationDialog"
import useConfirmationDialog from "../../../../confirmationDialog/hooks/useConfirmationDialog"

interface TargetSystemProps {
    openCreateOrEditHostDialog: boolean
    handleClickCloseCreateOrEditHostDialog: (shouldReload?: boolean) => Promise<void>
    host: string
    database: string
    hostsResponse: HostResponse[]
    handleHostChange: (event: SelectChangeEvent) => void
    handleDatabaseChange: (event: SelectChangeEvent) => void
    isEditMode: boolean
    handleClickOpenCreateHostDialog: () => void
    handleClickOpenEditHostDialog: () => void
    handleClickDeleteHost: () => Promise<void>
    selectedHost?: HostResponse
}

export default function TargetSystemSection({
    openCreateOrEditHostDialog,
    handleClickCloseCreateOrEditHostDialog,
    host,
    database,
    hostsResponse,
    handleHostChange,
    handleDatabaseChange,
    isEditMode,
    handleClickOpenCreateHostDialog,
    handleClickOpenEditHostDialog,
    handleClickDeleteHost,
    selectedHost
}: Readonly<TargetSystemProps>) {
    const { openConfirmationDialog, handleClickCloseConfirmationDialog, handleClickOpenConfirmationDialog } = useConfirmationDialog()
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
                                {"Host Base URL: "} <strong>{selectedHost!.baseUrl}</strong>
                            </Typography>
                        </Stack>
                    </Stack>
                </ConfirmationDialog>
            )}
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
                                            {host.name + `(${host.baseUrl})`}
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
        </>
    )
}
