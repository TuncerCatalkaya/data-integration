import theme from "../../theme"
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, PaperProps } from "@mui/material"
import { Cancel, Check } from "@mui/icons-material"
import Draggable from "react-draggable"
import { useTranslation } from "react-i18next"
import { ReactNode } from "react"

interface ConfirmationDialogProps {
    open: boolean
    handleClickClose: () => void
    handleClickYes: () => void
    children: ReactNode
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#confirmation-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function ConfirmationDialog({ open, handleClickClose, handleClickYes, children }: Readonly<ConfirmationDialogProps>) {
    const translation = useTranslation()
    const handleClickYesButton = () => {
        handleClickYes()
        handleClickClose()
    }
    return (
        <Dialog
            open={open}
            onClose={handleClickClose}
            aria-labelledby="confirmation-dialog"
            PaperComponent={PaperComponent}
            sx={{ zIndex: theme.zIndex.modal + 1 }}
        >
            <DialogTitle sx={{ cursor: "move" }}>{translation.t("components.confirmationDialog.title")}</DialogTitle>
            <DialogContent>{children}</DialogContent>
            <DialogActions>
                <Button variant="contained" onClick={handleClickYesButton} endIcon={<Check />}>
                    {translation.t("components.confirmationDialog.actions.yes")}
                </Button>
                <Button variant="contained" color="error" onClick={handleClickClose} startIcon={<Cancel />}>
                    {translation.t("components.confirmationDialog.actions.no")}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
