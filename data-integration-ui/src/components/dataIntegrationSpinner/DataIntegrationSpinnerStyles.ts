import theme from "../../theme"

export const DataIntegrationSpinnerStyles = {
    backdrop: {
        zIndex: theme.zIndex.modal + 10,
        backgroundColor: "rgba(255, 255, 255, 0.3)"
    },
    stack: {
        backgroundColor: "rgba(255, 255, 255, 0.9)",
        padding: 1,
        borderRadius: 2,
        boxShadow: "0px 4px 20px rgba(0, 0, 0, 0.1)"
    }
}

export default DataIntegrationSpinnerStyles
