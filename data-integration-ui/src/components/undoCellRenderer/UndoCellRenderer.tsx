import { IconButton } from "@mui/material"
import { Undo } from "@mui/icons-material"
import { CustomCellRendererProps } from "ag-grid-react"

interface UndoCellRendererProps extends CustomCellRendererProps {
    originalValue: string | undefined
    onUndo: () => void
}

export default function UndoCellRenderer({ value, originalValue, onUndo }: Readonly<UndoCellRendererProps>) {
    const isEdited = originalValue !== undefined && originalValue !== value

    return (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>{value}</span>
            {isEdited && (
                <IconButton size="small" color="default" onClick={onUndo}>
                    <Undo fontSize="small" />
                </IconButton>
            )}
        </div>
    )
}
