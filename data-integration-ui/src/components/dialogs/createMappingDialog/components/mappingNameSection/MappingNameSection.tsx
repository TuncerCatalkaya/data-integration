import { Paper, TextField, Typography } from "@mui/material"
import { ChangeEvent } from "react"

interface MappingNameProps {
    mappingNameKey: string
    mappingName: string
    handleMappingNameChange: (event: ChangeEvent<HTMLInputElement>) => void
}

export default function MappingNameSection({ mappingNameKey, mappingName, handleMappingNameChange }: Readonly<MappingNameProps>) {
    return (
        <Paper sx={{ padding: "25px" }}>
            <Typography variant="h6" sx={{ paddingBottom: "15px" }}>
                Decide a name for the mapping
            </Typography>
            <TextField
                key={mappingNameKey}
                fullWidth
                label={"Name"}
                placeholder={"Enter a name..."}
                defaultValue={mappingName}
                InputLabelProps={{
                    shrink: true
                }}
                onChange={handleMappingNameChange}
            />
        </Paper>
    )
}
