import { Box, Theme } from "@mui/material"
import { useTranslation } from "react-i18next"
import { SxProps } from "@mui/system/styleFunctionSx"

interface ImageBoxProps {
    image?: string
    sx?: SxProps<Theme>
}

export default function ImageBox(imageBoxProps: Readonly<ImageBoxProps>) {
    const translation = useTranslation()

    return <Box component="img" alt={translation.t("components.imageBox.alt")} src={imageBoxProps.image} sx={imageBoxProps.sx} />
}
