import { createTheme } from "@mui/material/styles"

const tooltipZIndex = 1500
const modalZIndex = 10000

const theme = createTheme({
    palette: {
        primary: {
            main: "#54a433"
        },
        secondary: {
            main: "#404040"
        },
        common: {
            black: "#404040",
            white: "#ffffff",
        },
        background: {
            default: "#ffffff",
            paper: "#ffffff"
        },
        divider: "#dbdbdb",
        text: {
            primary: "#000000",
            secondary: "#696969",
            disabled: "#a5a5a5"
        },
        success: {
            main: "#5CB85C"
        },
        warning: {
            main: "#F0AD4E"
        },
        info: {
            main: "#0000cc",
            light: "#cce6ff"
        },
        error: {
            main: "#FF0000"
        },
        grey: {
            "500": "#f5f5f5"
        }
    },
    zIndex: {
        tooltip: tooltipZIndex,
        modal: modalZIndex
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    width: "fit-content",
                    borderRadius: 25
                }
            }
        }
    }
})

export default theme
