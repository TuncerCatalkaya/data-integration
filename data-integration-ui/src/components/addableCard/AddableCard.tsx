import { Box, Button, Paper, Stack, Typography } from "@mui/material"
import { Remove } from "@mui/icons-material"
import { ReactNode } from "react"

interface AddableCardProps {
    label: string
    index: number
    handleClickRemove: () => void
    removeDisabled: boolean
    children: ReactNode
}

export default function AddableCard({ label, index, handleClickRemove, removeDisabled, children }: Readonly<AddableCardProps>) {
    return (
        <Paper sx={{ padding: "25px" }}>
            <Stack direction="row">
                <Typography variant="h6">{label + " " + index}</Typography>
                <Box flexGrow={1} />
                <Button disabled={removeDisabled} variant="contained" color="error" startIcon={<Remove />} onClick={handleClickRemove}>
                    {"Remove " + label}
                </Button>
            </Stack>
            {children}
        </Paper>
    )
}
