import { Backdrop, CircularProgress, Stack, Typography } from "@mui/material"
import { useAppSelector } from "../../store/store"
import DataIntegrationSpinnerStyles from "./DataIntegrationSpinnerStyles"

interface DataIntegrationSpinnerProps {
    determinate?: {
        text: string
        value: number
    }
}

export default function DataIntegrationSpinner({ determinate }: Readonly<DataIntegrationSpinnerProps>) {
    const busyTexts = useAppSelector<string[]>(state => state.busy.texts)

    return (
        <Backdrop component={Stack} open sx={DataIntegrationSpinnerStyles.backdrop}>
            <Stack alignItems="center" sx={DataIntegrationSpinnerStyles.stack}>
                <CircularProgress variant={determinate ? "determinate" : "indeterminate"} value={determinate?.value} />
                {determinate && <Typography color="primary">{determinate.text + `${Math.round(determinate.value)}%`}</Typography>}
                {busyTexts.map(text => (
                    <Typography key={text} color="primary">
                        {text}
                    </Typography>
                ))}
            </Stack>
        </Backdrop>
    )
}
