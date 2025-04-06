import { Box, List, ListItem, Typography } from "@mui/material"

interface TooltipContentProps {
    messages: string[]
    maxHeight?: number
}

export default function TooltipContent({ messages, maxHeight }: Readonly<TooltipContentProps>) {
    return (
        <Box
            sx={{
                maxHeight: maxHeight ?? 100,
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
}
