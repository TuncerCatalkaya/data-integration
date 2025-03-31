import { Box } from "@mui/material"
import TooltipContent from "./TooltipContent"
import theme from "../../theme"
import { OverridableStringUnion } from "@mui/types"
import { SvgIconPropsColorOverrides } from "@mui/material/SvgIcon/SvgIcon"
import GetTooltipColor from "./util/GetTooltipColor"

interface ColorTooltipProps {
    color: OverridableStringUnion<"info" | "success" | "error", SvgIconPropsColorOverrides>
    messages: string[]
    maxWidth?: number
    maxHeight?: number
}

export default function ColorTooltip({ color, messages, maxWidth, maxHeight }: Readonly<ColorTooltipProps>) {
    const tooltipContentColor = GetTooltipColor(color)
    return (
        <Box
            style={{
                color: theme.palette.common.black,
                border: `3px dashed ${theme.palette[color].main}`,
                borderRadius: "8px",
                backgroundColor: tooltipContentColor,
                scrollbarColor: `${theme.palette[color].main} ${tooltipContentColor}`,
                scrollbarWidth: "thin",
                maxWidth: maxWidth ?? 500
            }}
        >
            <TooltipContent messages={messages} maxHeight={maxHeight} />
        </Box>
    )
}
