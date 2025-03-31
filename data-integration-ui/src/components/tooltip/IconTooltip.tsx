import theme from "../../theme"
import { Box, Tooltip } from "@mui/material"
import { Cancel, CheckCircle, Info } from "@mui/icons-material"
import { OverridableStringUnion } from "@mui/types"
import { SvgIconPropsColorOverrides } from "@mui/material/SvgIcon/SvgIcon"
import TooltipContent from "./TooltipContent"
import GetTooltipColor from "./util/GetTooltipColor"

interface InfoTooltipProps {
    color: OverridableStringUnion<"info" | "success" | "error", SvgIconPropsColorOverrides>
    messages: string[]
    placement?:
        | "bottom-end"
        | "bottom-start"
        | "bottom"
        | "left-end"
        | "left-start"
        | "left"
        | "right-end"
        | "right-start"
        | "right"
        | "top-end"
        | "top-start"
        | "top"
    maxWidth?: number
    maxHeight?: number
}

export default function IconTooltip({ color, messages, placement, maxWidth, maxHeight }: Readonly<InfoTooltipProps>) {
    const tooltipContentColor = GetTooltipColor(color)

    return (
        <Tooltip
            color={color}
            title={<TooltipContent messages={messages} maxHeight={maxHeight} />}
            placement={placement}
            PopperProps={{ style: { zIndex: theme.zIndex.modal } }}
            componentsProps={{
                tooltip: {
                    sx: {
                        color: theme.palette.common.black,
                        border: `3px dashed ${theme.palette[color].main}`,
                        borderRadius: "8px",
                        backgroundColor: tooltipContentColor,
                        scrollbarColor: `${theme.palette[color].main} ${tooltipContentColor}`,
                        scrollbarWidth: "thin",
                        maxWidth: maxWidth ?? 500
                    }
                }
            }}
        >
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    width: 40,
                    height: 40
                }}
            >
                {color === "info" && <Info color="info" />}
                {color === "success" && <CheckCircle color="success" />}
                {color === "error" && <Cancel color="error" />}
            </Box>
        </Tooltip>
    )
}
