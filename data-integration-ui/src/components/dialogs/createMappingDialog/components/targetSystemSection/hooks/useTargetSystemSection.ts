import { useCallback, useEffect, useState } from "react"
import { SelectChangeEvent } from "@mui/material"
import { HostsApi } from "../../../../../../features/hosts/hosts.api"
import { HostResponse } from "../../../../../../features/hosts/hosts.types"

export default function useTargetSystemSection() {
    const [host, setHost] = useState("select")
    const [hostError, setHostError] = useState(false)

    const [database, setDatabase] = useState("select")
    const [databaseError, setDatabaseError] = useState(false)

    const [hostsResponse, setHostsResponse] = useState<HostResponse[]>([])

    const [openCreateOrEditHostDialog, setOpenCreateOrEditHostDialog] = useState(false)
    const [isEditMode, setIsEditMode] = useState(false)

    const [getHosts] = HostsApi.useLazyGetHostsQuery()
    const [deleteHost] = HostsApi.useDeleteHostMutation()

    const handleHostChange = (event: SelectChangeEvent) => {
        const newHost = event.target.value
        setHost(newHost)
        setDatabase("select")
        setHostError(false)
        setDatabaseError(false)
    }
    const handleDatabaseChange = (event: SelectChangeEvent) => {
        const newDatabase = event.target.value
        setDatabase(newDatabase)
        setDatabaseError(false)
    }

    const selectedHost = hostsResponse.find(h => h.id === host)

    const handleClickOpenCreateHostDialog = () => {
        setIsEditMode(false)
        setOpenCreateOrEditHostDialog(true)
    }
    const handleClickOpenEditHostDialog = () => {
        setIsEditMode(true)
        setOpenCreateOrEditHostDialog(true)
    }
    const handleClickDeleteHost = async () => {
        await deleteHost({ hostId: selectedHost!.id })
        await fetchHostsData()
        setHost("select")
        setDatabase("select")
    }

    const handleClickCloseCreateOrEditHostDialog = async (shouldReload = false) => {
        setOpenCreateOrEditHostDialog(false)
        if (shouldReload) {
            await fetchHostsData()
        }
    }

    const fetchHostsData = useCallback(async () => {
        const hostsResponse = await getHosts().unwrap()
        setHostsResponse(hostsResponse)
    }, [getHosts])

    useEffect(() => {
        fetchHostsData()
    }, [fetchHostsData])

    return {
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
    }
}
