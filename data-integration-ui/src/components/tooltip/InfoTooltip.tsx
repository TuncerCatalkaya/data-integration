import theme from "../../theme"
import { Box, List, ListItem, Tooltip, Typography } from "@mui/material"
import { ReactElement } from "react"

interface InfoTooltipProps {
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
    children: ReactElement
}

export default function InfoTooltip({ messages, placement, maxWidth, children }: Readonly<InfoTooltipProps>) {
    const tooltipContent = (
        <Box
            sx={{
                maxHeight: 100,
                overflowY: "auto"
            }}
        >
            <List sx={{ listStyleType: "disc", pl: messages.length > 1 ? 2 : 0 }}>
                {messages.map((msg, id) => (
                    <ListItem key={msg + id} sx={{ display: "list-item", py: 0.5 }}>
                        <Typography key={msg + id} variant="body2" sx={{ whiteSpace: "pre-wrap" }}>
                            {msg}
                        </Typography>
                    </ListItem>
                ))}
            </List>
        </Box>
    )
    return (
        <Tooltip
            color="info"
            title={tooltipContent}
            placement={placement}
            PopperProps={{ style: { zIndex: theme.zIndex.modal } }}
            componentsProps={{
                tooltip: {
                    sx: {
                        color: theme.palette.common.black,
                        border: `3px dashed ${theme.palette.info.main}`,
                        borderRadius: "8px",
                        backgroundColor: theme.palette.info.light,
                        scrollbarColor: `${theme.palette.info.main} ${theme.palette.info.light}`,
                        scrollbarWidth: "thin",
                        maxWidth: maxWidth ?? 500
                    }
                }
            }}
        >
            {children}
        </Tooltip>
    )
}
