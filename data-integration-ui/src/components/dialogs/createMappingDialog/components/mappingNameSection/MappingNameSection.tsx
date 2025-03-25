import { Paper, TextField, Typography } from "@mui/material"
import { MutableRefObject } from "react"

interface MappingNameProps {
    mappingNameRef: MutableRefObject<HTMLInputElement | undefined>
    defaultValue: string
    mappingNameError: boolean
    handleMappingNameChange: () => void
}

export default function MappingNameSection({ mappingNameRef, defaultValue, mappingNameError, handleMappingNameChange }: Readonly<MappingNameProps>) {
    return (
        <Paper sx={{ padding: "25px" }}>
            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                Decide a name for the mapping
            </Typography>
            <TextField
                key={defaultValue}
                inputRef={mappingNameRef}
                fullWidth
                label={"Name"}
                placeholder={"Enter a name..."}
                defaultValue={defaultValue}
                error={mappingNameError}
                InputLabelProps={{
                    shrink: true
                }}
                onChange={handleMappingNameChange}
            />
        </Paper>
    )
}
