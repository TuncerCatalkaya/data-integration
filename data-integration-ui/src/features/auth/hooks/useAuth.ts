import { useAppDispatch, useAppSelector } from "../../../store/store"
import { useCallback, useMemo } from "react"
import { AuthApi } from "../auth.api"
import AuthSlice from "../auth.slice"
import { AuthUtils } from "../auth.utils"
import GetFrontendEnvironment from "../../../utils/GetFrontendEnvironment"

export default function useAuth() {
    const dispatch = useAppDispatch()

    const token = useAppSelector<string | undefined>(state => state.auth.token)

    const [refreshTokenCall] = AuthApi.useRefreshTokenMutation()

    const refreshToken = useCallback(
        (fn?: (token?: string) => void) => {
            if (!AuthUtils.isJwtTokenValid(token)) {
                refreshTokenCall().then(response => {
                    if (response.data) {
                        const refreshedToken = response.data[GetFrontendEnvironment("VITE_REFRESH_TOKEN_ENDPOINT_ACCESS_TOKEN_NAME")]
                        dispatch(AuthSlice.actions.setToken(refreshedToken))
                        if (fn) {
                            fn(refreshedToken)
                        }
                    } else {
                        dispatch(AuthSlice.actions.setToken(undefined))
                    }
                })
            } else if (fn) {
                fn(token)
            }
        },
        [dispatch, token, refreshTokenCall]
    )

    return useMemo(() => {
        return { token, refreshToken }
    }, [token, refreshToken])
}
