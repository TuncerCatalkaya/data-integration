import {
    Box,
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    PaperProps,
    Select,
    SelectChangeEvent,
    Stack,
    Typography
} from "@mui/material"
import Draggable from "react-draggable"
import theme from "../../../../theme"
import { useTranslation } from "react-i18next"
import { FileOpen, InsertDriveFile } from "@mui/icons-material"
import { VisuallyHiddenInput } from "../../../../components/visuallyHiddenInput/VisuallyHiddenInput"
import { ChangeEvent, useState } from "react"

interface FileBrowserDialogProps {
    open: boolean
    handleClickClose: () => void
    handleFileChange: (e: ChangeEvent<HTMLInputElement>, delimiter: string) => Promise<void>
}

function PaperComponent(props: PaperProps) {
    return (
        <Draggable handle="#import-data-dialog" cancel={'[class*="MuiDialogContent-root"]'}>
            <Paper {...props} />
        </Draggable>
    )
}

export default function ImportDataDialog({ open, handleClickClose, handleFileChange }: Readonly<FileBrowserDialogProps>) {
    const [delimiter, setDelimiter] = useState("select")

    const translation = useTranslation()

    const handleDelimiterChange = async (event: SelectChangeEvent) => {
        const newDelimiter = event.target.value
        setDelimiter(newDelimiter)
    }
    return (
        <Dialog
            open={open}
            onClose={handleClickClose}
            aria-labelledby="import-data-dialog"
            PaperComponent={PaperComponent}
            PaperProps={{
                style: {
                    bottom: "10%"
                }
            }}
            sx={{ zIndex: theme.zIndex.modal }}
        >
            <DialogTitle sx={{ cursor: "move" }}>
                <Stack spacing={1}>
                    <Stack direction="row" display="flex" justifyContent="space-between">
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <InsertDriveFile />
                            <Typography variant="h6">{translation.t("pages.projectImport.components.dialogs.importDataDialog.title")}</Typography>
                        </Stack>
                    </Stack>
                </Stack>
            </DialogTitle>
            <DialogContent>
                <Stack direction="row" alignItems="center" spacing={2} sx={{ padding: "5px" }}>
                    <Box>
                        <Button component="label" role={undefined} variant="contained" tabIndex={-1} startIcon={<FileOpen />}>
                            Choose File
                            <VisuallyHiddenInput
                                type="file"
                                accept=".csv"
                                onChange={event => {
                                    handleFileChange(event, delimiter)
                                    if (delimiter !== "select") {
                                        handleClickClose()
                                    }
                                }}
                            />
                        </Button>
                    </Box>
                    <FormControl sx={{ backgroundColor: theme.palette.common.white, minWidth: "200px" }}>
                        <InputLabel>Delimiter</InputLabel>
                        <Select value={delimiter} label="delimiter" onChange={handleDelimiterChange}>
                            <MenuItem value="select" disabled>
                                {"Select a delimiter"}
                            </MenuItem>
                            <MenuItem value=",">comma (,)</MenuItem>
                            <MenuItem value=";">semicolon (;)</MenuItem>
                            <MenuItem value="\t">tab (\t)</MenuItem>
                            <MenuItem value="|">pipe (|)</MenuItem>
                            <MenuItem value=" ">space ()</MenuItem>
                        </Select>
                    </FormControl>
                </Stack>
            </DialogContent>
        </Dialog>
    )
}
