import { useState } from "react"

export default function useConfirmationDialog() {
    const [openConfirmationDialog, setOpenConfirmationDialog] = useState(false)

    const handleClickOpenConfirmationDialog = () => setOpenConfirmationDialog(true)
    const handleClickCloseConfirmationDialog = () => setOpenConfirmationDialog(false)

    return {
        openConfirmationDialog,
        handleClickOpenConfirmationDialog,
        handleClickCloseConfirmationDialog
    }
}
