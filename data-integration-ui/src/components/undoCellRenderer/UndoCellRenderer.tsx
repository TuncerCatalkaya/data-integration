import { IconButton } from "@mui/material"
import { Lock, Undo } from "@mui/icons-material"
import { CustomCellRendererProps } from "ag-grid-react"

interface UndoCellRendererProps extends CustomCellRendererProps {
    freeze: boolean
    originalValue: string | undefined
    onUndo: () => void
}

export default function UndoCellRenderer({ value, freeze, originalValue, onUndo }: Readonly<UndoCellRendererProps>) {
    const isEdited = originalValue !== undefined && originalValue !== value
    return (
        <div style={{ display: "flex", alignItems: "center", justifyContent: !freeze ? "space-between" : "left", minHeight: 41 }}>
            {freeze && <Lock fontSize="small" />}
            <span>{value}</span>
            {isEdited && (
                <IconButton size="small" color="default" onClick={onUndo} sx={{ display: freeze ? "none" : "display" }}>
                    <Undo fontSize="small" />
                </IconButton>
            )}
        </div>
    )
}
