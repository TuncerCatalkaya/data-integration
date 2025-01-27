import { createSlice, PayloadAction } from "@reduxjs/toolkit"

interface AuthState {
    token?: string
}

const initialState: AuthState = {}

const AuthSlice = createSlice({
    name: "authSlice",
    initialState,
    reducers: {
        setToken: (state, action: PayloadAction<string | undefined>) => {
            state.token = action.payload
        }
    }
})

export default AuthSlice
