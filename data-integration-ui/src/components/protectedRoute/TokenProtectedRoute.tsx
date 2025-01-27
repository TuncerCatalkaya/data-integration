import { ReactNode, useEffect } from "react"
import useAuth from "../../features/auth/hooks/useAuth"

interface TokenProtectedRouteProps {
    component: ReactNode
}

export default function TokenProtectedRoute(tokenProtectedRouteProps: Readonly<TokenProtectedRouteProps>) {
    const { refreshToken } = useAuth()

    useEffect(() => {
        refreshToken()
    }, [refreshToken])

    return <>{tokenProtectedRouteProps.component}</>
}
