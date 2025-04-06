import { jwtDecode, JwtPayload } from "jwt-decode"

const SECONDS_TO_MILLISECONDS_CONVERSION = 1000
const ONE_MINUTE_IN_MILLISECONDS = 60000 // buffer because of clock skew

export const AuthUtils = {
    isJwtTokenValid(token?: string) {
        return token ? !isTokenExpired(token) : false
    }
}

function isTokenExpired(token: string) {
    try {
        const decodedToken = jwtDecode<JwtPayload>(token)

        if (decodedToken.exp) {
            return Date.now() >= decodedToken.exp * SECONDS_TO_MILLISECONDS_CONVERSION + ONE_MINUTE_IN_MILLISECONDS
        }

        return false
    } catch (error) {
        return true
    }
}
