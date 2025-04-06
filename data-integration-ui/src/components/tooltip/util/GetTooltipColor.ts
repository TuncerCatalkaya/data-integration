import { OverridableStringUnion } from "@mui/types"
import { SvgIconPropsColorOverrides } from "@mui/material/SvgIcon/SvgIcon"

export default function GetTooltipColor(color: OverridableStringUnion<"info" | "success" | "error", SvgIconPropsColorOverrides>) {
    switch (color) {
        case "info":
            return "#cce6ff"
        case "success":
            return "#ddefdd"
        case "error":
            return "#f5d2d2"
    }
}
