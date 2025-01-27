import { Stack, Typography } from "@mui/material"
import { ReactNode } from "react"

interface ErrorContentProps {
    topNode?: ReactNode
    h2?: string
    h5?: string
}

export default function ErrorContent(errorContentProps: Readonly<ErrorContentProps>) {
    return (
        <Stack alignItems="center" spacing={5}>
            {errorContentProps.topNode}
            <Stack alignItems="center">
                {errorContentProps.h2 && (
                    <Typography color="primary" variant="h2">
                        {errorContentProps.h2}
                    </Typography>
                )}
                {errorContentProps.h5 && (
                    <Typography color="primary" variant="h6">
                        {errorContentProps.h5}
                    </Typography>
                )}
            </Stack>
        </Stack>
    )
}
