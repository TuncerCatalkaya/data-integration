import { ItemStatusResponse } from "../../features/projects/projects.types"
import IconTooltip from "../tooltip/IconTooltip"

interface CellStatusIconProps {
    status: ItemStatusResponse
    errorMessages: string[]
}

export default function CellStatusIcon({ status, errorMessages }: Readonly<CellStatusIconProps>) {
    return (
        <div style={{ alignItems: "center", justifyContent: "center" }}>
            {status === ItemStatusResponse.MAPPED && <IconTooltip color="info" messages={["Mapped"]} placement="right" maxWidth={800} maxHeight={300} />}
            {status === ItemStatusResponse.FAILED && <IconTooltip color="error" messages={errorMessages} placement="right" maxWidth={700} maxHeight={200} />}
            {status === ItemStatusResponse.INTEGRATED && (
                <IconTooltip color="success" messages={["Integrated"]} placement="right" maxWidth={700} maxHeight={300} />
            )}
        </div>
    )
}
