import { Suspense } from "react"
import { Box, CssBaseline, ThemeProvider } from "@mui/material"
import theme from "./theme"
import { SnackbarProvider } from "notistack"
import { store } from "./store/store"
import { Provider } from "react-redux"
import AppLoader from "./AppLoader"
import i18next from "i18next"
import HttpApi from "i18next-http-backend"
import { initReactI18next } from "react-i18next"
import Languages from "./constants/Languages"
import GetFrontendEnvironment from "./utils/GetFrontendEnvironment"

interface AppProps {
    token?: string
    language: string
}

function App(appProps: Readonly<AppProps>) {
    return (
        <Suspense>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Box
                    sx={{
                        "& .notistack-SnackbarContainer": {
                            [GetFrontendEnvironment("VITE_SNACKBAR_ORIENTATION_VERTICAL")]: GetFrontendEnvironment("VITE_SNACKBAR_OFFSET") + " !important",
                            zIndex: theme.zIndex.modal + 1
                        }
                    }}
                >
                    <SnackbarProvider
                        autoHideDuration={GetFrontendEnvironment("VITE_SNACKBAR_AUTO_HIDE_DURATION")}
                        maxSnack={GetFrontendEnvironment("VITE_SNACKBAR_MAX_SNACK")}
                        preventDuplicate
                        anchorOrigin={{
                            vertical: GetFrontendEnvironment("VITE_SNACKBAR_ORIENTATION_VERTICAL"),
                            horizontal: GetFrontendEnvironment("VITE_SNACKBAR_ORIENTATION_HORIZONTAL")
                        }}
                        style={{ whiteSpace: "pre-wrap" }}
                    >
                        <Provider store={store}>
                            {import.meta.env.DEV ? (
                                <Box display="flex" justifyContent="center" padding={5}>
                                    <AppLoader {...appProps} />
                                </Box>
                            ) : (
                                <AppLoader {...appProps} />
                            )}
                        </Provider>
                    </SnackbarProvider>
                </Box>
            </ThemeProvider>
        </Suspense>
    )
}

i18next
    .use(HttpApi)
    .use(initReactI18next)
    .init({
        backend: {
            loadPath: (import.meta.env.PROD ? window.dataIntegrationBaseUrl : "") + "/locales/{{lng}}/{{ns}}.json"
        },
        lng: "en",
        fallbackLng: Languages.en.language,
        supportedLngs: Object.values(Languages).map(language => language.language),
        debug: import.meta.env.DEV,
        interpolation: {
            escapeValue: false
        }
    })

export default App
